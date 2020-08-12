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

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.EventListener;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.CheInstallationLocation;

@Singleton
public class CheNamespace {

  private final String cheNamespaceName;
  private final CheKubernetesClientFactory clientFactory;
  private final WorkspaceManager workspaceManager;

  @Inject
  public CheNamespace(
      CheInstallationLocation installationLocation,
      CheKubernetesClientFactory clientFactory,
      WorkspaceManager workspaceManager) {
    this.cheNamespaceName = installationLocation.getInstallationLocationNamespace();
    this.clientFactory = clientFactory;
    this.workspaceManager = workspaceManager;
  }

  public ConfigMap createConfigMap(ConfigMap configMap, String workspaceId)
      throws InfrastructureException {
    validate(workspaceId);

    putLabel(configMap, CHE_WORKSPACE_ID_LABEL, workspaceId);
    try {
      return clientFactory.create().configMaps().inNamespace(cheNamespaceName).create(configMap);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  private void validate(String workspaceId) throws InfrastructureException {
    if (cheNamespaceName == null) {
      throw new InfrastructureException("Unable to determine Che installation location");
    }

    try {
      Workspace ws = workspaceManager.getWorkspace(workspaceId);
      if (ws.getStatus() != WorkspaceStatus.STARTING) {
        throw new InfrastructureException("only for starting workspace");
      }
    } catch (NotFoundException | ServerException e) {
      throw new InfrastructureException(e);
    }
  }

  public void cleanUp(String workspaceId) throws InfrastructureException {
    cleanUpConfigMaps(workspaceId);
  }

  private void cleanUpConfigMaps(String workspaceId) throws InfrastructureException {
    try {
      clientFactory
          .create()
          .configMaps()
          .inNamespace(cheNamespaceName)
          .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
          .withPropagationPolicy("Background")
          .delete();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  @Singleton
  private static class CheKubernetesClientFactory extends KubernetesClientFactory {

    @Inject
    public CheKubernetesClientFactory(
        @Nullable @Named("che.infra.kubernetes.master_url") String masterUrl,
        @Nullable @Named("che.infra.kubernetes.trust_certs") Boolean doTrustCerts,
        @Named("che.infra.kubernetes.client.http.async_requests.max") int maxConcurrentRequests,
        @Named("che.infra.kubernetes.client.http.async_requests.max_per_host")
            int maxConcurrentRequestsPerHost,
        @Named("che.infra.kubernetes.client.http.connection_pool.max_idle") int maxIdleConnections,
        @Named("che.infra.kubernetes.client.http.connection_pool.keep_alive_min")
            int connectionPoolKeepAlive,
        EventListener eventListener) {
      super(
          masterUrl,
          doTrustCerts,
          maxConcurrentRequests,
          maxConcurrentRequestsPerHost,
          maxIdleConnections,
          connectionPoolKeepAlive,
          eventListener);
    }

    /** @param workspaceId ignored */
    @Override
    public KubernetesClient create(String workspaceId) throws InfrastructureException {
      return create();
    }

    /**
     * creates an instance of {@link KubernetesClient} that is meant to be used on Che installation
     * namespace
     */
    @Override
    public KubernetesClient create() throws InfrastructureException {
      return super.create();
    }

    @Override
    protected Config buildConfig(Config config, String workspaceId) {
      return config;
    }
  }
}
