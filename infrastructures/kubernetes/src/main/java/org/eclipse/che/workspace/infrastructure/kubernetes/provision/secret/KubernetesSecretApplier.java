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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret;

import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.DEVFILE_COMPONENT_ALIAS_ATTRIBUTE;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.Secret;
import java.util.Optional;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Base class for secret appliers. Contains common functionality to find devfile components by name
 * and check override automount properties.
 */
@Beta
public abstract class KubernetesSecretApplier<E extends KubernetesEnvironment> {

  /**
   * Applies particular secret to workspace containers.
   *
   * @param env environment to retrieve components from
   * @param runtimeIdentity identity of current runtime
   * @param secret secret to apply
   * @throws InfrastructureException when secret applying error
   */
  public abstract void applySecret(E env, RuntimeIdentity runtimeIdentity, Secret secret)
      throws InfrastructureException;

  /**
   * Tries to retrieve devfile component by given container name.
   *
   * @param env kubernetes environment of the workspace
   * @param containerName name of container to find it's parent component
   * @return matched component
   */
  final Optional<ComponentImpl> getComponent(E env, String containerName) {
    InternalMachineConfig internalMachineConfig = env.getMachines().get(containerName);
    if (internalMachineConfig != null) {
      String componentName =
          internalMachineConfig.getAttributes().get(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE);
      if (componentName != null) {
        return env.getDevfile()
            .getComponents()
            .stream()
            .filter(c -> componentName.equals(c.getAlias()))
            .findFirst();
      }
    }
    return Optional.empty();
  }

  /**
   * @param component source component
   * @return {@code true} when {@code automountWorkspaceSecret} property explicitly set to {@code
   *     false},or {@code false} otherwise.
   */
  final boolean isComponentAutomountFalse(ComponentImpl component) {
    return component.getAutomountWorkspaceSecrets() != null
        && !component.getAutomountWorkspaceSecrets();
  }

  /**
   * @param component source component
   * @return {@code true} when {@code automountWorkspaceSecret} property explicitly set to {@code
   *     true},or {@code false} otherwise.
   */
  final boolean isComponentAutomountTrue(ComponentImpl component) {
    return component.getAutomountWorkspaceSecrets() != null
        && component.getAutomountWorkspaceSecrets();
  }
}
