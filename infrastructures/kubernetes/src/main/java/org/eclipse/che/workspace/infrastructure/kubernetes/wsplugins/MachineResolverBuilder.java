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
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;

/** @author Alexander Garagatyi */
public class MachineResolverBuilder {

  private Container container;
  private CheContainer cheContainer;
  private String defaultSidecarMemorySizeAttribute;
  private List<ChePluginEndpoint> containerEndpoints;

  public MachineResolver build() {
    if (container == null
        || cheContainer == null
        || defaultSidecarMemorySizeAttribute == null
        || containerEndpoints == null) {
      throw new IllegalStateException();
    }

    return new MachineResolver(
        container, cheContainer, defaultSidecarMemorySizeAttribute, containerEndpoints);
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
}
