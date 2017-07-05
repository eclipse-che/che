/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.plugin.openshift.client;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.event.ServerIdleEvent;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * Class used to remove workspace directories in Persistent Volume when a workspace
 * is delete while running on OpenShift. Deleted workspace directories are stored
 * in a list. Upon Che server idling, all of these workspaces are deleted simultaneously
 * from the PVC using a {@link OpenShiftPvcHelper} job.
 * <p>
 * Since deleting a workspace does not immediately remove its files, re-creating a workspace
 * with a previously used name can result in files from the previous workspace still being
 * present.
 *
 * @see WorkspaceFilesCleaner
 * @author amisevsk
 */
@Singleton
public class OpenShiftWorkspaceFilesCleaner implements WorkspaceFilesCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftConnector.class);
    private static final Set<String> deleteQueue = ConcurrentHashMap.newKeySet();
    private final String projectNamespace;
    private final String workspacesPvcName;
    private final OpenShiftPvcHelper openShiftPvcHelper;

    @Inject
    public OpenShiftWorkspaceFilesCleaner(EventService eventService,
                                          OpenShiftPvcHelper openShiftPvcHelper,
                                          @Named("che.openshift.project") String projectNamespace,
                                          @Named("che.openshift.workspaces.pvc.name") String workspacesPvcName) {
        this.projectNamespace = projectNamespace;
        this.workspacesPvcName = workspacesPvcName;
        this.openShiftPvcHelper = openShiftPvcHelper;
        eventService.subscribe(new EventSubscriber<ServerIdleEvent>() {
            @Override
            public void onEvent(ServerIdleEvent event) {
                deleteWorkspacesInQueue(event);
            }
        });
    }

    @Override
    public void clear(Workspace workspace) throws IOException, ServerException {
        String workspaceName = workspace.getConfig().getName();
        if (isNullOrEmpty(workspaceName)) {
            LOG.error("Could not get workspace name for files removal.");
            return;
        }
        deleteQueue.add(workspaceName);
    }

    private void deleteWorkspacesInQueue(ServerIdleEvent event) {
        List<String> deleteQueueCopy = new ArrayList<>(deleteQueue);
        String[] dirsToDelete = deleteQueueCopy.toArray(new String[deleteQueueCopy.size()]);

        LOG.info("Deleting {} workspaces on PVC {}", deleteQueueCopy.size(), workspacesPvcName);
        boolean successful = openShiftPvcHelper.createJobPod(workspacesPvcName,
                                                             projectNamespace,
                                                             "delete-",
                                                             OpenShiftPvcHelper.Command.REMOVE,
                                                             dirsToDelete);
        if (successful) {
            deleteQueue.removeAll(deleteQueueCopy);
        }
    }

    /**
     * Clears the list of workspace directories to be deleted. Necessary for testing.
     */
    @VisibleForTesting
    protected static void clearDeleteQueue() {
        deleteQueue.clear();
    }
}
