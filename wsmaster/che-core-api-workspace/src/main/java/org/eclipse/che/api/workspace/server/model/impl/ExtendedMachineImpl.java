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
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.ServerConf2;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alexander Garagatyi
 */
@Entity(name = "ExternalMachine")
@Table(name = "externalmachine")
public class ExtendedMachineImpl implements ExtendedMachine {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "externalmachine_agents",
                     joinColumns = @JoinColumn(name = "externalmachine_id"))
    @Column(name = "agents")
    private List<String> agents;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "externalmachine_attributes",
                     joinColumns = @JoinColumn(name = "externalmachine_id"))
    @MapKeyColumn(name = "attributes_key")
    @Column(name = "attributes")
    private Map<String, String> attributes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "servers_id")
    @MapKeyColumn(name = "servers_key")
    private Map<String, ServerConf2Impl> servers;

    public ExtendedMachineImpl() {}

    public ExtendedMachineImpl(List<String> agents,
                               Map<String, ? extends ServerConf2> servers,
                               Map<String, String> attributes) {
        if (agents != null) {
            this.agents = new ArrayList<>(agents);
        }
        if (servers != null) {
            this.servers = servers.entrySet()
                                  .stream()
                                  .collect(Collectors.toMap(Map.Entry::getKey,
                                                            entry -> new ServerConf2Impl(entry.getValue())));
        }
        if (attributes != null) {
            this.attributes = new HashMap<>(attributes);
        }
    }

    public ExtendedMachineImpl(ExtendedMachine machine) {
        this(machine.getAgents(), machine.getServers(), machine.getAttributes());
    }

    @Override
    public List<String> getAgents() {
        if (agents == null) {
            agents = new ArrayList<>();
        }
        return agents;
    }

    public void setAgents(List<String> agents) {
        this.agents = agents;
    }

    public ExtendedMachineImpl withAgents(List<String> agents) {
        this.agents = agents;
        return this;
    }

    @Override
    public Map<String, ServerConf2Impl> getServers() {
        if (servers == null) {
            servers = new HashMap<>();
        }
        return servers;
    }

    public void setServers(Map<String, ServerConf2Impl> servers) {
        this.servers = servers;
    }

    public ExtendedMachineImpl withServers(Map<String, ServerConf2Impl> servers) {
        this.servers = servers;
        return this;
    }

    @Override
    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public ExtendedMachineImpl withAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExtendedMachineImpl)) {
            return false;
        }
        final ExtendedMachineImpl that = (ExtendedMachineImpl)obj;
        return Objects.equals(id, that.id)
               && getAgents().equals(that.getAgents())
               && getAttributes().equals(that.getAttributes())
               && getServers().equals(that.getServers());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + getAgents().hashCode();
        hash = 31 * hash + getAttributes().hashCode();
        hash = 31 * hash + getServers().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "ExtendedMachineImpl{" +
               "id=" + id +
               ", agents=" + agents +
               ", attributes=" + attributes +
               ", servers=" + servers +
               '}';
    }
}
