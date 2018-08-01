/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.che.ide.QueryParameters;

/**
 * Provides {@link IdeInitializationStrategy} depending on the loading mode (default or factory).
 *
 * @see IdeInitializationStrategy
 * @see DefaultIdeInitializationStrategy
 * @see FactoryIdeInitializationStrategy
 */
@Singleton
class IdeInitializationStrategyProvider implements Provider<IdeInitializationStrategy> {

  private final IdeInitializationStrategy currentStrategy;

  @Inject
  IdeInitializationStrategyProvider(
      QueryParameters queryParameters,
      DefaultIdeInitializationStrategy defaultStrategy,
      FactoryIdeInitializationStrategy factoryStrategy) {
    final boolean factoryMode = !queryParameters.getByName("factory").isEmpty();

    currentStrategy = factoryMode ? factoryStrategy : defaultStrategy;
  }

  /** @throws IllegalStateException if initialization strategy cannot be found */
  @Override
  public IdeInitializationStrategy get() {
    if (currentStrategy != null) {
      return currentStrategy;
    }

    throw new IllegalStateException("IDE initialization strategy cannot be found.");
  }
}
