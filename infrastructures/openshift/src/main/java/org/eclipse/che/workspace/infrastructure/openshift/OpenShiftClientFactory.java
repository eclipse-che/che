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
package org.eclipse.che.workspace.infrastructure.openshift;

import static com.google.common.base.Strings.isNullOrEmpty;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import io.fabric8.openshift.client.internal.OpenShiftOAuthInterceptor;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
@Singleton
public class OpenShiftClientFactory extends KubernetesClientFactory {

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftClientFactory.class);

  @Inject
  public OpenShiftClientFactory(
      @Nullable @Named("che.infra.kubernetes.master_url") String masterUrl,
      @Nullable @Named("che.infra.kubernetes.username") String username,
      @Nullable @Named("che.infra.kubernetes.password") String password,
      @Nullable @Named("che.infra.kubernetes.oauth_token") String oauthToken,
      @Nullable @Named("che.infra.kubernetes.trust_certs") Boolean doTrustCerts) {
    super(masterUrl, username, password, oauthToken, doTrustCerts);
  }

  protected Config buildDefaultConfig(
      String masterUrl, String username, String password, String oauthToken, Boolean doTrustCerts) {
    OpenShiftConfigBuilder configBuilder = new OpenShiftConfigBuilder();
    if (!isNullOrEmpty(masterUrl)) {
      configBuilder.withMasterUrl(masterUrl);
    }

    if (!isNullOrEmpty(username)) {
      configBuilder.withUsername(username);
    }

    if (!isNullOrEmpty(password)) {
      configBuilder.withPassword(password);
    }

    if (!isNullOrEmpty(oauthToken)) {
      configBuilder.withOauthToken(oauthToken);
    }

    if (doTrustCerts != null) {
      configBuilder.withTrustCerts(doTrustCerts);
    }

    Config theConfig = configBuilder.build();
    return theConfig;
  }

  private OpenShiftClient createOC(Config config) throws InfrastructureException {
    OkHttpClient clientHttpClient =
        getHttpClient().newBuilder().authenticator(Authenticator.NONE).build();
    OkHttpClient.Builder builder = clientHttpClient.newBuilder();
    builder.interceptors().clear();
    clientHttpClient =
        builder
            .addInterceptor(
                new OpenShiftOAuthInterceptor(clientHttpClient, OpenShiftConfig.wrap(config)))
            .build();

    return new UnclosableOpenShiftClient(clientHttpClient, config);
  }

  /**
   * Creates instance of {@link OpenShiftClient}.
   *
   * @throws InfrastructureException if any error occurs on client instance creation.
   */
  public OpenShiftClient createOC(String workspaceId) throws InfrastructureException {
    Config configForWorkspace = buildConfig(getDefaultConfig(), workspaceId);
    return createOC(configForWorkspace);
  }

  /**
   * Creates instance of {@link OpenShiftClient}.
   *
   * @throws InfrastructureException if any error occurs on client instance creation.
   */
  public OpenShiftClient createOC() throws InfrastructureException {
    return createOC(buildConfig(getDefaultConfig(), null));
  }

  @PreDestroy
  private void cleanup() {
    OkHttpClient httpClient = getHttpClient();
    try {
      if (httpClient.connectionPool() != null) {
        httpClient.connectionPool().evictAll();
      }
      if (httpClient.dispatcher() != null
          && httpClient.dispatcher().executorService() != null
          && !httpClient.dispatcher().executorService().isShutdown()) {
        httpClient.dispatcher().executorService().shutdown();
      }
    } catch (RuntimeException ex) {
      LOG.error(ex.getMessage());
    }
  }

  /** Decorates the {@link DefaultOpenShiftClient} so that it can not be closed from the outside. */
  private static class UnclosableOpenShiftClient extends DefaultOpenShiftClient {

    public UnclosableOpenShiftClient(OkHttpClient httpClient, Config config) {
      super(
          httpClient,
          config instanceof OpenShiftConfig
              ? (OpenShiftConfig) config
              : new OpenShiftConfig(config));
    }

    @Override
    public void close() {}
  }
}
