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
import static io.fabric8.kubernetes.client.utils.Utils.isNotNullOrEmpty;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.URLUtils;
import io.fabric8.kubernetes.client.utils.Utils;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import io.fabric8.openshift.client.internal.OpenShiftOAuthInterceptor;
import java.io.IOException;
import java.net.URL;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

  private static final String AUTHORIZATION = "Authorization";
  private static final String AUTHORIZE_PATH =
      "oauth/authorize?response_type=token&client_id=openshift-challenging-client";
  private static final String LOCATION = "Location";

  private static final String BEFORE_TOKEN = "access_token=";
  private static final String AFTER_TOKEN = "&expires";

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

  protected Interceptor buildKubernetesInterceptor(Config config) {
    final String oauthToken;
    if (Utils.isNotNullOrEmpty(config.getUsername())
        && Utils.isNotNullOrEmpty(config.getPassword())) {
      synchronized (getHttpClient()) {
        try {
          OkHttpClient.Builder builder = getHttpClient().newBuilder();
          builder.interceptors().clear();
          OkHttpClient clone = builder.build();

          String credential =
              Credentials.basic(config.getUsername(), new String(config.getPassword()));
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

    return new Interceptor() {
      @Override
      public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (isNotNullOrEmpty(oauthToken)) {
          Request authReq =
              chain
                  .request()
                  .newBuilder()
                  .addHeader("Authorization", "Bearer " + oauthToken)
                  .build();
          return chain.proceed(authReq);
        }
        return chain.proceed(request);
      }
    };
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

  @PreDestroy
  private void cleanup() {
    try {
      doCleanup();
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
