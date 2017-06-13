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

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;

/** @deprecated use {@link org.eclipse.che.ide.api.workspace.model.MachineImpl} */
@Deprecated
public class MachineEntityImpl implements MachineEntity {

    protected final Machine                    machineDescriptor;
    protected final Map<String, MachineServer> servers;
    protected final Map<String, String>        runtimeProperties;
    protected final List<Link>                 machineLinks;
    private final   String                     name;

    public MachineEntityImpl(String name, @NotNull Machine machineDescriptor) {
        this.name = name;
        this.machineDescriptor = machineDescriptor;
        this.machineLinks = new ArrayList<>();

        Map<String, ? extends Server> serverDtoMap = machineDescriptor.getServers();
        servers = new HashMap<>(serverDtoMap.size());
        for (String s : serverDtoMap.keySet()) {
            servers.put(s, new MachineServer(s, serverDtoMap.get(s)));
        }
        runtimeProperties = machineDescriptor.getProperties();
    }

    @Override
    public boolean isDev() {
        return machineDescriptor.getServers().get(WSAGENT_REFERENCE) != null;
    }

    @Deprecated
    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Deprecated
    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public Map<String, String> getProperties() {
        return runtimeProperties;
    }

    @Deprecated
    @Override
    public String getTerminalUrl() {
        // FIXME: spi ide
        final MachineServer server = getServer(Constants.TERMINAL_REFERENCE);
        if (server != null) {
            return server.getUrl().replaceFirst("http", "ws") + "/pty";
        }

        //should not be
        final String message = "Reference " + Constants.TERMINAL_REFERENCE + " not found in " + name + " description";
        Log.error(getClass(), message);
        throw new RuntimeException(message);
    }

    @Deprecated
    @Override
    public String getExecAgentUrl() {
        // FIXME: spi ide
        final MachineServer server = getServer(Constants.EXEC_AGENT_REFERENCE);
        if (server != null) {
            return server.getUrl().replaceFirst("http", "ws") + "/connect";
        }

        //should not be
        final String message = "Reference " + Constants.EXEC_AGENT_REFERENCE + " not found in " + name + " description";
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

        return Objects.equals(getName(), otherMachine.getName());

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }
}
