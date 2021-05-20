/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainerPort;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;

/** @author Oleksandr Garagatyi */
public class K8sContainerResolverBuilder {

  private String imagePullPolicy;
  private CheContainer container;
  private List<ChePluginEndpoint> pluginEndpoints;

  public K8sContainerResolverBuilder setContainer(CheContainer container) {
    this.container = container;
    return this;
  }

  public K8sContainerResolverBuilder setPluginEndpoints(List<ChePluginEndpoint> pluginEndpoints) {
    this.pluginEndpoints = pluginEndpoints;
    return this;
  }

  public K8sContainerResolverBuilder setImagePullPolicy(String imagePullPolicy) {
    this.imagePullPolicy = imagePullPolicy;
    return this;
  }

  public K8sContainerResolver build() {
    if (container == null || pluginEndpoints == null) {
      throw new IllegalStateException();
    }
    List<ChePluginEndpoint> containerEndpoints =
        getContainerEndpoints(container.getPorts(), pluginEndpoints);
    return new K8sContainerResolver(imagePullPolicy, container, containerEndpoints);
  }

  private List<ChePluginEndpoint> getContainerEndpoints(
      List<CheContainerPort> ports, List<ChePluginEndpoint> endpoints) {

    if (ports == null || ports.isEmpty()) {
      return Collections.emptyList();
    }
    return ports
        .stream()
        .map(CheContainerPort::getExposedPort)
        .flatMap(port -> endpoints.stream().filter(e -> e.getTargetPort() == port))
        .collect(Collectors.toList());
  }
}
