/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.openshift.client;

import static io.fabric8.kubernetes.client.utils.Utils.isNotNullOrEmpty;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import io.fabric8.openshift.client.internal.OpenShiftOAuthInterceptor;
import java.io.IOException;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class OpenShiftClientFactory {

  private OkHttpClient httpClient;

  @Inject
  public OpenShiftClientFactory(
      OpenshiftWorkspaceEnvironmentProvider workspaceEnvironmentProvider) {
    this.httpClient =
        HttpClientUtils.createHttpClient(workspaceEnvironmentProvider.getDefaultOpenshiftConfig());
  }

  @PreDestroy
  public void cleanup() {
    if (httpClient.connectionPool() != null) {
      httpClient.connectionPool().evictAll();
    }
    if (httpClient.dispatcher() != null
        && httpClient.dispatcher().executorService() != null
        && !httpClient.dispatcher().executorService().isShutdown()) {
      httpClient.dispatcher().executorService().shutdown();
    }
  }

  public CheOpenshiftClient newOcClient(Config config) {
    OkHttpClient clientHttpClient =
        httpClient.newBuilder().authenticator(Authenticator.NONE).build();
    OkHttpClient.Builder builder = clientHttpClient.newBuilder();
    builder.interceptors().clear();
    clientHttpClient =
        builder
            .addInterceptor(
                new OpenShiftOAuthInterceptor(clientHttpClient, OpenShiftConfig.wrap(config)))
            .build();

    return new CheOpenshiftClient(clientHttpClient, config);
  }

  public CheKubernetesClient newKubeClient(Config config) {
    OkHttpClient clientHttpClient =
        httpClient.newBuilder().authenticator(Authenticator.NONE).build();
    OkHttpClient.Builder builder = clientHttpClient.newBuilder();
    builder.interceptors().clear();
    clientHttpClient =
        builder
            .addInterceptor(
                new Interceptor() {
                  @Override
                  public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    if (isNotNullOrEmpty(config.getOauthToken())) {
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
                })
            .build();

    return new CheKubernetesClient(clientHttpClient, config);
  }

  public CheOpenshiftClient newOcClient() {
    return newOcClient(new OpenShiftConfigBuilder().build());
  }

  public CheKubernetesClient newKubeClient() {
    return newKubeClient(new OpenShiftConfigBuilder().build());
  }
}
