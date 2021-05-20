/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.EventListener;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * This {@link KubernetesClientFactory} ensures that we use `che` ServiceAccount and not related to
 * any workspace. It always provides client with default {@link Config}. It's useful for operations
 * that needs permissions of `che` SA, such as operations inside `che` namespace (like creating a
 * ConfigMaps for Gateway router) or some cluster-wide actions (like labeling the namespaces).
 */
@Singleton
public class CheServerKubernetesClientFactory extends KubernetesClientFactory {

  @Inject
  public CheServerKubernetesClientFactory(
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
