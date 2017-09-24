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
package org.eclipse.che.workspace.infrastructure.openshift;

import static com.google.common.base.Strings.isNullOrEmpty;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.commons.annotation.Nullable;

/** @author Sergii Leshchenko */
public class OpenShiftClientFactory {
  private final Config config;

  @Inject
  public OpenShiftClientFactory(
      @Nullable @Named("che.infra.openshift.master_url") String masterUrl,
      @Nullable @Named("che.infra.openshift.username") String username,
      @Nullable @Named("che.infra.openshift.password") String password,
      @Nullable @Named("che.infra.openshift.trust_certs") Boolean doTrustCerts) {
    config = new Config();
    if (!isNullOrEmpty(masterUrl)) {
      config.setMasterUrl(masterUrl);
    }

    if (!isNullOrEmpty(username)) {
      config.setUsername(username);
    }

    if (!isNullOrEmpty(password)) {
      config.setPassword(password);
    }

    if (doTrustCerts != null) {
      config.setTrustCerts(doTrustCerts);
    }
  }

  public OpenShiftClient create() {
    return new DefaultOpenShiftClient(config);
  }
}
