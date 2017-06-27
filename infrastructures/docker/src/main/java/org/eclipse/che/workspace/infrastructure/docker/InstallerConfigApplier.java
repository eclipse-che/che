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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.docker.InstallerConfigApplier.PROPERTIES.ENVIRONMENT;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Applies docker specific properties of the installers to {@link DockerContainerConfig} or {@link DockerEnvironment}.
 *
 * </p>
 * Dependencies between installers are respected.
 * This class must be called before machines represented by {@link DockerContainerConfig} is started,
 * otherwise changing configuration has no effect.
 * </br>
 * The list of supported properties are:
 * <li>environment</li>
 *
 * The {@code environment} property contains command separated environment variables to set
 * respecting the following format: "name=value".
 *
 * @author Anatolii Bazko
 * @author Alexander Garagatyi
 * @see Installer#getProperties()
 * @see DockerContainerConfig#getEnvironment()
 * @see DockerContainerConfig#getPorts()
 * @see DockerContainerConfig#getLabels()
 */
public class InstallerConfigApplier {
    private static final Logger LOG = getLogger(InstallerConfigApplier.class);

    private final InstallerRegistry installerRegistry;

    @Inject
    public InstallerConfigApplier(InstallerRegistry installerRegistry) {
        this.installerRegistry = installerRegistry;
    }

    /**
     * Applies docker specific properties to an environment of machines.
     *
     * @param envConfig
     *         environment config with the list of installers that should be injected into machine
     * @param dockerEnvironment
     *         affected environment of machines
     * @throws InstallerException
     *         if any error occurs
     */
    public void apply(Environment envConfig,
                      DockerEnvironment dockerEnvironment) throws InstallerException {
        for (Map.Entry<String, ? extends MachineConfig> machineEntry : envConfig.getMachines()
                                                                                .entrySet()) {
            String machineName = machineEntry.getKey();
            MachineConfig machineConf = machineEntry.getValue();
            DockerContainerConfig dockerService = dockerEnvironment.getServices().get(machineName);

            apply(machineConf, dockerService);
        }
    }

    /**
     * Applies docker specific properties to a machine.
     *
     * @param machineConf
     *         machine config with the list of installer that should be injected into machine
     * @param machine
     *         affected machine
     * @throws InstallerException
     *         if any error occurs
     */
    public void apply(@Nullable MachineConfig machineConf,
                      DockerContainerConfig machine) throws InstallerException {
        if (machineConf != null) {
            for (Installer installer : installerRegistry.getOrderedInstallers(machineConf.getInstallers())) {
                addEnv(machine, installer.getProperties());
                addExposedPorts(machine, installer.getServers());
                addLabels(machine, installer.getServers());
            }
        }
    }

    private void addLabels(DockerContainerConfig service, Map<String, ? extends ServerConfig> servers) {
        for (Map.Entry<String, ? extends ServerConfig> entry : servers.entrySet()) {
            String ref = entry.getKey();
            ServerConfig conf = entry.getValue();

            service.getLabels().put("che:server:" + conf.getPort() + ":protocol", conf.getProtocol());
            service.getLabels().put("che:server:" + conf.getPort() + ":ref", ref);

            String path = conf.getPath();
            if (!isNullOrEmpty(path)) {
                service.getLabels().put("che:server:" + conf.getPort() + ":path", path);
            }
        }
    }

    private void addEnv(DockerContainerConfig service, Map<String, String> properties) {
        String environment = properties.get(ENVIRONMENT.toString());
        if (isNullOrEmpty(environment)) {
            return;
        }

        Map<String, String> newEnv = new HashMap<>();
        if (service.getEnvironment() != null) {
            newEnv.putAll(service.getEnvironment());
        }

        for (String env : environment.split(",")) {
            String[] items = env.split("=");
            if (items.length != 2) {
                LOG.warn(format("Illegal environment variable '%s' format", env));
                continue;
            }
            String var = items[0];
            String name = items[1];

            newEnv.put(var, name);
        }

        service.setEnvironment(newEnv);
    }

    private void addExposedPorts(DockerContainerConfig service, Map<String, ? extends ServerConfig> servers) {
        for (ServerConfig server : servers.values()) {
            service.getExpose().add(server.getPort());
        }
    }

    public enum PROPERTIES {
        ENVIRONMENT("environment");

        private final String value;

        PROPERTIES(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
