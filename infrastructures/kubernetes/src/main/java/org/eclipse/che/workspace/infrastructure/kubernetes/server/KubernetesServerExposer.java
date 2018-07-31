/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.INTERNAL_SERVER_ATTRIBUTE;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposer;

/**
 * Helps to modify {@link KubernetesEnvironment} to make servers that are configured by {@link
 * ServerConfig} publicly or workspace-wide accessible.
 *
 * <p>To make server accessible it is needed to make sure that container port is declared, create
 * {@link Service}. To make it also publicly accessible it is needed to create corresponding {@link
 * Ingress} for exposing this port.
 *
 * <p>Created services and ingresses will have serialized servers which are exposed by the
 * corresponding object and machine name to which these servers belongs to.
 *
 * <p>Container, service and ingress are linked in the following way:
 *
 * <pre>
 * Pod
 * metadata:
 *   labels:
 *     type: web-app
 * spec:
 *   containers:
 *   ...
 *   - ports:
 *     - containerPort: 8080
 *       name: web-app
 *       protocol: TCP
 *   ...
 * </pre>
 *
 * Then services expose containers ports in the following way:
 *
 * <pre>
 * Service
 * metadata:
 *   name: service123
 * spec:
 *   selector:                        ---->> Pod.metadata.labels
 *     type: web-app
 *   ports:
 *     - name: web-app
 *       port: 8080
 *       targetPort: [8080|web-app]   ---->> Pod.spec.ports[0].[containerPort|name]
 *       protocol: TCP                ---->> Pod.spec.ports[0].protocol
 * </pre>
 *
 * Then, a server exposer strategy is used to expose one of the service's ports, to outside of the
 * cluster. Currently, Host-Based and Path-Based Ingresses can be used to expose service ports.
 *
 * <p>For accessing publicly accessible server user will use ingress host or its load balancer IP.
 * For accessing workspace-wide accessible server user will use service name. Information about
 * servers that are exposed by ingress and/or service are stored in annotations of a ingress or
 * service.
 *
 * @author Sergii Leshchenko
 * @author Alexander Garagatyi
 * @see Annotations
 */
public class KubernetesServerExposer<T extends KubernetesEnvironment> {

  public static final int SERVER_UNIQUE_PART_SIZE = 8;
  public static final String SERVER_PREFIX = "server";

  private final ExternalServerExposerStrategy<T> externalServerExposer;
  private final SecureServerExposer<T> secureServerExposer;
  private final String machineName;
  private final Container container;
  private final Pod pod;
  private final T k8sEnv;

  public KubernetesServerExposer(
      ExternalServerExposerStrategy<T> externalServerExposer,
      SecureServerExposer<T> secureServerExposer,
      String machineName,
      Pod pod,
      Container container,
      T k8sEnv) {
    this.externalServerExposer = externalServerExposer;
    this.secureServerExposer = secureServerExposer;
    this.machineName = machineName;
    this.pod = pod;
    this.container = container;
    this.k8sEnv = k8sEnv;
  }

  /**
   * Exposes specified servers.
   *
   * <p>Note that created Kubernetes objects will select the corresponding pods by {@link
   * Constants#CHE_ORIGINAL_NAME_LABEL} label. That should be added by {@link
   * UniqueNamesProvisioner}.
   *
   * @param servers servers to expose
   * @see UniqueNamesProvisioner#provision(KubernetesEnvironment, RuntimeIdentity)
   */
  public void expose(Map<String, ? extends ServerConfig> servers) throws InfrastructureException {
    Map<String, ServerConfig> internalServers = new HashMap<>();
    Map<String, ServerConfig> externalServers = new HashMap<>();
    Map<String, ServerConfig> secureServers = new HashMap<>();

    servers.forEach(
        (key, value) -> {
          if ("true".equals(value.getAttributes().get(INTERNAL_SERVER_ATTRIBUTE))) {
            // Server is internal. It doesn't make sense to make an it secure since
            // it is available only within workspace servers
            internalServers.put(key, value);
          } else {
            // Server is external. Check if it should be secure or not
            if ("true".equals(value.getAttributes().get(ServerConfig.SECURE_SERVER_ATTRIBUTE))) {
              secureServers.put(key, value);
            } else {
              externalServers.put(key, value);
            }
          }
        });

    Collection<ServicePort> servicePorts = exposePorts(servers.values());
    Service service =
        new ServerServiceBuilder()
            .withName(generate(SERVER_PREFIX, SERVER_UNIQUE_PART_SIZE) + '-' + machineName)
            .withMachineName(machineName)
            .withSelectorEntry(CHE_ORIGINAL_NAME_LABEL, pod.getMetadata().getName())
            .withPorts(new ArrayList<>(servicePorts))
            .withServers(internalServers)
            .build();

    String serviceName = service.getMetadata().getName();
    k8sEnv.getServices().put(serviceName, service);

    for (ServicePort servicePort : servicePorts) {
      // expose service port related external servers if exist
      Map<String, ServerConfig> matchedExternalServers = match(externalServers, servicePort);
      if (!matchedExternalServers.isEmpty()) {
        externalServerExposer.expose(
            k8sEnv, machineName, serviceName, servicePort, matchedExternalServers);
      }

      // expose service port related secure servers if exist
      Map<String, ServerConfig> matchedSecureServers = match(secureServers, servicePort);
      if (!matchedSecureServers.isEmpty()) {
        secureServerExposer.expose(
            k8sEnv, machineName, serviceName, servicePort, matchedSecureServers);
      }
    }
  }

  private Map<String, ServerConfig> match(
      Map<String, ServerConfig> servers, ServicePort servicePort) {
    int port = servicePort.getTargetPort().getIntVal();
    return servers
        .entrySet()
        .stream()
        .filter(e -> parseInt(e.getValue().getPort().split("/")[0]) == port)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Collection<ServicePort> exposePorts(Collection<? extends ServerConfig> serverConfig) {
    Map<String, ServicePort> exposedPorts = new HashMap<>();
    Set<String> portsToExpose =
        serverConfig.stream().map(ServerConfig::getPort).collect(Collectors.toSet());

    for (String portToExpose : portsToExpose) {
      String[] portProtocol = portToExpose.split("/");
      int port = parseInt(portProtocol[0]);
      String protocol = portProtocol.length > 1 ? portProtocol[1].toUpperCase() : "TCP";
      Optional<ContainerPort> exposedOpt =
          container
              .getPorts()
              .stream()
              .filter(p -> p.getContainerPort().equals(port) && protocol.equals(p.getProtocol()))
              .findAny();

      ContainerPort containerPort;
      if (exposedOpt.isPresent()) {
        containerPort = exposedOpt.get();
      } else {
        containerPort =
            new ContainerPortBuilder().withContainerPort(port).withProtocol(protocol).build();
        container.getPorts().add(containerPort);
      }

      exposedPorts.put(
          portToExpose,
          new ServicePortBuilder()
              .withName("server-" + containerPort.getContainerPort())
              .withPort(containerPort.getContainerPort())
              .withProtocol(protocol)
              .withNewTargetPort(containerPort.getContainerPort())
              .build());
    }
    return exposedPorts.values();
  }
}
