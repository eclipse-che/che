/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
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
  private final WorkspaceVolumesStrategy strategy;
  private final KubernetesNamespaceFactory namespaceFactory;

  @Inject
  public WorkspacePVCCleaner(
      @Named("che.infra.kubernetes.pvc.enabled") boolean pvcEnabled,
      KubernetesNamespaceFactory namespaceFactory,
      WorkspaceVolumesStrategy pvcStrategy) {
    this.pvcEnabled = pvcEnabled;
    this.namespaceFactory = namespaceFactory;
    this.strategy = pvcStrategy;
  }

  @Inject
  public void subscribe(EventService eventService) {
    if (pvcEnabled && !namespaceFactory.isPredefined())
      eventService.subscribe(
          event -> {
            final String workspaceId = event.getWorkspace().getId();
            try {
              strategy.cleanup(workspaceId);
            } catch (InfrastructureException ex) {
              LOG.error(
                  "Failed to cleanup workspace '{}' data. Cause: {}", workspaceId, ex.getMessage());
            }
          },
          WorkspaceRemovedEvent.class);
  }
}
