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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.fabric8.kubernetes.client.utils.Utils.isNotNullOrEmpty;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import io.fabric8.kubernetes.client.utils.Utils;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
@Singleton
public class KubernetesClientFactory {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesClientFactory.class);

  /** {@link OkHttpClient} instance shared by all Kubernetes clients. */
  private OkHttpClient httpClient;

  /**
   * Default Kubernetes {@link Config} that will be the base configuration to create per-workspace
   * configurations.
   */
  private Config defaultConfig;

  @Inject
  public KubernetesClientFactory(
      @Nullable @Named("che.infra.kubernetes.master_url") String masterUrl,
      @Nullable @Named("che.infra.kubernetes.username") String username,
      @Nullable @Named("che.infra.kubernetes.password") String password,
      @Nullable @Named("che.infra.kubernetes.oauth_token") String oauthToken,
      @Nullable @Named("che.infra.kubernetes.trust_certs") Boolean doTrustCerts,
      @Named("che.infra.kubernetes.client.http.async_requests.max") int maxConcurrentRequests,
      @Named("che.infra.kubernetes.client.http.async_requests.max_per_host")
          int maxConcurrentRequestsPerHost,
      @Named("che.infra.kubernetes.client.http.connection_pool.max_idle") int maxIdleConnections,
      @Named("che.infra.kubernetes.client.http.connection_pool.keep_alive_min")
          int connectionPoolKeepAlive) {
    this.defaultConfig =
        buildDefaultConfig(masterUrl, username, password, oauthToken, doTrustCerts);
    OkHttpClient temporary = HttpClientUtils.createHttpClient(defaultConfig);
    OkHttpClient.Builder builder = temporary.newBuilder();
    ConnectionPool oldPool = temporary.connectionPool();
    builder.connectionPool(
        new ConnectionPool(maxIdleConnections, connectionPoolKeepAlive, TimeUnit.MINUTES));
    oldPool.evictAll();
    this.httpClient = builder.build();
    httpClient.dispatcher().setMaxRequests(maxConcurrentRequests);
    httpClient.dispatcher().setMaxRequestsPerHost(maxConcurrentRequestsPerHost);
  }

  /**
   * Creates an instance of {@link KubernetesClient} that can be used to perform any operation
   * related to a given workspace. </br> For all operations performed in the context of a given
   * workspace (workspace start, workspace stop, etc ...), this method should be used to retrieve a
   * Kubernetes client.
   *
   * @param workspaceId Identifier of the workspace on which Kubernetes operations will be performed
   * @throws InfrastructureException if any error occurs on client instance creation.
   */
  public KubernetesClient create(String workspaceId) throws InfrastructureException {
    Config configForWorkspace = buildConfig(defaultConfig, workspaceId);

    return create(configForWorkspace);
  }

  /**
   * Creates an instance of {@link KubernetesClient} that can be used to perform any operation
   * <strong>that is not related to a given workspace</strong>. </br> For all operations performed
   * in the context of a given workspace (workspace start, workspace stop, etc ...), the {@code
   * create(String workspaceId)} method should be used to retrieve a Kubernetes client.
   *
   * @throws InfrastructureException if any error occurs on client instance creation.
   */
  public KubernetesClient create() throws InfrastructureException {
    return create(buildConfig(defaultConfig, null));
  }

  /** Retrieves the {@link OkHttpClient} instance shared by all Kubernetes clients. */
  protected OkHttpClient getHttpClient() {
    return httpClient;
  }

  /**
   * Retrieves the default Kubernetes {@link Config} that will be the base configuration to create
   * per-workspace configurations.
   */
  protected Config getDefaultConfig() {
    return defaultConfig;
  }

  /**
   * Builds the default Kubernetes {@link Config} that will be the base configuration to create
   * per-workspace configurations.
   */
  protected Config buildDefaultConfig(
      String masterUrl, String username, String password, String oauthToken, Boolean doTrustCerts) {
    ConfigBuilder configBuilder = new ConfigBuilder();
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

    Config config = configBuilder.build();
    return config;
  }

  /**
   * Builds the Kubernetes {@link Config} object based on a default {@link Config} object and an
   * optional workspace Id.
   */
  protected Config buildConfig(Config defaultConfig, @Nullable String workspaceId)
      throws InfrastructureException {
    return defaultConfig;
  }

  protected Interceptor buildKubernetesInterceptor(Config config) {
    return new Interceptor() {
      @Override
      public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (isNotNullOrEmpty(config.getUsername()) && isNotNullOrEmpty(config.getPassword())) {
          Request authReq =
              chain
                  .request()
                  .newBuilder()
                  .addHeader(
                      "Authorization",
                      Credentials.basic(config.getUsername(), config.getPassword()))
                  .build();
          return chain.proceed(authReq);
        } else if (isNotNullOrEmpty(config.getOauthToken())) {
          Request authReq =
              chain
                  .request()
                  .newBuilder()
                  .addHeader("Authorization", "Bearer " + config.getOauthToken())
                  .build();
          return chain.proceed(authReq);
        }
        return chain.proceed(request);
      }
    };
  }

  /**
   * Shuts down the {@link KubernetesClient} by closing it's connection pool. Typically should be
   * called on application tear down.
   */
  public void shutdownClient() {
    ConnectionPool connectionPool = httpClient.connectionPool();
    Dispatcher dispatcher = httpClient.dispatcher();
    ExecutorService executorService =
        httpClient.dispatcher() != null ? httpClient.dispatcher().executorService() : null;

    if (dispatcher != null) {
      dispatcher.cancelAll();
    }

    if (connectionPool != null) {
      connectionPool.evictAll();
    }

    Utils.shutdownExecutorService(executorService);
  }

  /**
   * Creates instance of {@link KubernetesClient} that uses an {@link OkHttpClient} instance derived
   * from the shared {@code httpClient} instance in which interceptors are overriden to authenticate
   * with the credentials (user/password or Oauth token) contained in the {@code config} parameter.
   *
   * @throws InfrastructureException if any error occurs on client instance creation.
   */
  private KubernetesClient create(Config config) throws InfrastructureException {
    OkHttpClient clientHttpClient =
        httpClient.newBuilder().authenticator(Authenticator.NONE).build();
    OkHttpClient.Builder builder = clientHttpClient.newBuilder();
    builder.interceptors().clear();
    clientHttpClient = builder.addInterceptor(buildKubernetesInterceptor(config)).build();

    return new UnclosableKubernetesClient(clientHttpClient, config);
  }

  /**
   * Decorates the {@link DefaultKubernetesClient} so that it can not be closed from the outside.
   */
  private static class UnclosableKubernetesClient extends DefaultKubernetesClient {

    public UnclosableKubernetesClient(OkHttpClient httpClient, Config config) {
      super(httpClient, config);
    }

    @Override
    public void close() {}
  }
}
