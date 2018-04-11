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

/**
 * Provides implementation of {@link ExternalServerExposerStrategy} for configured value.
 *
 * @author Guy Daich
 */
@Singleton
public class ExternalServerExposerStrategyProvider
    implements Provider<ExternalServerExposerStrategy> {

  private final ExternalServerExposerStrategy externalServerExposerStrategy;

  @Inject
  public ExternalServerExposerStrategyProvider(
      @Named("che.infra.kubernetes.server_strategy") String strategy,
      Map<String, ExternalServerExposerStrategy> strategies) {
    final ExternalServerExposerStrategy externalServerExposerStrategy = strategies.get(strategy);
    if (externalServerExposerStrategy != null) {
      this.externalServerExposerStrategy = externalServerExposerStrategy;
    } else {
      throw new ConfigurationException(
          format("Unsupported Ingress strategy '%s' configured", strategy));
    }
  }

  @Override
  public ExternalServerExposerStrategy get() {
    return externalServerExposerStrategy;
  }
}
