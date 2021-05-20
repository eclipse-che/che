/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.Map;
import org.eclipse.che.inject.ConfigurationException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GatewayConfigmapLabelsTest {

  @Test(dataProvider = "isGatewayConfigData")
  public void testIsGatewayConfig(
      String labelsProperty, Map<String, String> labels, boolean isGatewayConfigExpected) {
    GatewayConfigmapLabels gatewayConfigmapLabels = new GatewayConfigmapLabels(labelsProperty);
    ConfigMap cm =
        new ConfigMapBuilder().withNewMetadata().withLabels(labels).endMetadata().build();
    assertEquals(gatewayConfigmapLabels.isGatewayConfig(cm), isGatewayConfigExpected);
  }

  @Test(expectedExceptions = ConfigurationException.class)
  public void failsToConstructWhenLabelsAreNull() {
    new GatewayConfigmapLabels(null);
  }

  @Test(expectedExceptions = ConfigurationException.class)
  public void failsToConstructWhenLabelsAreEmpty() {
    new GatewayConfigmapLabels("");
  }

  @Test(expectedExceptions = ConfigurationException.class)
  public void failsToConstructWhenLabelsAreNotValid() {
    new GatewayConfigmapLabels("badvalue");
  }

  @DataProvider
  public Object[][] isGatewayConfigData() {
    return new Object[][] {
      {
        "app=che,component=che-gateway-config",
        ImmutableMap.of("app", "che", "component", "che-gateway-config"),
        true
      },
      {
        "app=che, component=che-gateway-config",
        ImmutableMap.of("app", "che", "component", "che-gateway-config"),
        true
      },
      {"app=che", ImmutableMap.of("app", "che", "component", "che-gateway-config"), true},
      {
        "app=che,component=che-gateway-config",
        ImmutableMap.of("app", "cheche", "component", "che-gateway-config"),
        false
      },
      {"app=che,component=che-gateway-config", ImmutableMap.of("app", "cheche"), false},
      {
        "app=che,component=che-gateway-config",
        ImmutableMap.of("component", "che-gateway-config"),
        false
      },
    };
  }
}
