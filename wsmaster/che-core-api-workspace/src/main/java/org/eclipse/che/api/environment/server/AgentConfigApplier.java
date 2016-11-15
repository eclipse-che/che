/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.ServerConf2;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.environment.server.AgentConfigApplier.PROPERTIES.ENVIRONMENT;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Applies docker specific properties of the agents to {@link CheServiceImpl} or {@link CheServicesEnvironmentImpl}.
 *
 * </p>
 * Dependencies between agents are respected.
 * This class must be called before machines represented by {@link CheServiceImpl} is started,
 * otherwise changing configuration has no effect.
 * </br>
 * The list of supported properties are:
 * <li>environment</li>
 *
 * The {@code environment} property contains command separated environment variables to set
 * respecting the following format: "name=value".
 *
 * @see Agent#getProperties()
 * @see CheServiceImpl#getEnvironment()
 * @see CheServiceImpl#getPorts()
 * @see CheServiceImpl#getLabels()
 *
 * @author Anatolii Bazko
 * @author Alexander Garagatyi
 */
public class AgentConfigApplier {
    private static final Logger LOG = getLogger(AgentConfigApplier.class);

    private final AgentSorter   sorter;
    private final AgentRegistry agentRegistry;

    @Inject
    public AgentConfigApplier(AgentSorter sorter, AgentRegistry agentRegistry) {
        this.sorter = sorter;
        this.agentRegistry = agentRegistry;
    }

    /**
     * Applies docker specific properties to an environment of machines.
     *
     * @param envConfig
     *         environment config with the list of agents that should be injected into machine
     * @param internalEnv
     *         affected environment of machines
     * @throws AgentException
     *         if any error occurs
     */
    public void apply(Environment envConfig,
                      CheServicesEnvironmentImpl internalEnv) throws AgentException {
        for (Map.Entry<String, ? extends ExtendedMachine> machineEntry : envConfig.getMachines()
                                                                                  .entrySet()) {
            String machineName = machineEntry.getKey();
            ExtendedMachine machineConf = machineEntry.getValue();
            CheServiceImpl internalMachine = internalEnv.getServices().get(machineName);

            apply(machineConf, internalMachine);
        }
    }

    /**
     * Applies docker specific properties to a machine.
     *
     * @param machineConf
     *         machine config with the list of agents that should be injected into machine
     * @param machine
     *         affected machine
     * @throws AgentException
     *         if any error occurs
     */
    public void apply(@Nullable ExtendedMachine machineConf,
                      CheServiceImpl machine) throws AgentException {
        if (machineConf != null) {
            for (AgentKey agentKey : sorter.sort(machineConf.getAgents())) {
                Agent agent = agentRegistry.getAgent(agentKey);
                addEnv(machine, agent.getProperties());
                addExposedPorts(machine, agent.getServers());
                addLabels(machine, agent.getServers());
            }
        }
    }

    private void addLabels(CheServiceImpl service, Map<String, ? extends ServerConf2> servers) {
        for (Map.Entry<String, ? extends ServerConf2> entry : servers.entrySet()) {
            String ref = entry.getKey();
            ServerConf2 conf = entry.getValue();

            service.getLabels().put("che:server:" + conf.getPort() + ":protocol", conf.getProtocol());
            service.getLabels().put("che:server:" + conf.getPort() + ":ref", ref);

            String path = conf.getProperties().get("path");
            if (!isNullOrEmpty(path)) {
                service.getLabels().put("che:server:" + conf.getPort() + ":path", path);
            }
        }
    }

    private void addEnv(CheServiceImpl service, Map<String, String> properties) {
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

    private void addExposedPorts(CheServiceImpl service, Map<String, ? extends ServerConf2> servers) {
        for (ServerConf2 server : servers.values()) {
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
