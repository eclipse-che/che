/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
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
  private final boolean isPredefined;
  private final String serviceAccountName;
  private final KubernetesClientFactory clientFactory;

  @Inject
  public KubernetesNamespaceFactory(
      @Nullable @Named("che.infra.kubernetes.namespace") String namespaceName,
      @Nullable @Named("che.infra.kubernetes.service_account_name") String serviceAccountName,
      KubernetesClientFactory clientFactory) {
    this.namespaceName = namespaceName;
    this.isPredefined = !isNullOrEmpty(namespaceName);
    this.serviceAccountName = serviceAccountName;
    this.clientFactory = clientFactory;
  }

  /**
   * Returns true if namespace is predefined for all workspaces or false if each workspace will be
   * provided with a new namespace.
   */
  public boolean isPredefined() {
    return isPredefined;
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
    final String namespaceName = isPredefined ? this.namespaceName : workspaceId;
    KubernetesNamespace namespace = doCreateNamespace(workspaceId, namespaceName);
    namespace.prepare();

    if (!isPredefined() && !isNullOrEmpty(serviceAccountName)) {
      // prepare service account for workspace only if account name is configured
      // and project is not predefined
      // since predefined project should be prepared during Che deployment
      KubernetesWorkspaceServiceAccount workspaceServiceAccount =
          doCreateServiceAccount(workspaceId, namespaceName);
      workspaceServiceAccount.prepare();
    }

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
    return doCreateNamespace(workspaceId, namespace);
  }

  @VisibleForTesting
  KubernetesNamespace doCreateNamespace(String workspaceId, String name) {
    return new KubernetesNamespace(clientFactory, name, workspaceId);
  }

  @VisibleForTesting
  KubernetesWorkspaceServiceAccount doCreateServiceAccount(
      String workspaceId, String namespaceName) {
    return new KubernetesWorkspaceServiceAccount(
        workspaceId, namespaceName, serviceAccountName, clientFactory);
  }
}
