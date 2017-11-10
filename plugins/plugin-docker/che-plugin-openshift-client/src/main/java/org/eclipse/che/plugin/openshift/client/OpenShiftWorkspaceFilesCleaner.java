/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.openshift.client;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner;
import org.eclipse.che.api.workspace.server.WorkspaceSubjectRegistry;
import org.eclipse.che.api.workspace.server.event.ServerIdleEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.plugin.openshift.client.exception.OpenShiftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to remove workspace directories in Persistent Volume when a workspace is delete while
 * running on OpenShift. Deleted workspace directories are stored in a list. Upon Che server idling,
 * all of these workspaces are deleted simultaneously from the PVC using a {@link
 * OpenShiftPvcHelper} job.
 *
 * <p>Since deleting a workspace does not immediately remove its files, re-creating a workspace with
 * a previously used name can result in files from the previous workspace still being present.
 *
 * @see WorkspaceFilesCleaner
 * @author amisevsk
 */
@Singleton
public class OpenShiftWorkspaceFilesCleaner implements WorkspaceFilesCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftConnector.class);
  private static final Set<String> deleteQueue = ConcurrentHashMap.newKeySet();
  private OpenshiftWorkspaceEnvironmentProvider workspaceEnvironmentProvider;
  private WorkspaceSubjectRegistry workspaceSubjectRegistry;
  private final String workspacesPvcName;
  private final OpenShiftPvcHelper openShiftPvcHelper;

  @Inject
  public OpenShiftWorkspaceFilesCleaner(
      EventService eventService,
      OpenShiftPvcHelper openShiftPvcHelper,
      OpenshiftWorkspaceEnvironmentProvider workspaceEnvironmentProvider,
      WorkspaceSubjectRegistry workspaceSubjectRegistry,
      @Named("che.openshift.workspaces.pvc.name") String workspacesPvcName) {
    this.workspaceEnvironmentProvider = workspaceEnvironmentProvider;
    this.workspacesPvcName = workspacesPvcName;
    this.openShiftPvcHelper = openShiftPvcHelper;
    this.workspaceSubjectRegistry = workspaceSubjectRegistry;

    if (!workspaceEnvironmentProvider.areWorkspacesExternal()) {
      eventService.subscribe(
          new EventSubscriber<ServerIdleEvent>() {
            @Override
            public void onEvent(ServerIdleEvent event) {
              try {
                deleteWorkspacesInQueue(event);
              } catch (OpenShiftException e) {
                LOG.warn("Error while deleting the workspaces", e);
              }
            }
          });
    }
  }

  @Override
  public void clear(Workspace workspace) throws IOException, ServerException {
    String workspaceName = workspace.getConfig().getName();
    if (isNullOrEmpty(workspaceName)) {
      LOG.error("Could not get workspace name for files removal.");
      return;
    }
    if (workspaceEnvironmentProvider.areWorkspacesExternal()) {
      String workspaceId = workspace.getId();
      LOG.info("Synchronously deleting workspace {} on PVC {}", workspaceId, workspacesPvcName);
      Subject subject = workspaceSubjectRegistry.getWorkspaceStarter(workspaceId);
      if (subject == null) {
        subject = EnvironmentContext.getCurrent().getSubject();
      }
      deleteWorkspacesOnPvc(Arrays.asList(workspaceName), subject);
    } else {
      deleteQueue.add(workspaceName);
    }
  }

  private void deleteWorkspacesInQueue(ServerIdleEvent event) throws OpenShiftException {
    List<String> deleteQueueCopy = new ArrayList<>(deleteQueue);
    if (deleteWorkspacesOnPvc(deleteQueueCopy, null)) {
      deleteQueue.removeAll(deleteQueueCopy);
    }
  }

  private boolean deleteWorkspacesOnPvc(List<String> deleteQueue, Subject subject)
      throws OpenShiftException {
    String[] dirsToDelete = deleteQueue.toArray(new String[deleteQueue.size()]);

    LOG.info("Deleting {} workspaces on PVC {}", deleteQueue.size(), workspacesPvcName);
    boolean successful =
        openShiftPvcHelper.createJobPod(
            subject,
            workspacesPvcName,
            workspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(subject),
            "delete-",
            OpenShiftPvcHelper.Command.REMOVE,
            dirsToDelete);
    return successful;
  }

  /** Clears the list of workspace directories to be deleted. Necessary for testing. */
  @VisibleForTesting
  protected static void clearDeleteQueue() {
    deleteQueue.clear();
  }
}
