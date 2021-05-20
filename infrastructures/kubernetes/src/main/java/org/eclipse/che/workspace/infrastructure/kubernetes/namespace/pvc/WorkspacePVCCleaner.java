/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cleans the workspace related Kubernetes resources after {@link WorkspaceRemovedEvent}.
 *
 * <p>Note that depending on a configuration different types of cleaners may be chosen. In case of
 * configuration when new Kubernetes namespace created for each workspace, the whole namespace will
 * be removed, after workspace removal.
 *
 * @author Anton Korneta
 */
@Singleton
public class WorkspacePVCCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspacePVCCleaner.class);

  private final boolean pvcEnabled;
  private final WorkspaceVolumesStrategy strategy;

  @Inject
  public WorkspacePVCCleaner(
      @Named("che.infra.kubernetes.pvc.enabled") boolean pvcEnabled,
      WorkspaceVolumesStrategy pvcStrategy) {
    this.pvcEnabled = pvcEnabled;
    this.strategy = pvcStrategy;
  }

  @Inject
  public void subscribe(EventService eventService) {
    if (pvcEnabled) {
      eventService.subscribe(
          event -> {
            final Workspace workspace = event.getWorkspace();
            try {
              strategy.cleanup(workspace);
            } catch (InfrastructureException ex) {
              LOG.error(
                  "Failed to cleanup workspace '{}' data. Cause: {}",
                  workspace.getId(),
                  ex.getMessage());
            }
          },
          WorkspaceRemovedEvent.class);
    }
  }
}
