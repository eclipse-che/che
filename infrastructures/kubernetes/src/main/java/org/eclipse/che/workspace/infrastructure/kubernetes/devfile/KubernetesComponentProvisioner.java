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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

import com.google.inject.name.Named;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentProvisioner;
import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;

/**
 * Provision kubernetes/openshift component in {@link DevfileImpl} according to the value of
 * environment with kubernetes/openshfit recipe if the specified {@link WorkspaceConfigImpl} has
 * such.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesComponentProvisioner implements ComponentProvisioner {

  private final Set<String> handledEnvironmentTypes;

  @Inject
  public KubernetesComponentProvisioner(
      @Named(KubernetesDevfileBindings.KUBERNETES_BASED_ENVIRONMENTS_KEY_NAME)
          Set<String> handledEnvironmentTypes) {
    this.handledEnvironmentTypes = handledEnvironmentTypes;
  }

  /**
   * Provision kubernetes/openshift component in {@link DevfileImpl} according to the value of
   * environment with kubernetes/openshift recipe if the specified {@link WorkspaceConfigImpl} has
   * such.
   *
   * @param devfile devfile to which created kubernetes/openshfit component should be injected
   * @param workspaceConfig workspace config that may contain environment with kubernetes/openshift
   *     recipe to convert
   * @throws IllegalArgumentException if the specified workspace config or devfile is null
   * @throws WorkspaceExportException if workspace config has more than one kubernetes/openshift
   *     environments
   * @throws WorkspaceExportException if workspace config has one kubernetes/openshift environments.
   *     Exporting of such workspaces will be implemented soon
   */
  @Override
  public void provision(DevfileImpl devfile, WorkspaceConfigImpl workspaceConfig)
      throws WorkspaceExportException {
    checkArgument(devfile != null, "The environment must not be null");
    checkArgument(workspaceConfig != null, "The workspace config must not be null");

    List<Entry<String, EnvironmentImpl>> k8sEnvironments =
        workspaceConfig
            .getEnvironments()
            .entrySet()
            .stream()
            .filter(e -> handledEnvironmentTypes.contains(e.getValue().getRecipe().getType()))
            .collect(Collectors.toList());

    if (k8sEnvironments.isEmpty()) {
      return;
    }

    if (k8sEnvironments.size() > 1) {
      String allEnvs = String.join("/", handledEnvironmentTypes);

      throw new WorkspaceExportException(
          format(
              "Workspace with multiple %s environments can not be converted to devfile", allEnvs));
    }

    EnvironmentImpl env = k8sEnvironments.get(0).getValue();
    throw new WorkspaceExportException(
        format(
            "Exporting of workspace with `%s` is not supported yet.", env.getRecipe().getType()));
  }
}
