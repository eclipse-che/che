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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineLimits;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.ServerConf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Data object for {@link MachineConfig}.
 *
 * @author Eugene Voevodin
 */
public class MachineConfigImpl implements MachineConfig {

    public static MachineConfigImplBuilder builder() {
        return new MachineConfigImplBuilder();
    }

    private boolean              dev;
    private String               name;
    private String               type;
    private MachineSourceImpl    source;
    private MachineLimitsImpl    limits;
    private List<ServerConfImpl> servers;
    private Map<String, String>  envVariables;

    public MachineConfigImpl() {
    }

    public MachineConfigImpl(boolean dev,
                             String name,
                             String type,
                             MachineSource source,
                             MachineLimits machineLimits,
                             List<? extends ServerConf> servers,
                             Map<String, String> envVariables) {
        this.dev = dev;
        this.name = name;
        this.type = type;
        this.envVariables = envVariables;
        if (servers != null) {
            this.servers = servers.stream()
                                  .map(ServerConfImpl::new)
                                  .collect(Collectors.toList());
        }
        if (source != null) {
            this.source = new MachineSourceImpl(source);
        }
        this.limits = new MachineLimitsImpl(machineLimits);

    }

    public MachineConfigImpl(MachineConfig machineCfg) {
        this(machineCfg.isDev(),
             machineCfg.getName(),
             machineCfg.getType(),
             machineCfg.getSource(),
             machineCfg.getLimits(),
             machineCfg.getServers(),
             machineCfg.getEnvVariables());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public MachineSourceImpl getSource() {
        return source;
    }

    public void setSource(MachineSourceImpl machineSource) {
        this.source = machineSource;
    }

    @Override
    public boolean isDev() {
        return dev;
    }

    public void setDev(boolean dev) {
        this.dev = dev;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public MachineLimitsImpl getLimits() {
        return limits;
    }

    public void setLimits(MachineLimits machineLimits) {
        this.limits = new MachineLimitsImpl(machineLimits);
    }

    @Override
    public List<ServerConfImpl> getServers() {
        if (servers == null) {
            servers = new ArrayList<>();
        }
        return servers;
    }

    public void setServers(List<ServerConfImpl> servers) {
        this.servers = servers;
    }

    @Override
    public Map<String, String> getEnvVariables() {
        if (envVariables == null) {
            envVariables = new HashMap<>();
        }
        return envVariables;
    }

    public void setEnvVariables(Map<String, String> envVariables) {
        this.envVariables = envVariables;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MachineConfigImpl)) return false;
        final MachineConfigImpl other = (MachineConfigImpl)obj;
        return dev == other.dev &&
               Objects.equals(name, other.name) &&
               Objects.equals(source, other.source) &&
               Objects.equals(limits, other.limits) &&
               Objects.equals(type, other.type) &&
               Objects.equals(getServers(), other.getServers()) &&
               Objects.equals(getEnvVariables(), other.getEnvVariables());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Boolean.hashCode(dev);
        hash = hash * 31 + Objects.hashCode(name);
        hash = hash * 31 + Objects.hashCode(type);
        hash = hash * 31 + Objects.hashCode(source);
        hash = hash * 31 + Objects.hashCode(limits);
        hash = hash * 31 + Objects.hashCode(getServers());
        hash = hash * 31 + Objects.hashCode(getEnvVariables());
        return hash;
    }

    @Override
    public String toString() {
        return "MachineConfigImpl{" +
               "dev=" + dev +
               ", name='" + name + '\'' +
               ", type='" + type + '\'' +
               ", source=" + source +
               ", machineLimits=" + limits +
               ", servers=" + getServers() +
               ", envVariables=" + getEnvVariables() +
               '}';
    }

    /**
     * Helps to build complex {@link MachineConfigImpl machine config impl}.
     *
     * @see MachineConfigImpl#builder()
     */
    public static class MachineConfigImplBuilder {

        private boolean                    dev;
        private String                     name;
        private String                     type;
        private MachineSource              source;
        private MachineLimits              machineLimits;
        private List<? extends ServerConf> servers;
        private Map<String, String>        envVariables;

        public MachineConfigImpl build() {
            return new MachineConfigImpl(dev,
                                         name,
                                         type,
                                         source,
                                         machineLimits,
                                         servers,
                                         envVariables);
        }

        public MachineConfigImplBuilder fromConfig(MachineConfig machineConfig) {
            dev = machineConfig.isDev();
            name = machineConfig.getName();
            type = machineConfig.getType();
            source = machineConfig.getSource();
            machineLimits = machineConfig.getLimits();
            servers = machineConfig.getServers();
            envVariables = machineConfig.getEnvVariables();
            return this;
        }

        public MachineConfigImplBuilder setDev(boolean dev) {
            this.dev = dev;
            return this;
        }

        public MachineConfigImplBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public MachineConfigImplBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public MachineConfigImplBuilder setSource(MachineSource machineSource) {
            this.source = machineSource;
            return this;
        }

        public MachineConfigImplBuilder setLimits(MachineLimits machineLimits) {
            this.machineLimits = machineLimits;
            return this;
        }

        public MachineConfigImplBuilder setServers(List<? extends ServerConf> servers) {
            this.servers = servers;
            return this;
        }

        public MachineConfigImplBuilder setEnvVariables(Map<String, String> envVariables) {
            this.envVariables = envVariables;
            return this;
        }
    }
}
