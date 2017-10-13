/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.provision.installer;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.ConfigurationProvisioner;
import org.slf4j.Logger;

/**
 * Applies OpenShift specific properties of the installers to {@link OpenShiftEnvironment}.
 *
 * <p>This class must be called before OpenShift environment is started, otherwise changing
 * configuration has no effect.
 *
 * <p>This class performs following changes to environment: <br>
 * - adds environment variable to {@link Container containers} that are required by installers or
 * agents. <br>
 * - adds environment variables which are specified in properties of installer configuration. The
 * environment property contains environment variables in the following format:
 * "env1=value1,env2=value2,..."; <br>
 * - performs all required changes that are needed for exposing installers' servers.
 *
 * @author Sergii Leshchenko
 */
public class InstallerConfigProvisioner implements ConfigurationProvisioner {

  private static final Logger LOG = getLogger(InstallerConfigProvisioner.class);

  private static final String ENVIRONMENT_PROPERTY = "environment";

  private final MachineTokenProvider machineTokenProvider;
  private final String cheServerEndpoint;

  @Inject
  public InstallerConfigProvisioner(
      MachineTokenProvider machineTokenProvider, @Named("che.api") String cheServerEndpoint) {
    this.machineTokenProvider = machineTokenProvider;
    this.cheServerEndpoint = cheServerEndpoint;
  }

  @Override
  public void provision(
      InternalEnvironment environment, OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    String devMachineName =
        WsAgentMachineFinderUtil.getWsAgentServerMachine(environment)
            .orElseThrow(() -> new InfrastructureException("Machine with wsagent not found"));

    for (Pod pod : osEnv.getPods().values()) {
      String podName = pod.getMetadata().getName();
      for (Container container : pod.getSpec().getContainers()) {
        String containerName = container.getName();
        String machineName = podName + "/" + containerName;
        InternalMachineConfig machineConf = environment.getMachines().get(machineName);

        for (Installer installer : machineConf.getInstallers()) {
          provisionEnv(container, installer.getProperties());
        }

        // CHE_API is used by installers for agent binary downloading
        putEnv(container.getEnv(), "CHE_API", cheServerEndpoint);

        putEnv(
            container.getEnv(),
            "USER_TOKEN",
            machineTokenProvider.getToken(identity.getWorkspaceId()));

        // TODO incorrect place for env variable addition. workspace ID is needed for wsagent server, not installer
        // WORKSPACE_ID is required only by workspace agent
        if (devMachineName.equals(machineName)) {
          putEnv(container.getEnv(), "CHE_WORKSPACE_ID", identity.getWorkspaceId());
        }
      }
    }
  }

  private void putEnv(List<EnvVar> envs, String key, String value) {
    envs.removeIf(env -> key.equals(env.getName()));
    envs.add(new EnvVar(key, value, null));
  }

  private void provisionEnv(Container container, Map<String, String> properties) {
    String environment = properties.get(ENVIRONMENT_PROPERTY);
    if (isNullOrEmpty(environment)) {
      return;
    }

    for (String env : environment.split(",")) {
      String[] items = env.split("=");
      if (items.length != 2) {
        LOG.warn(format("Illegal environment variable '%s' format", env));
        continue;
      }
      String name = items[0];
      String value = items[1];

      container.getEnv().add(new EnvVar(name, value, null));
    }
  }
}
