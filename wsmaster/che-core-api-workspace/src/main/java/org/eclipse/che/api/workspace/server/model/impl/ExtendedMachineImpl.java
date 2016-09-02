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
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.ServerConf2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alexander Garagatyi
 */
public class ExtendedMachineImpl implements ExtendedMachine {
    private List<String>                 agents;
    private Map<String, ServerConf2Impl> servers;

    public ExtendedMachineImpl() {}

    public ExtendedMachineImpl(List<String> agents,
                               Map<String, ? extends ServerConf2> servers) {
        if (agents != null) {
            this.agents = new ArrayList<>(agents);
        }
        if (servers != null) {
            this.servers = servers.entrySet()
                                  .stream()
                                  .collect(Collectors.toMap(Map.Entry::getKey,
                                                            entry -> new ServerConf2Impl(entry.getValue())));
        }
    }

    public ExtendedMachineImpl(ExtendedMachine machine) {
        if (machine.getAgents() != null) {
            this.agents = new ArrayList<>(machine.getAgents());
        }
        if (machine.getServers() != null) {
            this.servers = machine.getServers()
                                  .entrySet()
                                  .stream()
                                  .collect(Collectors.toMap(Map.Entry::getKey,
                                                            entry -> new ServerConf2Impl(entry.getValue())));
        }
    }

    @Override
    public List<String> getAgents() {
        return agents;
    }

    public void setAgents(List<String> agents) {
        this.agents = agents;
    }

    @Override
    public Map<String, ServerConf2Impl> getServers() {
        return servers;
    }

    public void setServers(Map<String, ServerConf2Impl> servers) {
        this.servers = servers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtendedMachineImpl)) return false;
        ExtendedMachineImpl that = (ExtendedMachineImpl)o;
        return Objects.equals(agents, that.agents) &&
               Objects.equals(servers, that.servers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agents, servers);
    }

    @Override
    public String toString() {
        return "ExtendedMachineImpl{" +
               "agents=" + agents +
               ", servers=" + servers +
               '}';
    }
}
