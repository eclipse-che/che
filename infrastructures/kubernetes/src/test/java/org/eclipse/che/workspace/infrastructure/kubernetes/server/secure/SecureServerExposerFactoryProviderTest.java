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

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider.SECURE_EXPOSER_IMPL_PROPERTY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider.UNKNOWN_EXPOSER_ERROR_TEMPLATE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider.UNKNOWN_SECURE_SERVER_EXPOSER_CONFIGURED_IN_WS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
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
  private static final String EXPOSER1 = "exposer1";
  private static final String EXPOSER2 = "exposer2";
  private static final String NON_EXISTING_EXPOSER = "non-existing";

  @Mock private SecureServerExposerFactory<KubernetesEnvironment> secureServerExposer1;
  @Mock private SecureServerExposerFactory<KubernetesEnvironment> secureServerExposer2;

  private Map<String, SecureServerExposerFactory<KubernetesEnvironment>> factories;
  private KubernetesEnvironment kubernetesEnvironment;

  @BeforeMethod
  public void setUp() {
    factories = new HashMap<>();
    factories.put(EXPOSER1, secureServerExposer1);
    factories.put(EXPOSER2, secureServerExposer2);
    kubernetesEnvironment = KubernetesEnvironment.builder().build();
  }

  @Test
  public void shouldReturnConfiguredSecureServerExposer() {
    // given
    SecureServerExposerFactoryProvider<KubernetesEnvironment> factoryProvider =
        new SecureServerExposerFactoryProvider<>(EXPOSER1, factories);

    // when
    SecureServerExposerFactory<KubernetesEnvironment> factory =
        factoryProvider.get(kubernetesEnvironment);

    // then
    assertSame(factory, secureServerExposer1);
  }

  @Test(
      expectedExceptions = ConfigurationException.class,
      expectedExceptionsMessageRegExp =
          "Unknown secure servers exposer 'non-existing' is configured. Currently supported: exposer1, exposer2.")
  public void shouldThrowAnExceptionIfConfiguredSecureServerWasNotFound() {
    // given
    new SecureServerExposerFactoryProvider<>(NON_EXISTING_EXPOSER, factories);
  }

  @Test
  public void shouldAddWarningIfSecureServerConfiguredInEnvironmentWasNotFound() {
    // given
    SecureServerExposerFactoryProvider<KubernetesEnvironment> factoryProvider =
        new SecureServerExposerFactoryProvider<>(EXPOSER1, factories);
    kubernetesEnvironment.setAttributes(
        ImmutableMap.of(SECURE_EXPOSER_IMPL_PROPERTY, NON_EXISTING_EXPOSER));
    WarningImpl expectedWarning =
        new WarningImpl(
            UNKNOWN_SECURE_SERVER_EXPOSER_CONFIGURED_IN_WS,
            format(
                UNKNOWN_EXPOSER_ERROR_TEMPLATE,
                NON_EXISTING_EXPOSER,
                String.join(", ", factories.keySet())));

    SecureServerExposerFactory<KubernetesEnvironment> factory =
        factoryProvider.get(kubernetesEnvironment);

    // then
    assertSame(factory, secureServerExposer1);
    assertEquals(kubernetesEnvironment.getWarnings().size(), 1);
    assertEquals(kubernetesEnvironment.getWarnings().get(0), expectedWarning);
  }

  @Test
  public void shouldReturnSecureServerExposerConfiguredInEnvironment() {
    // given
    SecureServerExposerFactoryProvider<KubernetesEnvironment> factoryProvider =
        new SecureServerExposerFactoryProvider<>(EXPOSER1, factories);
    kubernetesEnvironment.setAttributes(ImmutableMap.of(SECURE_EXPOSER_IMPL_PROPERTY, EXPOSER2));

    SecureServerExposerFactory<KubernetesEnvironment> factory =
        factoryProvider.get(kubernetesEnvironment);

    // then
    assertSame(factory, secureServerExposer2);
  }
}
