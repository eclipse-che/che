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

import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

/** @author Sergii Leshchenko */
public class OpenShiftClientFactory {
  private final OpenShiftConfig config;

  @Inject
  public OpenShiftClientFactory(
      @Nullable @Named("che.infra.openshift.master_url") String masterUrl,
      @Nullable @Named("che.infra.openshift.username") String username,
      @Nullable @Named("che.infra.openshift.password") String password,
      @Nullable @Named("che.infra.openshift.oauth_token") String oauthToken,
      @Nullable @Named("che.infra.openshift.trust_certs") Boolean doTrustCerts) {
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
    config = configBuilder.build();
  }

  /**
   * Creates instance of {@link OpenShiftClient}.
   *
   * @throws InfrastructureException if any error occurs on client instance creation.
   */
  public OpenShiftClient create() throws InfrastructureException {
    return new DefaultOpenShiftClient(config);
  }
}
