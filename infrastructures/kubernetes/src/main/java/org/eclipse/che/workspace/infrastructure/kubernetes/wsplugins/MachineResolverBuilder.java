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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import io.fabric8.kubernetes.api.model.Container;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.commons.lang.Pair;

/** @author Alexander Garagatyi */
public class MachineResolverBuilder {

  private Container container;
  private CheContainer cheContainer;
  private String defaultSidecarMemoryLimitAttribute;
  private String defaultSidecarMemoryRequestAttribute;
  private String defaultSidecarCpuLimitAttribute;
  private String defaultSidecarCpuRequestAttribute;
  private List<ChePluginEndpoint> containerEndpoints;
  private Pair<String, String> projectsRootPathEnvVar;
  private Component component;

  public MachineResolver build() {
    if (container == null
        || cheContainer == null
        || defaultSidecarMemoryLimitAttribute == null
        || defaultSidecarMemoryRequestAttribute == null
        || defaultSidecarCpuLimitAttribute == null
        || defaultSidecarCpuRequestAttribute == null
        || containerEndpoints == null
        || projectsRootPathEnvVar == null) {
      throw new IllegalStateException(
          "Unable to build MachineResolver because some fields are null");
    }

    return new MachineResolver(
        projectsRootPathEnvVar,
        container,
        cheContainer,
        defaultSidecarMemoryLimitAttribute,
        defaultSidecarMemoryRequestAttribute,
        defaultSidecarCpuLimitAttribute,
        defaultSidecarCpuRequestAttribute,
        containerEndpoints,
        component);
  }

  public MachineResolverBuilder setContainer(Container container) {
    this.container = container;
    return this;
  }

  public MachineResolverBuilder setCheContainer(CheContainer cheContainer) {
    this.cheContainer = cheContainer;
    return this;
  }

  public MachineResolverBuilder setDefaultSidecarMemoryLimitAttribute(
      String defaultSidecarMemoryLimitAttribute) {
    this.defaultSidecarMemoryLimitAttribute = defaultSidecarMemoryLimitAttribute;
    return this;
  }

  public MachineResolverBuilder setDefaultSidecarMemoryRequestAttribute(
      String defaultSidecarMemoryRequestAttribute) {
    this.defaultSidecarMemoryRequestAttribute = defaultSidecarMemoryRequestAttribute;
    return this;
  }

  public MachineResolverBuilder setDefaultSidecarCpuLimitAttribute(
      String defaultSidecarCpuLimitAttribute) {
    this.defaultSidecarCpuLimitAttribute = defaultSidecarCpuLimitAttribute;
    return this;
  }

  public MachineResolverBuilder setDefaultSidecarCpuRequestAttribute(
      String defaultSidecarCpuRequestAttribute) {
    this.defaultSidecarCpuRequestAttribute = defaultSidecarCpuRequestAttribute;
    return this;
  }

  public MachineResolverBuilder setContainerEndpoints(List<ChePluginEndpoint> containerEndpoints) {
    this.containerEndpoints = containerEndpoints;
    return this;
  }

  public MachineResolverBuilder setProjectsRootPathEnvVar(
      Pair<String, String> projectsRootPathEnvVar) {
    this.projectsRootPathEnvVar = projectsRootPathEnvVar;
    return this;
  }

  public MachineResolverBuilder setComponent(Component component) {
    this.component = component;
    return this;
  }
}
