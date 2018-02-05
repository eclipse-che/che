/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValueBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressBackendBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import io.fabric8.kubernetes.api.model.extensions.IngressSpecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner;

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
 * Then corresponding ingress expose one of the service's port:
 *
 * <pre>
 * Ingress
 * ...
 * spec:
 *   rules:
 *     - http:
 *         paths:
 *           - path: service123/webapp        ---->> Service.metadata.name + / + Service.spec.ports[0].name
 *             backend:
 *               serviceName: service123      ---->> Service.metadata.name
 *               servicePort: [8080|web-app]  ---->> Service.spec.ports[0].[port|name]
 * </pre>
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

  private final Map<String, String> ingressAnnotations;
  protected final String machineName;
  protected final Container container;
  protected final Pod pod;
  protected final T kubernetesEnvironment;

  public KubernetesServerExposer(
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> ingressAnnotations,
      String machineName,
      Pod pod,
      Container container,
      T kubernetesEnvironment) {
    this.ingressAnnotations = ingressAnnotations;
    this.machineName = machineName;
    this.pod = pod;
    this.container = container;
    this.kubernetesEnvironment = kubernetesEnvironment;
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
  public void expose(Map<String, ? extends ServerConfig> servers) {
    Map<String, ServerConfig> internalServers = new HashMap<>();
    Map<String, ServerConfig> externalServers = new HashMap<>();

    servers.forEach(
        (key, value) -> {
          if ("true".equals(value.getAttributes().get(INTERNAL_SERVER_ATTRIBUTE))) {
            internalServers.put(key, value);
          } else {
            externalServers.put(key, value);
          }
        });

    Map<String, ServicePort> portToServicePort = exposePort(servers.values());
    Service service =
        new ServiceBuilder()
            .withName(generate(SERVER_PREFIX, SERVER_UNIQUE_PART_SIZE) + '-' + machineName)
            .withMachineName(machineName)
            .withSelectorEntry(CHE_ORIGINAL_NAME_LABEL, pod.getMetadata().getName())
            .withPorts(new ArrayList<>(portToServicePort.values()))
            .withServers(internalServers)
            .build();

    String serviceName = service.getMetadata().getName();
    kubernetesEnvironment.getServices().put(serviceName, service);

    exposeExternalServers(serviceName, portToServicePort, externalServers);
  }

  protected void exposeExternalServers(
      String serviceName,
      Map<String, ServicePort> portToServicePort,
      Map<String, ServerConfig> externalServers) {
    for (ServicePort servicePort : portToServicePort.values()) {
      int port = servicePort.getTargetPort().getIntVal();

      Map<String, ServerConfig> ingressesServers =
          externalServers
              .entrySet()
              .stream()
              .filter(e -> parseInt(e.getValue().getPort().split("/")[0]) == port)
              .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

      Ingress ingress =
          new IngressBuilder()
              .withName(serviceName + '-' + servicePort.getName())
              .withMachineName(machineName)
              .withServiceName(serviceName)
              .withAnnotations(ingressAnnotations)
              .withServicePort(servicePort.getName())
              .withServers(ingressesServers)
              .build();

      kubernetesEnvironment.getIngresses().put(ingress.getMetadata().getName(), ingress);
    }
  }

  private Map<String, ServicePort> exposePort(Collection<? extends ServerConfig> serverConfig) {
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
    return exposedPorts;
  }

  private static class ServiceBuilder {
    private String name;
    private String machineName;
    private Map<String, String> selector = new HashMap<>();
    private List<ServicePort> ports = Collections.emptyList();
    private Map<String, ? extends ServerConfig> serversConfigs = Collections.emptyMap();

    private ServiceBuilder withName(String name) {
      this.name = name;
      return this;
    }

    private ServiceBuilder withSelectorEntry(String key, String value) {
      selector.put(key, value);
      return this;
    }

    private ServiceBuilder withPorts(List<ServicePort> ports) {
      this.ports = ports;
      return this;
    }

    private ServiceBuilder withServers(Map<String, ? extends ServerConfig> serversConfigs) {
      this.serversConfigs = serversConfigs;
      return this;
    }

    private Service build() {
      io.fabric8.kubernetes.api.model.ServiceBuilder builder =
          new io.fabric8.kubernetes.api.model.ServiceBuilder();
      return builder
          .withNewMetadata()
          .withName(name.replace("/", "-"))
          .withAnnotations(
              Annotations.newSerializer()
                  .servers(serversConfigs)
                  .machineName(machineName)
                  .annotations())
          .endMetadata()
          .withNewSpec()
          .withSelector(selector)
          .withPorts(ports)
          .endSpec()
          .build();
    }

    public ServiceBuilder withMachineName(String machineName) {
      this.machineName = machineName;
      return this;
    }
  }

  private static class IngressBuilder {
    private String name;
    private String serviceName;
    private IntOrString servicePort;
    private Map<String, ? extends ServerConfig> serversConfigs;
    private String machineName;
    private Map<String, String> annotations;

    private IngressBuilder withName(String name) {
      this.name = name;
      return this;
    }

    private IngressBuilder withServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    private IngressBuilder withAnnotations(Map<String, String> annotations) {
      this.annotations = annotations;
      return this;
    }

    private IngressBuilder withServicePort(String targetPortName) {
      this.servicePort = new IntOrString(targetPortName);
      return this;
    }

    private IngressBuilder withServers(Map<String, ? extends ServerConfig> serversConfigs) {
      this.serversConfigs = serversConfigs;
      return this;
    }

    public IngressBuilder withMachineName(String machineName) {
      this.machineName = machineName;
      return this;
    }

    private Ingress build() {

      IngressBackend ingressBackend =
          new IngressBackendBuilder()
              .withServiceName(serviceName)
              .withNewServicePort(servicePort.getStrVal())
              .build();

      String serverPath = "/" + serviceName + "/" + servicePort.getStrVal();
      HTTPIngressPath httpIngressPath =
          new HTTPIngressPathBuilder().withPath(serverPath).withBackend(ingressBackend).build();

      HTTPIngressRuleValue httpIngressRuleValue =
          new HTTPIngressRuleValueBuilder().withPaths(httpIngressPath).build();
      IngressRule ingressRule = new IngressRuleBuilder().withHttp(httpIngressRuleValue).build();
      IngressSpec ingressSpec = new IngressSpecBuilder().withRules(ingressRule).build();

      Map<String, String> ingressAnnotations = new HashMap<>(annotations);
      ingressAnnotations.putAll(
          Annotations.newSerializer()
              .servers(serversConfigs)
              .machineName(machineName)
              .annotations());

      return new io.fabric8.kubernetes.api.model.extensions.IngressBuilder()
          .withSpec(ingressSpec)
          .withMetadata(
              new ObjectMetaBuilder().withName(name).withAnnotations(ingressAnnotations).build())
          .build();
    }
  }
}
