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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.environment.server.AgentConfigApplier.PROPERTIES.ENVIRONMENT;
import static org.eclipse.che.api.environment.server.AgentConfigApplier.PROPERTIES.PORTS;

/**
 * Applies docker specific properties of the agents over {@link CheServiceImpl}.
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
 * @see CheServiceImpl#getEnvironment()
 * @see CheServiceImpl#getPorts()
 *
 * @author Anatolii Bazko
 */
@Singleton
public class AgentConfigApplier {
    private static final Logger LOG = LoggerFactory.getLogger(AgentConfigApplier.class);

    private final AgentSorter sorter;
    private final AgentRegistry agentRegistry;

    @Inject
    public AgentConfigApplier(AgentSorter sorter, AgentRegistry agentRegistry) {
        this.sorter = sorter;
        this.agentRegistry = agentRegistry;
    }

    /**
     * Applies docker specific properties.
     *
     * @param service
     *      the service
     * @param agentKeys
     *      the list of injected agents into machine
     *
     * @throws AgentException
     */
    public void modify(CheServiceImpl service, @Nullable List<String> agentKeys) throws AgentException {
        for (AgentKey agentKey : sorter.sort(agentKeys)) {
            Agent agent = agentRegistry.getAgent(agentKey);
            addEnv(service, agent.getProperties());
            addExposedPorts(service, agent.getProperties());
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

    private void addExposedPorts(CheServiceImpl service, Map<String, String> properties) {
        String ports = properties.get(PORTS.toString());
        if (isNullOrEmpty(ports)) {
            return;
        }

        for (String port : ports.split(",")) {
            String[] items = port.split(":"); // ref:port
            if (items.length == 1) {
                service.getExpose().add(items[0]);
            } else {
                service.getExpose().add(items[1]);
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

