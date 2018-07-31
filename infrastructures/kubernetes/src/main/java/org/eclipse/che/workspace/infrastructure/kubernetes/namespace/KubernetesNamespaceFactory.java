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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;

/**
 * Helps to create {@link KubernetesNamespace} instances.
 *
 * @author Anton Korneta
 */
@Singleton
public class KubernetesNamespaceFactory {

  private final String namespaceName;
  private final KubernetesClientFactory clientFactory;

  @Inject
  public KubernetesNamespaceFactory(
      @Nullable @Named("che.infra.kubernetes.namespace") String namespaceName,
      KubernetesClientFactory clientFactory) {
    this.namespaceName = namespaceName;
    this.clientFactory = clientFactory;
  }

  /**
   * Returns true if namespace is predefined for all workspaces or false if each workspace will be
   * provided with a new namespace.
   */
  public boolean isPredefined() {
    return isNullOrEmpty(namespaceName);
  }

  /**
   * Creates a Kubernetes namespace for the specified workspace.
   *
   * <p>The namespace name will be chosen according to a configuration, and it will be prepared
   * (created if necessary).
   *
   * @param workspaceId identifier of the workspace
   * @return created namespace
   * @throws InfrastructureException if any exception occurs during namespace preparing
   */
  public KubernetesNamespace create(String workspaceId) throws InfrastructureException {
    final String namespaceName =
        isNullOrEmpty(this.namespaceName) ? workspaceId : this.namespaceName;
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, namespaceName, workspaceId);
    namespace.prepare();
    return namespace;
  }

  /**
   * Creates a Kubernetes namespace for the specified workspace.
   *
   * <p>Namespace won't be prepared. This method should be used only in case workspace recovering.
   *
   * @param workspaceId identifier of the workspace
   * @return created namespace
   */
  public KubernetesNamespace create(String workspaceId, String namespace) {
    return new KubernetesNamespace(clientFactory, namespace, workspaceId);
  }
}
