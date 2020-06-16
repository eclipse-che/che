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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret;

import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.DEVFILE_COMPONENT_ALIAS_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.ANNOTATION_PREFIX;

import io.fabric8.kubernetes.api.model.Secret;
import java.util.Optional;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

public abstract class KubernetesSecretApplier<E extends KubernetesEnvironment> {

  static final String ANNOTATION_AUTOMOUNT = ANNOTATION_PREFIX + "/" + "automount-workspace-secret";

  public abstract void applySecret(E env, Secret secret) throws InfrastructureException;

  Optional<ComponentImpl> getComponent(E env, String name) {
    InternalMachineConfig internalMachineConfig = env.getMachines().get(name);
    if (internalMachineConfig != null) {
      String componentName =
          internalMachineConfig.getAttributes().get(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE);
      if (componentName != null) {
        return env.getDevfile()
            .getComponents()
            .stream()
            .filter(c -> c.getAlias().equals(componentName))
            .findFirst();
      }
    }
    return Optional.empty();
  }

  boolean isOverridenByFalse(ComponentImpl component) {
    return component.getAutomountWorkspaceSecrets() != null
        && !component.getAutomountWorkspaceSecrets();
  }

  boolean isOverridenByTrue(ComponentImpl component) {
    return component.getAutomountWorkspaceSecrets() != null
        && component.getAutomountWorkspaceSecrets();
  }
}
