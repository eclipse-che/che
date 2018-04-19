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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.lang.String.format;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Provides implementation of {@link ExternalServerExposerStrategy} for configured value.
 *
 * @author Guy Daich
 */
@Singleton
public class ExternalServerExposerStrategyProvider<T extends KubernetesEnvironment>
    implements Provider<ExternalServerExposerStrategy<T>> {

  public static final String STRATEGY_PROPERTY = "che.infra.kubernetes.server_strategy";

  private final ExternalServerExposerStrategy<T> externalServerExposerStrategy;

  @Inject
  public ExternalServerExposerStrategyProvider(
      @Named(STRATEGY_PROPERTY) String strategy,
      Map<String, ExternalServerExposerStrategy<T>> strategies) {
    final ExternalServerExposerStrategy<T> externalServerExposerStrategy = strategies.get(strategy);
    if (externalServerExposerStrategy != null) {
      this.externalServerExposerStrategy = externalServerExposerStrategy;
    } else {
      throw new ConfigurationException(
          format("Unsupported Ingress strategy '%s' configured", strategy));
    }
  }

  @Override
  public ExternalServerExposerStrategy<T> get() {
    return externalServerExposerStrategy;
  }
}
