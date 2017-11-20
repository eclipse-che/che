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
package org.eclipse.che.workspace.infrastructure.openshift.project.pvc;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cleans the workspace related OpenShift resources after {@code WorkspaceRemovedEvent}.
 *
 * <p>Note that depending on a configuration different types of cleaners may be chosen. In case of
 * configuration when new OpenShift project created for each workspace, the whole project will be
 * removed, after workspace removal.
 *
 * @author Anton Korneta
 */
@Singleton
public class WorkspacePVCCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspacePVCCleaner.class);

  private final boolean pvcEnabled;
  private final String projectName;
  private final WorkspaceVolumesStrategy strategy;

  @Inject
  public WorkspacePVCCleaner(
      @Named("che.infra.openshift.pvc.enabled") boolean pvcEnabled,
      @Nullable @Named("che.infra.openshift.project") String projectName,
      WorkspaceVolumesStrategy pvcStrategy) {
    this.pvcEnabled = pvcEnabled;
    this.projectName = projectName;
    this.strategy = pvcStrategy;
  }

  @Inject
  public void subscribe(EventService eventService) {
    if (pvcEnabled && !isNullOrEmpty(projectName))
      eventService.subscribe(
          new EventSubscriber<WorkspaceRemovedEvent>() {
            @Override
            public void onEvent(WorkspaceRemovedEvent event) {
              final String workspaceId = event.getWorkspace().getId();
              try {
                strategy.cleanup(workspaceId);
              } catch (InfrastructureException ex) {
                LOG.error(
                    "Failed to cleanup workspace '{}' data. Cause: {}",
                    workspaceId,
                    ex.getMessage());
              }
            }
          });
  }
}
