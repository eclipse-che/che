/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import static java.util.stream.Collectors.joining;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Provides implementation of {@link SecureServerExposerFactory} according to configuration property
 * with name `che.server.secure_exposer`.
 *
 * @author Sergii Leshchenko
 */
public class SecureServerExposerFactoryProvider<T extends KubernetesEnvironment>
    implements Provider<SecureServerExposerFactory<T>> {

  private final String serverExposer;

  private final Map<String, SecureServerExposerFactory<T>> factories;

  @Inject
  public SecureServerExposerFactoryProvider(
      @Named("che.server.secure_exposer") String serverExposer,
      Map<String, SecureServerExposerFactory<T>> factories) {
    this.serverExposer = serverExposer;
    this.factories = factories;
  }

  /**
   * Creates instance of {@link SecureServerExposerFactory} that will expose secure servers for
   * runtime with the specified runtime identity.
   */
  @Override
  public SecureServerExposerFactory<T> get() {
    SecureServerExposerFactory<T> serverExposerFactory = factories.get(serverExposer);
    if (serverExposerFactory == null) {
      throw new ConfigurationException(
          "Unknown secure servers exposer is configured '"
              + serverExposer
              + "'. "
              + "Currently supported: "
              + factories.keySet().stream().collect(joining(", "))
              + ".");
    }

    return serverExposerFactory;
  }
}
