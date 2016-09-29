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
package org.eclipse.che.ide.extension.machine.client.machine;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.ide.api.machine.MachineEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;

/**
 * The class which describes machine entity.
 *
 * @author Dmitry Shnurenko
 * @author Roman Nikitenko
 */
public class MachineEntityImpl implements MachineEntity {

    private final MachineDto    descriptor;
    private final List<Link>    machineLinks;
    private final MachineConfig machineConfig;

    @Inject
    public MachineEntityImpl(@Assisted MachineDto descriptor) {
        this.descriptor = descriptor;
        this.machineLinks = descriptor.getLinks();
        this.machineConfig = descriptor.getConfig();
    }

    @Override
    public MachineConfig getConfig() {
        return machineConfig;
    }

    @Override
    public String getId() {
        return descriptor.getId();
    }

    @Override
    public String getWorkspaceId() {
        return descriptor.getWorkspaceId();
    }

    @Override
    public String getEnvName() {
        return descriptor.getEnvName();
    }

    @Override
    public String getOwner() {
        return descriptor.getOwner();
    }

    @Override
    public MachineStatus getStatus() {
        return descriptor.getStatus();
    }

    @Override
    public MachineRuntimeInfo getRuntime() {
        return descriptor.getRuntime();
    }

    @Override
    public boolean isDev() {
        return machineConfig.isDev();
    }

    @Override
    public String getType() {
        return machineConfig.getType();
    }

    @Override
    public String getDisplayName() {
        return machineConfig.getName();
    }

    @Override
    public Map<String, String> getProperties() {
        MachineRuntimeInfo machineRuntime = descriptor.getRuntime();
        return machineRuntime != null ? machineRuntime.getProperties() : null;
    }

    @Override
    public String getTerminalUrl() {
        for (Link link : machineLinks) {
            if (TERMINAL_REFERENCE.equals(link.getRel())) {
                return link.getHref();
            }
        }
        return "";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        MachineEntityImpl otherMachine = (MachineEntityImpl)other;

        return Objects.equals(getId(), otherMachine.getId());

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
