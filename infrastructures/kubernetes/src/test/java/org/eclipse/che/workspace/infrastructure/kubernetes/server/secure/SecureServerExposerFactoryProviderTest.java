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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import static org.testng.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class SecureServerExposerFactoryProviderTest {
  @Mock private SecureServerExposerFactory<KubernetesEnvironment> secureServerExposer1;
  @Mock private SecureServerExposerFactory<KubernetesEnvironment> secureServerExposer2;

  private Map<String, SecureServerExposerFactory<KubernetesEnvironment>> factories;

  @BeforeMethod
  public void setUp() {
    factories = new HashMap<>();
    factories.put("exposer1", secureServerExposer1);
    factories.put("exposer2", secureServerExposer2);
  }

  @Test
  public void shouldReturnConfiguredSecureServerExposer() {
    // given
    SecureServerExposerFactoryProvider<KubernetesEnvironment> factoryProvider =
        new SecureServerExposerFactoryProvider<>("exposer1", factories);

    // when
    SecureServerExposerFactory<KubernetesEnvironment> factory = factoryProvider.get();

    // then
    assertSame(factory, secureServerExposer1);
  }

  @Test(
    expectedExceptions = ConfigurationException.class,
    expectedExceptionsMessageRegExp =
        "Unknown secure servers exposer is configured 'non-existing'. Currently supported: exposer1, exposer2."
  )
  public void shouldThrowAnExceptionIfConfiguredSecureServerWasNotFound() {
    // given
    SecureServerExposerFactoryProvider<KubernetesEnvironment> factoryProvider =
        new SecureServerExposerFactoryProvider<>("non-existing", factories);

    // when
    factoryProvider.get();
  }
}
