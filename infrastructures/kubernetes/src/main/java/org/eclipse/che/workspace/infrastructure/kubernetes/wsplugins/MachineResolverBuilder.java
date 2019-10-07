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

import io.fabric8.kubernetes.api.model.Container;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.commons.lang.Pair;

/** @author Alexander Garagatyi */
public class MachineResolverBuilder {

  private Container container;
  private CheContainer cheContainer;
  private String defaultSidecarMemorySizeAttribute;
  private List<ChePluginEndpoint> containerEndpoints;
  private Map<String, String> wsAttributes;
  private Pair<String, String> projectsRootPathEnvVar;
  private String pluginPublisher;
  private String pluginName;
  private String pluginId;

  public MachineResolver build() {
    if (container == null
        || cheContainer == null
        || defaultSidecarMemorySizeAttribute == null
        || wsAttributes == null
        || containerEndpoints == null
        || projectsRootPathEnvVar == null
        || pluginPublisher == null
        || pluginName == null
        || pluginId == null) {
      throw new IllegalStateException();
    }

    return new MachineResolver(
        pluginPublisher,
        pluginName,
        pluginId,
        projectsRootPathEnvVar,
        container,
        cheContainer,
        defaultSidecarMemorySizeAttribute,
        containerEndpoints,
        wsAttributes);
  }

  public MachineResolverBuilder setContainer(Container container) {
    this.container = container;
    return this;
  }

  public MachineResolverBuilder setCheContainer(CheContainer cheContainer) {
    this.cheContainer = cheContainer;
    return this;
  }

  public MachineResolverBuilder setDefaultSidecarMemorySizeAttribute(
      String defaultSidecarMemorySizeAttribute) {
    this.defaultSidecarMemorySizeAttribute = defaultSidecarMemorySizeAttribute;
    return this;
  }

  public MachineResolverBuilder setContainerEndpoints(List<ChePluginEndpoint> containerEndpoints) {
    this.containerEndpoints = containerEndpoints;
    return this;
  }

  public MachineResolverBuilder setAttributes(Map<String, String> wsConfigAttributes) {
    this.wsAttributes = wsConfigAttributes;
    return this;
  }

  public MachineResolverBuilder setProjectsRootPathEnvVar(
      Pair<String, String> projectsRootPathEnvVar) {
    this.projectsRootPathEnvVar = projectsRootPathEnvVar;
    return this;
  }

  public MachineResolverBuilder setPluginPublisher(String pluginPublisher) {
    this.pluginPublisher = pluginPublisher;
    return this;
  }

  public MachineResolverBuilder setPluginName(String pluginName) {
    this.pluginName = pluginName;
    return this;
  }

  public MachineResolverBuilder setPluginId(String pluginId) {
    this.pluginId = pluginId;
    return this;
  }
}
