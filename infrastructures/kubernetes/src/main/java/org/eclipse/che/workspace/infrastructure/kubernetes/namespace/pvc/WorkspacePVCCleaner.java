/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

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
 * Cleans the workspace related Kubernetes resources after {@code WorkspaceRemovedEvent}.
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
  private final String namespaceName;
  private final WorkspaceVolumesStrategy strategy;

  @Inject
  public WorkspacePVCCleaner(
      @Named("che.infra.kubernetes.pvc.enabled") boolean pvcEnabled,
      @Nullable @Named("che.infra.kubernetes.namespace") String namespaceName,
      WorkspaceVolumesStrategy pvcStrategy) {
    this.pvcEnabled = pvcEnabled;
    this.namespaceName = namespaceName;
    this.strategy = pvcStrategy;
  }

  @Inject
  public void subscribe(EventService eventService) {
    if (pvcEnabled && !isNullOrEmpty(namespaceName))
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
