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
package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenshiftProviderConfigFactoryTest {
  private static final String TEST_TOKEN = "touken";

  private Config defaultConfig;

  private OpenshiftProviderConfigFactory openshiftProviderConfigFactory =
      new OpenshiftProviderConfigFactory();

  @BeforeMethod
  public void setUp() {
    defaultConfig = new ConfigBuilder().build();
  }

  @Test
  public void createClientWithToken() {
    Config resultConfig =
        openshiftProviderConfigFactory.buildConfig(defaultConfig, null, TEST_TOKEN);

    assertEquals(resultConfig.getOauthToken(), TEST_TOKEN);
    assertNotEquals(resultConfig, defaultConfig);
  }

  @Test
  public void createClientWithBearerToken() {
    Config resultConfig =
        openshiftProviderConfigFactory.buildConfig(defaultConfig, null, "Bearer " + TEST_TOKEN);

    assertEquals(resultConfig.getOauthToken(), TEST_TOKEN);
    assertNotEquals(resultConfig, defaultConfig);
  }

  @Test
  public void getDefaultConfigWhenNoTokenPassed() {
    Config resultConfig = openshiftProviderConfigFactory.buildConfig(defaultConfig, null, null);

    assertEquals(resultConfig, defaultConfig);
  }
}
