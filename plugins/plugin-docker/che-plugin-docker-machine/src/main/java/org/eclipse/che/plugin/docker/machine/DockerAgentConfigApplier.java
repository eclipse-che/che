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
package org.eclipse.che.plugin.docker.machine;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.plugin.docker.machine.DockerAgentConfigApplier.PROPERTIES.ENVIRONMENT;
import static org.eclipse.che.plugin.docker.machine.DockerAgentConfigApplier.PROPERTIES.PORTS;

/**
 * Applies docker specific properties of the agents over {@link ContainerConfig}.
 * Dependencies between agents are respected.
 * Docker instance must't be started, otherwise changing configuration has no effect.
 *
 * The list of supported properties are:
 * <li>ports</li>
 * <li>environment</li>
 *
 * The {@code ports} property contains comma separated ports to expose respecting
 * the following format: "label:port/protocol" or "port/protocol.
 *
 * The {@code environment} property contains command separated environment variables to set
 * respecting the following format: "name=value".
 *
 * @see Agent#getProperties()
 * @see ContainerConfig#getEnv()
 * @see ContainerConfig#getExposedPorts()
 *
 * @author Anatolii Bazko
 */
@Singleton
public class DockerAgentConfigApplier {
    private static final Logger LOG = LoggerFactory.getLogger(DockerAgentConfigApplier.class);

    private final AgentSorter sorter;

    @Inject
    public DockerAgentConfigApplier(AgentSorter sorter) {
        this.sorter = sorter;
    }

    /**
     * Applies docker specific properties.
     *
     * @param containerConfig
     *      the container configuration to change
     * @param agentKeys
     *      the list of injected agents into machine
     *
     * @throws MachineException
     */
    public void applyOn(ContainerConfig containerConfig, List<String> agentKeys) throws MachineException {
        try {
            for (Agent agent : sorter.sort(agentKeys)) {
                addEnv(containerConfig, agent.getProperties());
                addExposedPorts(containerConfig, agent.getProperties());
            }
        } catch (AgentException e) {
            throw new MachineException(e.getMessage(), e);
        }
    }

    private void addEnv(ContainerConfig containerConfig, Map<String, String> properties) {
        String environment = properties.get(ENVIRONMENT.toString());
        if (isNullOrEmpty(environment)) {
            return;
        }

        List<String> newEnv = new LinkedList<>();
        if (containerConfig.getEnv() != null) {
            newEnv.addAll(asList(containerConfig.getEnv()));
        }

        for (String env : environment.split(",")) {
            String[] items = env.split("=");
            if (items.length != 2) {
                LOG.warn(format("Illegal environment variable '%s' format", env));
                continue;
            }
            newEnv.add(env);
        }

        containerConfig.setEnv(newEnv.toArray(new String[newEnv.size()]));
    }

    private void addExposedPorts(ContainerConfig containerConfig, Map<String, String> properties) {
        String ports = properties.get(PORTS.toString());
        if (isNullOrEmpty(ports)) {
            return;
        }

        for (String port : ports.split(",")) {
            String[] items = port.split(":"); // ref:port
            if (items.length == 1) {
                containerConfig.getExposedPorts().put(items[0], Collections.emptyMap());
            } else {
                containerConfig.getExposedPorts().put(items[1], Collections.emptyMap());
            }
        }
    }


    enum PROPERTIES {
        PORTS("ports"),
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

