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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Resolves Kubernetes {@link Service}s needed for a proper accessibility of a Che workspace
 * sidecar.
 *
 * <p>Proper accessibility here means that sidecar endpoint should be discoverable by an endpoint
 * name inside of a workspace.
 *
 * @author Oleksandr Garagatyi
 */
public class SidecarServicesProvisioner {

  private final List<ChePluginEndpoint> endpoints;
  private final String podName;

  public SidecarServicesProvisioner(List<ChePluginEndpoint> containerEndpoints, String podName) {
    this.endpoints = containerEndpoints;
    this.podName = podName;
  }

  /**
   * Add k8s Service objects to environment to provide service discovery in sidecar based
   * workspaces.
   */
  public void provision(KubernetesEnvironment kubernetesEnvironment)
      throws InfrastructureException {
    for (ChePluginEndpoint endpoint : endpoints) {
      String serviceName = endpoint.getName();
      Service service = createService(serviceName, podName, endpoint.getTargetPort());

      Map<String, Service> services = kubernetesEnvironment.getServices();
      if (!services.containsKey(serviceName)) {
        services.put(serviceName, service);
      } else {
        throw new InfrastructureException(
            format(
                "Applying of sidecar tooling failed. Kubernetes service with name '%s' already exists in the workspace environment.",
                serviceName));
      }
    }
  }

  private Service createService(String name, String podName, int port) {
    ServicePort servicePort =
        new ServicePortBuilder().withPort(port).withProtocol("TCP").withNewTargetPort(port).build();
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withSelector(singletonMap(CHE_ORIGINAL_NAME_LABEL, podName))
        .withPorts(singletonList(servicePort))
        .endSpec()
        .build();
  }
}
