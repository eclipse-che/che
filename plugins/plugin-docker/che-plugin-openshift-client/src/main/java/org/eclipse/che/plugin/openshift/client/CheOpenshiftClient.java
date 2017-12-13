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

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.URLUtils;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import java.net.URL;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

class CheOpenshiftClient extends DefaultOpenShiftClient {
  private static final String VERSION = "version";

  public CheOpenshiftClient(OkHttpClient httpClient, Config config) {
    super(
        httpClient,
        config instanceof OpenShiftConfig ? (OpenShiftConfig) config : new OpenShiftConfig(config));
  }

  @Override
  public void close() {}

  public String getVersion() {
    try {
      URL url = new URL(URLUtils.join(getMasterUrl().toString(), VERSION));
      Request.Builder requestBuilder = new Request.Builder().get().url(url);
      Request request = requestBuilder.build();
      Response response = httpClient.newCall(request).execute();
      try (ResponseBody body = response.body()) {
        return body.string();
      }
    } catch (Throwable t) {
      throw KubernetesClientException.launderThrowable(t);
    }
  }
}
