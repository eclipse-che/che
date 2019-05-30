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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static org.eclipse.che.api.workspace.shared.Constants.ARBITRARY_USER_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.CONTAINER_SOURCE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.RECIPE_CONTAINER_SOURCE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.Container;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

public class OpenShiftCommandProvisioner implements ConfigurationProvisioner<OpenShiftEnvironment> {

  @VisibleForTesting protected static final List<String> SHELL_BINARY = ImmutableList.of("/bin/sh");
  @VisibleForTesting protected static final String SHELL_ARGS = "-c";

  @VisibleForTesting
  protected static final String ADD_USER_COMMAND =
      "if ! whoami &> /dev/null && [ -w /etc/passwd ]; then "
          + "echo \"user:x:$(id -u):0:user user:projects/:/bin/bash\" >> /etc/passwd;"
          + "fi;";

  @VisibleForTesting protected static final String COMMAND_FORMAT = "%s %s %s";

  @Override
  public void provision(OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    if (!supportArbitraryUser(osEnv.getAttributes())) {
      return;
    }

    Set<String> recipeMachineNames =
        osEnv
            .getMachines()
            .entrySet()
            .stream()
            .filter(
                e ->
                    RECIPE_CONTAINER_SOURCE.equals(
                        e.getValue().getAttributes().get(CONTAINER_SOURCE_ATTRIBUTE)))
            .map(Entry::getKey)
            .collect(Collectors.toSet());

    for (PodData podData : osEnv.getPodsData().values()) {
      for (Container container : podData.getSpec().getContainers()) {
        String machineName = Names.machineName(podData, container);
        if (recipeMachineNames.contains(machineName)) {
          rewriteContainerCommand(container);
        }
      }
    }
  }

  private void rewriteContainerCommand(Container container) {
    List<String> defaultCommand = container.getCommand();
    List<String> defaultArgs = container.getArgs();

    if (defaultCommand == null || defaultCommand.size() == 0) {
      return;
    }

    String script =
        String.format(
            COMMAND_FORMAT,
            ADD_USER_COMMAND,
            String.join(" ", defaultCommand),
            String.join(" ", defaultArgs));
    container.setCommand(SHELL_BINARY);
    container.setArgs(ImmutableList.of(SHELL_ARGS, script));
  }

  private boolean supportArbitraryUser(Map<String, String> workspaceAttributes) {
    String supportArbitraryUser = workspaceAttributes.get(ARBITRARY_USER_ATTRIBUTE);
    return "true".equals(supportArbitraryUser);
  }
}
