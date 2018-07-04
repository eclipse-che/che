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
package org.eclipse.che.workspace.infrastructure.openshift.server;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.Route;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerStrategy;
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
public class OpenShiftExternalServerExposer
    implements ExternalServerExposerStrategy<OpenShiftEnvironment> {

  @Override
  public void expose(
      OpenShiftEnvironment openShiftEnvironment,
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {
    Route route =
        new RouteBuilder()
            .withName(serviceName + '-' + servicePort.getName())
            .withMachineName(machineName)
            .withTargetPort(servicePort.getName())
            .withServers(externalServers)
            .withTo(serviceName)
            .build();
    openShiftEnvironment.getRoutes().put(route.getMetadata().getName(), route);
  }

  private static class RouteBuilder {

    private String name;
    private String serviceName;
    private IntOrString targetPort;
    private Map<String, ? extends ServerConfig> serversConfigs;
    private String machineName;

    private RouteBuilder withName(String name) {
      this.name = name;
      return this;
    }

    private RouteBuilder withTo(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    private RouteBuilder withTargetPort(String targetPortName) {
      this.targetPort = new IntOrString(targetPortName);
      return this;
    }

    private RouteBuilder withServers(Map<String, ? extends ServerConfig> serversConfigs) {
      this.serversConfigs = serversConfigs;
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
              Annotations.newSerializer()
                  .servers(serversConfigs)
                  .machineName(machineName)
                  .annotations())
          .endMetadata()
          .withNewSpec()
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
