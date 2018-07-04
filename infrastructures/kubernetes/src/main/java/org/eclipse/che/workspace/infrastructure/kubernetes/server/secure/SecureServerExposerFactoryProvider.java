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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/** @author Sergii Leshchenko */
public class SecureServerExposerFactoryProvider<T extends KubernetesEnvironment>
    implements Provider<SecureServerExposerFactory<T>> {

  private final boolean agentsAuthEnabled;
  private final String serverExposer;

  private final DefaultSecureServersFactory<T> defaultSecureServersFactory;
  private final Map<String, SecureServerExposerFactory<T>> factories;

  @Inject
  public SecureServerExposerFactoryProvider(
      @Named("che.agents.auth_enabled") boolean agentsAuthEnabled,
      @Named("che.server.secure_exposer") String serverExposer,
      DefaultSecureServersFactory<T> defaultSecureServersFactory,
      Map<String, SecureServerExposerFactory<T>> factories) {
    this.agentsAuthEnabled = agentsAuthEnabled;
    this.serverExposer = serverExposer;
    this.defaultSecureServersFactory = defaultSecureServersFactory;
    this.factories = factories;
  }

  /**
   * Creates instance of {@link SecureServerExposerFactory} that will expose secure servers for
   * runtime with the specified runtime identity.
   */
  @Override
  public SecureServerExposerFactory<T> get() {
    if (!agentsAuthEnabled) {
      // return default secure server exposer because no need to protect servers with authentication
      return defaultSecureServersFactory;
    }

    SecureServerExposerFactory<T> serverExposerFactory = factories.get(serverExposer);
    if (serverExposerFactory == null) {
      throw new ConfigurationException(
          "Unknown secure servers exposer is configured '" + serverExposer + "'. ");
    }

    return serverExposerFactory;
  }
}
