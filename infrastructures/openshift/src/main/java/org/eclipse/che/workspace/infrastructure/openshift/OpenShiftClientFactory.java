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
package org.eclipse.che.workspace.infrastructure.openshift;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.fabric8.kubernetes.client.utils.Utils.isNotNullOrEmpty;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.ImpersonatorInterceptor;
import io.fabric8.kubernetes.client.utils.URLUtils;
import io.fabric8.kubernetes.client.utils.Utils;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import io.fabric8.openshift.client.internal.OpenShiftOAuthInterceptor;
import java.net.URL;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
@Singleton
public class OpenShiftClientFactory extends KubernetesClientFactory {

  private static final String AUTHORIZATION = "Authorization";
  private static final String AUTHORIZE_PATH =
      "oauth/authorize?response_type=token&client_id=openshift-challenging-client";
  private static final String LOCATION = "Location";

  private static final String BEFORE_TOKEN = "access_token=";
  private static final String AFTER_TOKEN = "&expires";

  private final OpenShiftClientConfigFactory configBuilder;

  @Inject
  public OpenShiftClientFactory(
      OpenShiftClientConfigFactory configBuilder,
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
    this.configBuilder = configBuilder;
  }

  /**
   * Creates an instance of {@link OpenShiftClient} that can be used to perform any operation
   * related to a given workspace. </br> <strong>Important note: </strong> However, in some
   * use-cases involving web sockets, the Openshift client may introduce connection leaks. That's
   * why this method should only be used for API calls that are specific to Openshift and thus not
   * available in the {@link KubernetesClient} class: mainly route-related calls and project-related
   * calls. For all other Kubernetes standard calls, prefer the {@code create(String workspaceId)}
   * method that returns a Kubernetes client.
   *
   * @param workspaceId Identifier of the workspace on which Openshift operations will be performed
   * @throws InfrastructureException if any error occurs on client instance creation.
   */
  public OpenShiftClient createOC(String workspaceId) throws InfrastructureException {
    Config configForWorkspace = buildConfig(getDefaultConfig(), workspaceId);
    return createOC(configForWorkspace);
  }

  /**
   * Creates an instance of {@link OpenShiftClient} that can be used to perform any operation
   * <strong>that is not related to a given workspace</strong>. </br> For operations performed in
   * the context of a given workspace (workspace start, workspace stop, etc ...), the {@code
   * createOC(String workspaceId)} method should be used to retrieve an Openshift client. </br>
   * <strong>Important note: </strong> However in some use-cases involving web sockets, the
   * Openshift client may introduce connection leaks. That's why this method should only be used for
   * API calls that are specific to Openshift and thus not available in the {@link KubernetesClient}
   * class: mainly route-related calls and project-related calls. For all other Kubernetes standard
   * calls, just use the {@code create()} or {@code create(String workspaceId)} methods that return
   * a Kubernetes client.
   *
   * @throws InfrastructureException if any error occurs on client instance creation.
   */
  public OpenShiftClient createOC() throws InfrastructureException {
    return createOC(buildConfig(getDefaultConfig(), null));
  }

  @Override
  public OkHttpClient getAuthenticatedHttpClient() throws InfrastructureException {
    if (!configBuilder.isPersonalized()) {
      throw new InfrastructureException(
          "Not able to construct impersonating openshift API client.");
    }
    return clientForConfig(buildConfig(getDefaultConfig(), null));
  }

  @Override
  protected Config buildDefaultConfig(String masterUrl, Boolean doTrustCerts) {
    OpenShiftConfigBuilder configBuilder = new OpenShiftConfigBuilder();
    if (!isNullOrEmpty(masterUrl)) {
      configBuilder.withMasterUrl(masterUrl);
    }

    if (doTrustCerts != null) {
      configBuilder.withTrustCerts(doTrustCerts);
    }

    return configBuilder.build();
  }

