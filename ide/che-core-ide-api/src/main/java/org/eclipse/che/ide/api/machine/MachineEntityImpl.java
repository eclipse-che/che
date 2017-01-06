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
package org.eclipse.che.ide.api.machine;

import com.google.common.base.Strings;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Vitalii Parfonov
 */

public class MachineEntityImpl implements MachineEntity {

    protected final Machine machineDescriptor;
    protected final MachineConfig machineConfig;

    protected final Map<String, MachineServer> servers;
    protected final Map<String, String>        runtimeProperties;
    protected final Map<String, String>        envVariables;
    protected final List<Link>                 machineLinks;


    public MachineEntityImpl(@NotNull Machine machineDescriptor) {
        this.machineDescriptor = machineDescriptor;
        this.machineConfig = machineDescriptor != null ? machineDescriptor.getConfig() : null;
        this.machineLinks = machineDescriptor instanceof Hyperlinks ? ((Hyperlinks)machineDescriptor).getLinks() : null;

        if (machineDescriptor == null || machineDescriptor.getRuntime() == null) {
            servers = null;
            runtimeProperties = null;
            envVariables = null;
        } else {
            MachineRuntimeInfo machineRuntime = machineDescriptor.getRuntime();
            Map<String, ? extends Server> serverDtoMap = machineRuntime.getServers();
            servers = new HashMap<>(serverDtoMap.size());
            for (String s : serverDtoMap.keySet()) {
                servers.put(s, new MachineServer(serverDtoMap.get(s)));
            }
            runtimeProperties = machineRuntime.getProperties();
            envVariables = machineRuntime.getEnvVariables();
        }



    }


    public String getWorkspace() {
        return machineDescriptor.getWorkspaceId();
    }

    @Override
    public MachineConfig getConfig() {
        return machineConfig;
    }

    public String getId() {
        return machineDescriptor.getId();
    }

    @Override
    public String getWorkspaceId() {
        return machineDescriptor.getWorkspaceId();
    }

    @Override
    public String getEnvName() {
        return machineDescriptor.getEnvName();
    }

    @Override
    public String getOwner() {
        return machineDescriptor.getOwner();
    }

    @Override
    public MachineStatus getStatus() {
        return machineDescriptor.getStatus();
    }

    @Override
    public MachineRuntimeInfo getRuntime() {
        return machineDescriptor.getRuntime();
    }

    @Override
    public boolean isDev() {
        return machineDescriptor.getConfig().isDev();
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
        return runtimeProperties;
    }

    public String getTerminalUrl() {
        for (Link link : machineLinks) {
            if (Constants.TERMINAL_REFERENCE.equals(link.getRel())) {
                return link.getHref();
            }
        }
        //should not be
        final String message = "Reference " + Constants.TERMINAL_REFERENCE + " not found in " + machineConfig.getName()  + " description";
        Log.error(getClass(), message);
        throw new RuntimeException(message);
    }

    public String getExecAgentUrl() {
        for (Link link :machineLinks) {
            if (Constants.EXEC_AGENT_REFERENCE.equals(link.getRel())) {
                return link.getHref();
            }
        }
        //should not be
        final String message = "Reference " + Constants.EXEC_AGENT_REFERENCE + " not found in " +  machineConfig.getName() + " description";
        Log.error(getClass(), message);
        throw new RuntimeException(message);
    }

    @Override
    public Map<String, MachineServer> getServers() {
        return servers;
    }

    @Override
    public MachineServer getServer(String reference) {
        if (!Strings.isNullOrEmpty(reference)) {
            for (MachineServer server : servers.values()) {
                if (reference.equals(server.getRef())) {
                    return server;
                }
            }
        }
        return null;
    }

    @Override
    public List<Link> getMachineLinks() {
        return machineLinks;
    }

    @Override
    public Link getMachineLink(String ref) {
        if (!Strings.isNullOrEmpty(ref)) {
            for (Link link : machineLinks) {
                if (ref.equals(link.getRel())) {
                    return link;
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getEnvVariables() {
        return envVariables;
    }

    /** Returns {@link Machine descriptor} of the Workspace Agent. */
    @Override
    public Machine getDescriptor() {
        return machineDescriptor;
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
