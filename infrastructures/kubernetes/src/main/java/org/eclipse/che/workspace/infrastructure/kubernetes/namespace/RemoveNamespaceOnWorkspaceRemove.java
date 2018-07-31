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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.fabric8.kubernetes.client.KubernetesClientException;
import javax.inject.Named;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for removing Kubernetes namespace on {@code WorkspaceRemovedEvent}.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class RemoveNamespaceOnWorkspaceRemove implements EventSubscriber<WorkspaceRemovedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(RemoveNamespaceOnWorkspaceRemove.class);

  private final KubernetesClientFactory clientFactory;
  private final String namespaceName;

  @Inject
  public RemoveNamespaceOnWorkspaceRemove(
      @Nullable @Named("che.infra.kubernetes.namespace") String namespaceName,
      KubernetesClientFactory clientFactory) {
    this.namespaceName = namespaceName;
    this.clientFactory = clientFactory;
  }

  @Inject
  public void subscribe(EventService eventService) {
    if (isNullOrEmpty(namespaceName)) {
      eventService.subscribe(this);
    }
  }

  @Override
  public void onEvent(WorkspaceRemovedEvent event) {
    try {
      doRemoveNamespace(event.getWorkspace().getId());
    } catch (InfrastructureException e) {
      LOG.warn(
          "Fail to remove Kubernetes namespace for workspace with id {}. Cause: {}",
          event.getWorkspace().getId(),
          e.getMessage());
    }
  }

  @VisibleForTesting
  void doRemoveNamespace(String namespaceName) throws InfrastructureException {
    try {
      clientFactory.create(namespaceName).namespaces().withName(namespaceName).delete();
    } catch (KubernetesClientException e) {
      if (!(e.getCode() == 403)) {
        throw new KubernetesInfrastructureException(e);
      }
      // namespace doesn't exist
    }
  }
}
