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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVER_NAME_ATTRIBUTE;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesServerExposer.class);

  public static final int SERVER_UNIQUE_PART_SIZE = 8;
  public static final String SERVER_PREFIX = "server";

  private final ExternalServerExposer<T> externalServerExposer;
  private final SecureServerExposer<T> secureServerExposer;
  private final String machineName;
  private final Container container;
  private final PodData pod;
  private final T k8sEnv;

  public KubernetesServerExposer(
      ExternalServerExposer<T> externalServerExposer,
      SecureServerExposer<T> secureServerExposer,
      String machineName,
      PodData pod,
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
   * A helper method to split the servers to unique sets that should be exposed together.
   *
   * <p>The consumer is responsible for doing the actual exposure and is supplied 2 pieces of data.
   * The first is the server ID, which is non-null for any unique server from the input set and null
   * for any compound set of servers that should be exposed together. The caller is responsible for
   * figuring out an appropriate ID in such case.
   *
   * @param allServers all unique and non-unique servers mixed together
   * @param consumer the consumer responsible for handling the split sets of servers
   */
  private static void onEachExposableServerSet(
      Map<String, ServerConfig> allServers, ServerSetExposer consumer)
      throws InfrastructureException {
    Map<String, ServerConfig> nonUniqueServers = new HashMap<>();

    for (Map.Entry<String, ServerConfig> e : allServers.entrySet()) {
      String serverId = makeServerNameValidForDns(e.getKey());
      if (e.getValue().isUnique()) {
        consumer.expose(serverId, ImmutableMap.of(serverId, e.getValue()));
      } else {
        nonUniqueServers.put(serverId, e.getValue());
      }
    }

    if (!nonUniqueServers.isEmpty()) {
      consumer.expose(null, nonUniqueServers);
    }
  }

  /** Replaces {@code /} with {@code -} in the provided name in an attempt to make it DNS safe. */
  public static String makeServerNameValidForDns(String name) {
    return name.replaceAll("/", "-");
  }

  /**
   * Exposes specified servers.
   *
   * <p>Note that created Kubernetes objects will select the corresponding pods by {@link
   * Constants#CHE_ORIGINAL_NAME_LABEL} label. That should be added by {@link
   * UniqueNamesProvisioner}.
   *
   * @param servers servers to expose
   * @see ConfigurationProvisioner#provision(KubernetesEnvironment, RuntimeIdentity)
   */
  public void expose(Map<String, ? extends ServerConfig> servers) throws InfrastructureException {
    Map<String, ServerConfig> internalServers = new HashMap<>();
    Map<String, ServerConfig> externalServers = new HashMap<>();
    Map<String, ServerConfig> secureServers = new HashMap<>();
    Map<String, ServicePort> unsecuredPorts = new HashMap<>();
    Map<String, ServicePort> securedPorts = new HashMap<>();

    splitServersAndPortsByExposureType(
        servers, internalServers, externalServers, secureServers, unsecuredPorts, securedPorts);

    provisionServicesForDiscoverableServers(servers);

    Optional<Service> serviceOpt = createService(internalServers, unsecuredPorts);

    if (serviceOpt.isPresent()) {
      Service service = serviceOpt.get();
      String serviceName = service.getMetadata().getName();
      k8sEnv.getServices().put(serviceName, service);
      exposeNonSecureServers(serviceName, externalServers, unsecuredPorts);
    }

    exposeSecureServers(secureServers, securedPorts);
  }

  // TODO: this creates discoverable services as an extra services. Service for same {@link
  //  ServerConfig} is also created later in in {@link #exposeNonSecureServers(Map, Map, Map)} or
  //  {@link #exposeSecureServers(Map, Map)} as a non-discoverable one. This was added during
  //  working on adding endpoints for kubernetes/openshift components, to keep behavior consistent.
  //  However, this logic is probably broken and should be changed.
  /**
   * Creates services with defined names for discoverable {@link ServerConfig}s. The name is taken
   * from {@link ServerConfig}'s attributes under {@link ServerConfig#SERVER_NAME_ATTRIBUTE} and
   * must be set, otherwise service won't be created.
   */
  private void provisionServicesForDiscoverableServers(
      Map<String, ? extends ServerConfig> servers) {
    for (String serverName : servers.keySet()) {
      ServerConfig server = servers.get(serverName);
      if (server.getAttributes().containsKey(SERVER_NAME_ATTRIBUTE)) {
        // remove the name from attributes so we don't send it to the client
        String endpointName = server.getAttributes().remove(SERVER_NAME_ATTRIBUTE);
        if (server.isDiscoverable()) {
          Service service =
              new ServerServiceBuilder()
                  .withName(endpointName)
                  .withMachineName(machineName)
                  .withSelectorEntry(CHE_ORIGINAL_NAME_LABEL, pod.getMetadata().getName())
                  .withPorts(Collections.singletonList(getServicePort(server)))
                  .withServers(Collections.singletonMap(serverName, server))
                  .build();
          k8sEnv.getServices().put(service.getMetadata().getName(), service);
        }
      }
    }
  }

  private void splitServersAndPortsByExposureType(
      Map<String, ? extends ServerConfig> servers,
      Map<String, ServerConfig> internalServers,
      Map<String, ServerConfig> externalServers,
      Map<String, ServerConfig> secureServers,
      Map<String, ServicePort> unsecuredPorts,
      Map<String, ServicePort> securedPorts) {

    servers.forEach(
        (key, value) -> {
          ServicePort sp = getServicePort(value);
          exposeInContainerIfNeeded(sp);
          if (value.isInternal()) {
            // Server is internal. It doesn't make sense to make an it secure since
            // it is available only within workspace servers
            internalServers.put(key, value);
            unsecuredPorts.put(value.getPort(), sp);
          } else {
            // Server is external. Check if it should be secure or not
            if (value.isSecure()) {
              secureServers.put(key, value);
              securedPorts.put(value.getPort(), sp);
            } else {
              externalServers.put(key, value);
              unsecuredPorts.put(value.getPort(), sp);
            }
          }
        });
  }

  private void exposeNonSecureServers(
      String serviceName,
      Map<String, ServerConfig> externalServers,
      Map<String, ServicePort> unsecuredPorts)
      throws InfrastructureException {

    for (ServicePort servicePort : unsecuredPorts.values()) {
      // expose service port related external servers if exist
      Map<String, ServerConfig> matchedExternalServers = match(externalServers, servicePort);
      if (!matchedExternalServers.isEmpty()) {
        onEachExposableServerSet(
            matchedExternalServers,
            (serverId, srvrs) ->
                externalServerExposer.expose(
                    k8sEnv, machineName, serviceName, serverId, servicePort, srvrs));
      }
    }
  }

  private Optional<Service> createService(
      Map<String, ServerConfig> internalServers, Map<String, ServicePort> unsecuredPorts) {
    Map<String, ServerConfig> allInternalServers = new HashMap<>(internalServers);
    if (unsecuredPorts.isEmpty()) {
      return Optional.empty();
    }

    Service service =
        new ServerServiceBuilder()
            .withName(generate(SERVER_PREFIX, SERVER_UNIQUE_PART_SIZE) + '-' + machineName)
            .withMachineName(machineName)
            .withSelectorEntry(CHE_ORIGINAL_NAME_LABEL, pod.getMetadata().getName())
            .withPorts(new ArrayList<>(unsecuredPorts.values()))
            .withServers(allInternalServers)
            .build();

    return Optional.of(service);
  }

  private void exposeSecureServers(
      Map<String, ServerConfig> securedServers, Map<String, ServicePort> securedPorts)
      throws InfrastructureException {

    if (securedPorts.isEmpty()) {
      return;
    }

    Optional<Service> secureService =
        secureServerExposer.createService(securedPorts.values(), pod, machineName, securedServers);

    String secureServiceName =
        secureService
            .map(
                s -> {
                  String n = s.getMetadata().getName();
                  k8sEnv.getServices().put(n, s);
                  return n;
                })
            .orElse(null);

    for (ServicePort servicePort : securedPorts.values()) {
      // expose service port related secure servers if exist
      Map<String, ServerConfig> matchedSecureServers = match(securedServers, servicePort);
      if (!matchedSecureServers.isEmpty()) {
        onEachExposableServerSet(
            matchedSecureServers,
            (serverId, srvrs) -> {
              secureServerExposer.expose(
                  k8sEnv, pod, machineName, secureServiceName, serverId, servicePort, srvrs);
            });
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

  private ServicePort getServicePort(ServerConfig serverConfig) {
    String[] portProtocol = serverConfig.getPort().split("/");
    int port = parseInt(portProtocol[0]);
    String protocol = portProtocol.length > 1 ? portProtocol[1].toUpperCase() : "TCP";
    return new ServicePortBuilder()
        .withName("server-" + port)
        .withPort(port)
        .withProtocol(protocol)
        .withNewTargetPort(port)
        .build();
  }

  private void exposeInContainerIfNeeded(ServicePort servicePort) {
    if (container
        .getPorts()
        .stream()
        .noneMatch(
            p ->
                p.getContainerPort().equals(servicePort.getPort())
                    && servicePort.getProtocol().equals(p.getProtocol()))) {
      ContainerPort containerPort =
          new ContainerPortBuilder()
              .withContainerPort(servicePort.getPort())
              .withProtocol(servicePort.getProtocol())
              .build();
      container.getPorts().add(containerPort);
    }
  }

  @FunctionalInterface
  private interface ServerSetExposer {
    void expose(String serverId, Map<String, ServerConfig> serverSet)
        throws InfrastructureException;
  }
}
