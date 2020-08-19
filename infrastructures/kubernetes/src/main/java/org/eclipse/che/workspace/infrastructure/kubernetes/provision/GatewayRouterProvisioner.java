package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_PORT_ATTRIBUTE;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGeneratorFactory;

public class GatewayRouterProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  public static final Map<String, String> GATEWAY_CONFIGMAP_LABELS =
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
    for (Entry<String, ConfigMap> configMapEntry : k8sEnv.getConfigMaps().entrySet()) {
      if (isGatewayConfig(configMapEntry.getValue())) {

        GatewayRouteConfigGenerator gatewayRouteConfigGenerator =
            configGeneratorFactory.create(identity.getInfrastructureNamespace());
        gatewayRouteConfigGenerator
            .addRouteConfig(configMapEntry.getKey(), configMapEntry.getValue());

        Map<String, ServerConfigImpl> servers = new Annotations.Deserializer(
            configMapEntry.getValue().getMetadata().getAnnotations()).servers();
        if (servers.size() != 1) {
          throw new InfrastructureException("expected 1");
        }
        String scKey = servers.keySet().stream().findFirst().get();
        ServerConfigImpl server = servers.get(scKey);

        if (!server.getAttributes().containsKey(SERVICE_NAME_ATTRIBUTE) ||
            !server.getAttributes().containsKey(SERVICE_PORT_ATTRIBUTE)) {
          throw new InfrastructureException("Need serviceName and servicePort");
        }

        final String serviceName = server.getAttributes().get(SERVICE_NAME_ATTRIBUTE);
        final String servicePort = server.getAttributes().get(SERVICE_PORT_ATTRIBUTE);

        configMapEntry.getValue()
            .setData(gatewayRouteConfigGenerator.generate(serviceName, servicePort,
                identity.getInfrastructureNamespace()));
      }
    }
  }

  public static boolean isGatewayConfig(ConfigMap configMap) {
    Map<String, String> labels = configMap.getMetadata().getLabels();
    for (Entry<String, String> labelEntry : GATEWAY_CONFIGMAP_LABELS.entrySet()) {
      if (labels == null || !labels.containsKey(labelEntry.getKey()) || !labels
          .get(labelEntry.getKey())
          .equals(labelEntry.getValue())) {
        return false;
      }
    }
    return true;
  }
}
