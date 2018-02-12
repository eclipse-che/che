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

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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

  private final UnclosableKubernetesClient client;

  @Inject
  public KubernetesClientFactory(
      @Nullable @Named("che.infra.kubernetes.master_url") String masterUrl,
      @Nullable @Named("che.infra.kubernetes.username") String username,
      @Nullable @Named("che.infra.kubernetes.password") String password,
      @Nullable @Named("che.infra.kubernetes.oauth_token") String oauthToken,
      @Nullable @Named("che.infra.kubernetes.trust_certs") Boolean doTrustCerts) {
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
    this.client = new UnclosableKubernetesClient(configBuilder.build());
  }

  /**
   * Creates instance of {@link KubernetesClient}.
   *
   * @throws InfrastructureException if any error occurs on client instance creation.
   */
  public KubernetesClient create() throws InfrastructureException {
    return client;
  }

  @PreDestroy
  private void cleanup() {
    try {
      client.doClose();
    } catch (RuntimeException ex) {
      LOG.error(ex.getMessage());
    }
  }

  /**
   * Decorates the {@link DefaultKubernetesClient} so that it can not be closed from the outside.
   */
  private static class UnclosableKubernetesClient extends DefaultKubernetesClient {

    public UnclosableKubernetesClient(Config config) {
      super(config);
    }

    @Override
    public void close() {}

    void doClose() {
      super.close();
    }
  }
}
