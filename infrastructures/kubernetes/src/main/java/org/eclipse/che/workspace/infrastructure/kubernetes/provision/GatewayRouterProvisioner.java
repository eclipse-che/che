package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGeneratorFactory;

public class GatewayRouterProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  protected static final Map<String, String> GATEWAY_CONFIGMAP_LABELS =
      ImmutableMap.<String, String>builder()
          .put("app", "che")
          .put("role", "gateway-config")
          .build();

  private final GatewayRouteConfigGeneratorFactory configGeneratorFactory;

  @Inject
  public GatewayRouterProvisioner(GatewayRouteConfigGeneratorFactory configGeneratorFactory) {
    this.configGeneratorFactory = configGeneratorFactory;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (k8sEnv.getGatewayRouteConfigs().isEmpty()) {
      return;
    }

    for (GatewayRouteConfig routeConfig : k8sEnv.getGatewayRouteConfigs()) {
      GatewayRouteConfigGenerator gatewayRouteConfigGenerator =
          configGeneratorFactory.create(identity.getInfrastructureNamespace());
      gatewayRouteConfigGenerator.addRouteConfig(routeConfig);

      ConfigMapBuilder configMapBuilder =
          new ConfigMapBuilder()
              .withNewMetadata()
              .withName(identity.getWorkspaceId() + "-" + routeConfig.getName())
              .withLabels(GATEWAY_CONFIGMAP_LABELS)
              .withAnnotations(routeConfig.getAnnotations())
              .endMetadata()
              .withData(
                  gatewayRouteConfigGenerator.generate(identity.getInfrastructureNamespace()));

      k8sEnv.getConfigMaps().put(routeConfig.getName(), configMapBuilder.build());
    }
  }
}
