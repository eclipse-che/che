package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.workspace.server.devfile.Constants.DISCOVERABLE_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.DockerimageComponentToWorkspaceApplier.CHE_COMPONENT_NAME_LABEL;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;

@Singleton
public class ComponentEndpointExtractor {

  Map<String, ServerConfigImpl> extractServerConfigsFromComponentEndpoints(Component component) {
    return component
        .getEndpoints()
        .stream()
        .collect(Collectors.toMap(Endpoint::getName, this::toServerConfig));
  }

  List<Service> extractServicesFromComponentEndpoints(Component component) {
    return component
        .getEndpoints()
        .stream()
        .filter(e -> "true".equals(e.getAttributes().get(DISCOVERABLE_ENDPOINT_ATTRIBUTE)))
        .map(e -> createService(component.getAlias(), e))
        .collect(Collectors.toList());
  }

  private ServerConfigImpl toServerConfig(Endpoint endpoint) {
    HashMap<String, String> attributes = new HashMap<>(endpoint.getAttributes());

    String protocol = attributes.remove("protocol");
    if (isNullOrEmpty(protocol)) {
      protocol = "http";
    }

    String path = attributes.remove("path");

    String isPublic = attributes.remove(PUBLIC_ENDPOINT_ATTRIBUTE);
    if ("false".equals(isPublic)) {
      ServerConfig.setInternal(attributes, true);
    }

    return new ServerConfigImpl(Integer.toString(endpoint.getPort()), protocol, path, attributes);
  }

  private Service createService(String componentName, Endpoint endpoint) {
    ServicePort servicePort =
        new ServicePortBuilder()
            .withPort(endpoint.getPort())
            .withProtocol("TCP")
            .withNewTargetPort(endpoint.getPort())
            .build();
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(endpoint.getName())
        .endMetadata()
        .withNewSpec()
        .withSelector(ImmutableMap.of(CHE_COMPONENT_NAME_LABEL, componentName))
        .withPorts(singletonList(servicePort))
        .endSpec()
        .build();
  }
}
