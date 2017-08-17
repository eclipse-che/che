/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.openshift.provision.installer;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;

import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.Utils;
import org.eclipse.che.workspace.infrastructure.openshift.ServerExposer;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.ConfigurationProvisioner;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Applies OpenShift specific properties of the installers to {@link OpenShiftEnvironment}.
 *
 * <p>This class must be called before OpenShift environment is started,
 * otherwise changing configuration has no effect.
 *
 * <p>This class performs following changes to environment:
 * <br> - adds environment variable to {@link Container containers} that are required by installers or agents.
 * <br> - adds environment variables which are specified in properties of installer configuration.
 * The environment property contains environment variables in the following format: "env1=value1,env2=value2,...";
 * <br> - performs all required changes that are needed for exposing installers' servers.
 *
 * @author Sergii Leshchenko
 */
public class InstallerConfigProvisioner implements ConfigurationProvisioner {
    private static final Logger LOG = getLogger(InstallerConfigProvisioner.class);

    private static final String ENVIRONMENT_PROPERTY = "environment";

    private final InstallerRegistry installerRegistry;
    private final String            cheServerEndpoint;


    @Inject
    public InstallerConfigProvisioner(InstallerRegistry installerRegistry,
                                      @Named("che.infra.openshift.che_server_endpoint") String cheServerEndpoint) {
        this.installerRegistry = installerRegistry;
        this.cheServerEndpoint = cheServerEndpoint;
    }

    @Override
    public void provision(EnvironmentImpl environment,
                          OpenShiftEnvironment osEnv,
                          RuntimeIdentity identity) throws InfrastructureException {
        for (Pod pod : osEnv.getPods().values()) {
            String podName = pod.getMetadata().getName();
            for (Container container : pod.getSpec().getContainers()) {
                String containerName = container.getName();
                String machineName = podName + "/" + containerName;
                MachineConfig machineConf = environment.getMachines().get(machineName);

                List<String> installers = machineConf.getInstallers();
                Map<String, ServerConfig> name2Server = new HashMap<>();
                for (Installer installer : getInstallers(installers)) {
                    provisionEnv(container, installer.getProperties());
                    name2Server.putAll(installer.getServers());
                }
                ServerExposer serverExposer = new ServerExposer(machineName, container, osEnv);
                serverExposer.expose("agents", name2Server);

                // CHE_API is used by installers for agent binary downloading
                container.getEnv().removeIf(env -> "CHE_API".equals(env.getName()));
                container.getEnv().add(new EnvVar("CHE_API", cheServerEndpoint, null));

                // WORKSPACE_ID is required only by workspace agent
                if (Utils.isDev(machineConf)) {
                    container.getEnv().removeIf(env -> "CHE_WORKSPACE_ID".equals(env.getName()));
                    container.getEnv().add(new EnvVar("CHE_WORKSPACE_ID", identity.getWorkspaceId(), null));
                }
            }
        }
    }

    private List<Installer> getInstallers(List<String> installerIds) throws InfrastructureException {
        try {
            return installerRegistry.getOrderedInstallers(installerIds);
        } catch (InstallerException e) {
            throw new InfrastructureException(e.getMessage(), e);
        }
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
