package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.GatewayRouterProvisioner.GATEWAY_CONFIGMAP_LABELS;
import static org.testng.Assert.*;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.Collections;
import java.util.Map;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GatewayRouterProvisionerTest {
  @Test(dataProvider = "isGatewayConfigData")
  public void testIsGatewayConfig(Map<String, String> labels, boolean isGatewayConfigExpected) {
    ConfigMap cm = new ConfigMapBuilder().withNewMetadata().withLabels(labels).endMetadata().build();
    assertEquals(GatewayRouterProvisioner.isGatewayConfig(cm), isGatewayConfigExpected);
  }

  @DataProvider
  public Object[][] isGatewayConfigData() {
    return new Object[][] {
        {GATEWAY_CONFIGMAP_LABELS, true},
        {ImmutableMap.builder().putAll(GATEWAY_CONFIGMAP_LABELS).put("other", "value").build(), true},
        {Collections.emptyMap(), false},
        {ImmutableMap.of("one", "two"), false},
        {ImmutableMap.of(), false},
        {ImmutableMap.of("app", "yes", "role", "no"), false},
        {ImmutableMap.of("app", GATEWAY_CONFIGMAP_LABELS.get("app"), "role", "no"), false},
        {ImmutableMap.of("app", "no", "role", GATEWAY_CONFIGMAP_LABELS.get("role")), false},
    };
  }
}
