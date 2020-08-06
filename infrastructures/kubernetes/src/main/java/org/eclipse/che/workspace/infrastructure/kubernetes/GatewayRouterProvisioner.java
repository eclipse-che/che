package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisioner.getSecureProtocol;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.TraefikGatewayRouteConfigGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;

@Singleton
public class GatewayRouterProvisioner {

  protected static final Map<String, String> GATEWAY_CONFIGMAP_LABELS =
      ImmutableMap.<String, String>builder()
          .put("app", "che")
          .put("role", "gateway-config")
          .build();

  private final KubernetesClientFactory clientFactory;
  private final Executor k8sExecutor;

  @Inject
  public GatewayRouterProvisioner(KubernetesClientFactory clientFactory,
      KubernetesSharedPool sharedPool) {
    this.clientFactory = clientFactory;
    this.k8sExecutor = sharedPool.getExecutor();
  }

  public List<ConfigMap> provision(RuntimeIdentity id, InternalEnvironment internalEnvironment)
      throws InfrastructureException {
    if (internalEnvironment.getGatewayRouteConfigs().isEmpty()) {
      return Collections.emptyList();
    }
    List<ConfigMap> routeConfigMaps = new ArrayList<>();
    KubernetesNamespace cheNamespace = new KubernetesNamespace(clientFactory, k8sExecutor, "che",
        id.getWorkspaceId());

    for (GatewayRouteConfig routeConfig : internalEnvironment.getGatewayRouteConfigs()) {
      GatewayRouteConfigGenerator gatewayRouteConfigGenerator = new TraefikGatewayRouteConfigGenerator(
          id.getInfrastructureNamespace());
      gatewayRouteConfigGenerator.addRouteConfig(routeConfig);
      ConfigMapBuilder configMapBuilder = new ConfigMapBuilder()
          .withNewMetadata()
          .withName(id.getWorkspaceId() + routeConfig.getName())
          .withLabels(GATEWAY_CONFIGMAP_LABELS)
          .withAnnotations(routeConfig.getAnnotations())
          .endMetadata()
          .withData(gatewayRouteConfigGenerator.generate());

      ConfigMap routeConfigMap = cheNamespace.configMaps().create(configMapBuilder.build());
      routeConfigMaps.add(routeConfigMap);
    }

    return routeConfigMaps;
  }
}
