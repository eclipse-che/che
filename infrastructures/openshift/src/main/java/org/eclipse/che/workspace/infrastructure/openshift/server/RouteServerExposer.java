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
package org.eclipse.che.workspace.infrastructure.openshift.server;

import static java.util.Collections.emptyMap;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.Route;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Helps to modify {@link OpenShiftEnvironment} to make servers that are configured by {@link
 * ServerConfig} publicly or workspace-wide accessible.
 *
 * <p>To make server accessible it is needed to make sure that container port is declared, create
 * {@link Service}. To make it also publicly accessible it is needed to create corresponding {@link
 * Route} for exposing this port.
 *
 * <p>Created services and routes will have serialized servers which are exposed by the
 * corresponding object and machine name to which these servers belongs to.
 *
 * <p>Container, service and route are linked in the following way:
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
 * Then corresponding route expose one of the service's port:
 *
 * <pre>
 * Route
 * ...
 * spec:
 *   to:
 *     name: dev-machine              ---->> Service.metadata.name
 *   port:
 *     targetPort: [8080|web-app]     ---->> Service.spec.ports[0].[port|name]
 * </pre>
 *
 * <p>For accessing publicly accessible server user will use route host. For accessing
 * workspace-wide accessible server user will use service name. Information about servers that are
 * exposed by route and/or service are stored in annotations of a route or service.
 *
 * @author Sergii Leshchenko
 * @author Alexander Garagatyi
 * @see Annotations
 */
@Singleton
public class RouteServerExposer implements ExternalServerExposer<OpenShiftEnvironment> {

  private final Map<String, String> labels;
  private final String domainSuffix;

  @Inject
  public RouteServerExposer(
      @Nullable @Named("che.infra.openshift.route.labels") String labelsProperty,
      @Nullable @Named("che.infra.openshift.route.host.domain_suffix") String domainSuffix) {
    this.labels =
        labelsProperty != null
            ? Splitter.on(",").withKeyValueSeparator("=").split(labelsProperty)
            : emptyMap();
    this.domainSuffix = domainSuffix;
  }

  @Override
  public void expose(
      OpenShiftEnvironment env,
      String machineName,
      String serviceName,
      String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {
    Route commonRoute =
        new RouteBuilder()
            .withName(Names.generateName("route"))
            .withMachineName(machineName)
            .withTargetPort(servicePort.getName())
            .withServers(externalServers)
            .withLabels(labels)
            .withHost(
                domainSuffix != null
                    ? NameGenerator.generate("route", "." + domainSuffix, 10)
                    : null)
            .withTo(serviceName)
            .build();
    env.getRoutes().put(commonRoute.getMetadata().getName(), commonRoute);
  }

  private static class RouteBuilder {

    private String name;
    private String host;
    private String serviceName;
    private IntOrString targetPort;
    private Map<String, ServerConfig> servers;
    private Map<String, String> labels;
    private String machineName;

    private RouteBuilder withName(String name) {
      this.name = name;
      return this;
    }

    private RouteBuilder withTo(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    private RouteBuilder withHost(String host) {
      this.host = host;
      return this;
    }

    private RouteBuilder withTargetPort(String targetPortName) {
      this.targetPort = new IntOrString(targetPortName);
      return this;
    }

    private RouteBuilder withServer(String serverName, ServerConfig serverConfig) {
      return withServers(ImmutableMap.of(serverName, serverConfig));
    }

    private RouteBuilder withServers(Map<String, ServerConfig> servers) {
      if (this.servers == null) {
        this.servers = new HashMap<>();
      }

      this.servers.putAll(servers);

      return this;
    }

    private RouteBuilder withLabels(Map<String, String> labels) {
      this.labels = labels;
      return this;
    }

    public RouteBuilder withMachineName(String machineName) {
      this.machineName = machineName;
      return this;
    }

    private Route build() {
      io.fabric8.openshift.api.model.RouteBuilder builder =
          new io.fabric8.openshift.api.model.RouteBuilder();

      return builder
          .withNewMetadata()
          .withName(name.replace("/", "-"))
          .withAnnotations(
              Annotations.newSerializer().servers(servers).machineName(machineName).annotations())
          .withLabels(labels)
          .endMetadata()
          .withNewSpec()
          .withHost(host)
          .withNewTo()
          .withName(serviceName)
          .endTo()
          .withNewPort()
          .withTargetPort(targetPort)
          .endPort()
          .endSpec()
          .build();
    }
  }
}
