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
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import okhttp3.OkHttpClient;

class CheKubernetesClient extends DefaultKubernetesClient {

  public CheKubernetesClient(OkHttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public void close() {}
}
