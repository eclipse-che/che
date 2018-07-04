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

import static org.testng.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class SecureServerExposerFactoryProviderTest {
  @Mock private DefaultSecureServersFactory<KubernetesEnvironment> defaultSecureServersFactory;

  @Mock private SecureServerExposerFactory<KubernetesEnvironment> customSecureServerExposer;

  private Map<String, SecureServerExposerFactory<KubernetesEnvironment>> factories =
      new HashMap<>();

  @Test
  public void shouldReturnDefaultSecureServerExposerWhenAgentAuthIsDisabled() {
    // given
    SecureServerExposerFactoryProvider<KubernetesEnvironment> factoryProvider =
        new SecureServerExposerFactoryProvider<>(
            false, "custom", defaultSecureServersFactory, factories);

    // when
    SecureServerExposerFactory<KubernetesEnvironment> factory = factoryProvider.get();

    // then
    assertSame(factory, defaultSecureServersFactory);
  }

  @Test
  public void shouldReturnConfiguredSecureServerExposerWhenAgentAuthIsEnabled() {
    // given
    factories.put("custom", customSecureServerExposer);
    SecureServerExposerFactoryProvider<KubernetesEnvironment> factoryProvider =
        new SecureServerExposerFactoryProvider<>(
            true, "custom", defaultSecureServersFactory, factories);

    // when
    SecureServerExposerFactory<KubernetesEnvironment> factory = factoryProvider.get();

    // then
    assertSame(factory, customSecureServerExposer);
  }

  @Test(expectedExceptions = ConfigurationException.class)
  public void shouldThrowAnExceptionIfConfiguredSecureServerWasNotFound() {
    // given
    SecureServerExposerFactoryProvider<KubernetesEnvironment> factoryProvider =
        new SecureServerExposerFactoryProvider<>(
            true, "non-existing", defaultSecureServersFactory, factories);

    // when
    factoryProvider.get();
  }
}
