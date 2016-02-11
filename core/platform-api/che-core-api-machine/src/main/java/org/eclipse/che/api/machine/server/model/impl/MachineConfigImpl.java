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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;

import java.util.Objects;

//TODO move?

/**
 * Data object for {@link MachineConfig}.
 *
 * @author Eugene Voevodin
 */
public class MachineConfigImpl implements MachineConfig {

    private boolean           isDev;
    private String            name;
    private String            type;
    private MachineSourceImpl source;
    private LimitsImpl        limits;

    public MachineConfigImpl() {
    }

    public MachineConfigImpl(boolean isDev,
                             String name,
                             String type,
                             MachineSource source,
                             Limits limits) {
        this.isDev = isDev;
        this.name = name;
        this.type = type;
        if (source != null) {
            this.source = new MachineSourceImpl(source.getType(), source.getLocation());
        }
        this.limits = new LimitsImpl(limits);
    }

    public MachineConfigImpl(MachineConfig machineCfg) {
        this(machineCfg.isDev(),
             machineCfg.getName(),
             machineCfg.getType(),
             machineCfg.getSource(),
             machineCfg.getLimits());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public MachineSource getSource() {
        return source;
    }

    public void setSource(MachineSourceImpl source) {
        this.source = source;
    }

    @Override
    public boolean isDev() {
        return isDev;
    }

    public void setDev(boolean isDev) {
        this.isDev = isDev;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = new LimitsImpl(limits);
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MachineConfigImpl)) return false;
        final MachineConfigImpl other = (MachineConfigImpl)obj;
        return isDev == other.isDev &&
               Objects.equals(name, other.name) &&
               Objects.equals(source, other.source) &&
               Objects.equals(limits, other.limits) &&
               Objects.equals(type, other.type);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Boolean.hashCode(isDev);
        hash = hash * 31 + Objects.hashCode(name);
        hash = hash * 31 + Objects.hashCode(type);
        hash = hash * 31 + Objects.hashCode(source);
        hash = hash * 31 + Objects.hashCode(limits);
        return hash;
    }

    @Override
    public String toString() {
        return "MachineConfigImpl{" +
               "isDev=" + isDev +
               ", name='" + name + '\'' +
               ", type='" + type + '\'' +
               ", source=" + source +
               ", memorySize=" + limits +
               '}';
    }
}
