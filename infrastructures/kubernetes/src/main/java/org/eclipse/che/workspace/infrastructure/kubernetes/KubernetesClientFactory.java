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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.fabric8.kubernetes.client.utils.Utils.isNotNullOrEmpty;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import io.fabric8.kubernetes.client.utils.ImpersonatorInterceptor;
import io.fabric8.kubernetes.client.utils.Utils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
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

  protected static final Logger REQUEST_LOG = LoggerFactory.getLogger("che.infra.request-logging");

  /** {@link OkHttpClient} instance shared by all Kubernetes clients. */
  private final OkHttpClient httpClient;

  /**
   * Default Kubernetes {@link Config} that will be the base configuration to create per-workspace
   * configurations.
   */
  private Config defaultConfig;

  @Inject
  public KubernetesClientFactory(
      @Nullable @Named("che.infra.kubernetes.master_url") String masterUrl,
      @Nullable @Named("che.infra.kubernetes.trust_certs") Boolean doTrustCerts,
      @Named("che.infra.kubernetes.client.http.async_requests.max") int maxConcurrentRequests,
      @Named("che.infra.kubernetes.client.http.async_requests.max_per_host")
          int maxConcurrentRequestsPerHost,
      @Named("che.infra.kubernetes.client.http.connection_pool.max_idle") int maxIdleConnections,
      @Named("che.infra.kubernetes.client.http.connection_pool.keep_alive_min")
          int connectionPoolKeepAlive,
      EventListener eventListener) {
    this.defaultConfig = buildDefaultConfig(masterUrl, doTrustCerts);
    OkHttpClient temporary = HttpClientUtils.createHttpClient(defaultConfig);
    OkHttpClient.Builder builder = temporary.newBuilder();
    ConnectionPool oldPool = temporary.connectionPool();
    builder.connectionPool(
        new ConnectionPool(maxIdleConnections, connectionPoolKeepAlive, TimeUnit.MINUTES));
    oldPool.evictAll();
    this.httpClient = builder.eventListener(eventListener).build();
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
    Config configForWorkspace = buildConfig(getDefaultConfig(), workspaceId, null);

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
    return create(buildConfig(getDefaultConfig(), null, null));
  }

  /**
   * Shuts down the {@link KubernetesClient} by closing its connection pool. Typically should be
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

  /** Retrieves the {@link OkHttpClient} instance shared by all Kubernetes clients. */
  protected OkHttpClient getHttpClient() {
    return httpClient;
  }

  /**
   * Unlike {@link #getHttpClient()} method, this method always returns an HTTP client that contains
   * interceptors that augment the request with authentication information available in the global
   * context.
   *
   * <p>Unlike {@link #getHttpClient()}, this method creates a new HTTP client instance each time it
   * is called.
   *
   * @return HTTP client with authorization set up
   * @throws InfrastructureException if it is not possible to build the client with authentication
   *     infromation
   */
  public OkHttpClient getAuthenticatedHttpClient() throws InfrastructureException {
    throw new InfrastructureException(
        "Impersonating the current user is not supported in the Kubernetes Client.");
  }

  /**
   * Retrieves the default Kubernetes {@link Config} that will be the base configuration to create
   * per-workspace configurations.
   */
  public Config getDefaultConfig() {
    return defaultConfig;
  }

  /**
   * Builds the default Kubernetes {@link Config} that will be the base configuration to create
   * per-workspace configurations.
   */
  protected Config buildDefaultConfig(String masterUrl, Boolean doTrustCerts) {
    ConfigBuilder configBuilder = new ConfigBuilder();
    if (!isNullOrEmpty(masterUrl)) {
      configBuilder.withMasterUrl(masterUrl);
    }

    if (doTrustCerts != null) {
      configBuilder.withTrustCerts(doTrustCerts);
    }

    return configBuilder.build();
  }

  /**
   * Builds the Kubernetes {@link Config} object based on a provided {@link Config} object and an
   * optional workspace ID.
   */
  protected Config buildConfig(Config config, @Nullable String workspaceId, @Nullable String token)
      throws InfrastructureException {
    return config;
  }

  protected Interceptor buildKubernetesInterceptor(Config config) {
    return chain -> {
      Request request = chain.request();
      if (isNotNullOrEmpty(config.getUsername()) && isNotNullOrEmpty(config.getPassword())) {
        Request authReq =
            chain
                .request()
                .newBuilder()
                .addHeader(
                    "Authorization", Credentials.basic(config.getUsername(), config.getPassword()))
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
    };
  }

  /**
   * Creates instance of {@link KubernetesClient} that uses an {@link OkHttpClient} instance derived
   * from the shared {@code httpClient} instance in which interceptors are overridden to
   * authenticate with the credentials (user/password or Oauth token) contained in the {@code
   * config} parameter.
   */
  private KubernetesClient create(Config config) {
    OkHttpClient clientHttpClient =
        httpClient.newBuilder().authenticator(Authenticator.NONE).build();
    OkHttpClient.Builder builder = clientHttpClient.newBuilder();
    builder.interceptors().clear();

    builder
        .addInterceptor(buildKubernetesInterceptor(config))
        .addInterceptor(new ImpersonatorInterceptor(config));

    initializeRequestTracing(builder);

    clientHttpClient = builder.build();

    return new UnclosableKubernetesClient(clientHttpClient, config);
  }

  protected void initializeRequestTracing(OkHttpClient.Builder builder) {
    if (REQUEST_LOG.isTraceEnabled()) {
      HttpLoggingInterceptor logging = new HttpLoggingInterceptor(REQUEST_LOG::trace);
      logging.setLevel(HttpLoggingInterceptor.Level.BODY);
      builder.addInterceptor(logging);
    }
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