  /**
   * Builds the Openshift {@link Config} object based on a provided {@link Config} object and an
   * optional workspace ID.
   *
   * <p>This method overrides the one in the Kubernetes infrastructure to introduce an additional
   * extension level by delegating to an {@link OpenShiftClientConfigFactory}
   */
  @Override
  protected Config buildConfig(Config config, @Nullable String workspaceId)
      throws InfrastructureException {
    return configBuilder.buildConfig(config, workspaceId);
  }

  @Override
  protected Interceptor buildKubernetesInterceptor(Config config) {
    final String oauthToken;
    if (Utils.isNotNullOrEmpty(config.getUsername())
        && Utils.isNotNullOrEmpty(config.getPassword())) {
      synchronized (getHttpClient()) {
        try {
          OkHttpClient.Builder builder = getHttpClient().newBuilder();
          builder.interceptors().clear();
          OkHttpClient clone = builder.build();

          String credential = Credentials.basic(config.getUsername(), config.getPassword());
          URL url = new URL(URLUtils.join(config.getMasterUrl(), AUTHORIZE_PATH));
          Response response =
              clone
                  .newCall(
                      new Request.Builder()
                          .get()
                          .url(url)
                          .header(AUTHORIZATION, credential)
                          .build())
                  .execute();

          // False positive warn: according to javadocs response.body() returns non-null value
          // if called after Call.execute()
          response.body().close();
          response = response.priorResponse() != null ? response.priorResponse() : response;
          response = response.networkResponse() != null ? response.networkResponse() : response;
          String token = response.header(LOCATION);
          if (token == null || token.isEmpty()) {
            throw new KubernetesClientException(
                "Unexpected response ("
                    + response.code()
                    + " "
                    + response.message()
                    + "), to the authorization request. Missing header:["
                    + LOCATION
                    + "]!");
          }
          token = token.substring(token.indexOf(BEFORE_TOKEN) + BEFORE_TOKEN.length());
          token = token.substring(0, token.indexOf(AFTER_TOKEN));
          oauthToken = token;
        } catch (Exception e) {
          throw KubernetesClientException.launderThrowable(e);
        }
      }
    } else if (Utils.isNotNullOrEmpty(config.getOauthToken())) {
      oauthToken = config.getOauthToken();
    } else {
      oauthToken = null;
    }

    return chain -> {
      Request request = chain.request();
      if (isNotNullOrEmpty(oauthToken)) {
        Request authReq =
            chain.request().newBuilder().addHeader("Authorization", "Bearer " + oauthToken).build();
        return chain.proceed(authReq);
      }
      return chain.proceed(request);
    };
  }

  private OpenShiftClient createOC(Config config) {
    return new UnclosableOpenShiftClient(
        clientForConfig(config), config, this::initializeRequestTracing);
  }

  private OkHttpClient clientForConfig(Config config) {
    OkHttpClient clientHttpClient =
        getHttpClient().newBuilder().authenticator(Authenticator.NONE).build();
    OkHttpClient.Builder builder = clientHttpClient.newBuilder();
    builder.interceptors().clear();
    return builder
        .addInterceptor(
            new OpenShiftOAuthInterceptor(clientHttpClient, OpenShiftConfig.wrap(config)))
        .addInterceptor(new ImpersonatorInterceptor(config))
        .build();
  }

  /** Decorates the {@link DefaultOpenShiftClient} so that it can not be closed from the outside. */
  private static class UnclosableOpenShiftClient extends DefaultOpenShiftClient {

    public UnclosableOpenShiftClient(
        OkHttpClient httpClient, Config config, Consumer<OkHttpClient.Builder> clientModifier) {
      super(
          httpClient,
          config instanceof OpenShiftConfig
              ? (OpenShiftConfig) config
              : new OpenShiftConfig(config));

      // the super constructor resets the http client configuration, so we enable the callers to
      // override the client config after it has modified in the super constructor
      OkHttpClient.Builder bld = this.httpClient.newBuilder();
      clientModifier.accept(bld);
      this.httpClient = bld.build();
    }

    @Override
    public void close() {}
  }
}
