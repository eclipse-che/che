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

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server.Server;
import org.eclipse.che.ide.util.Config;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The class which describes machine entity. The class is wrapper of MachineDescriptor.
 *
 * @author Dmitry Shnurenko
 */
public class Machine {

    public static final String TERMINAL_REF_KEY   = "terminal";
    public static final String EXTENSIONS_REF_KEY = "extensions";

    private final MachineDto    descriptor;
    private final EntityFactory entityFactory;

    private String activeTabName;

    @Inject
    public Machine(MachineLocalizationConstant locale, EntityFactory entityFactory, @Assisted MachineDto descriptor) {
        this.entityFactory = entityFactory;
        this.descriptor = descriptor;

        this.activeTabName = locale.tabInfo();
    }

    /** @return id of current machine */
    public String getId() {
        return descriptor.getId();
    }

    /** @return current machine's display name */
    public String getDisplayName() {
        return descriptor.getName();
    }

    /** @return state of current machine */
    public MachineStatus getStatus() {
        return descriptor.getStatus();
    }

    /** @return type of current machine */
    public String getType() {
        return descriptor.getType();
    }

    /** @return script of machine recipe */
    public String getRecipeUrl() {
        MachineSourceDto machineSource = descriptor.getSource();

        String machineSourceType = machineSource.getType();

        if ("recipe".equalsIgnoreCase(machineSourceType)) {
            return machineSource.getLocation();
        }

        return "";
    }

    /** @return special url to connect to terminal web socket. */
    @NotNull
    public String getWsServerExtensionsUrl() {
        String url = "";
        Map<String, ServerDto> serverDescriptors = descriptor.getMetadata().getServers();
        for (ServerDto descriptor : serverDescriptors.values()) {
            if (EXTENSIONS_REF_KEY.equals(descriptor.getRef())) {
                url = descriptor.getUrl();
            }
        }

        String extUrl = url.substring(url.indexOf(':'), url.length());

        boolean isSecureConnection = Window.Location.getProtocol().equals("https:");

        return (isSecureConnection ? "wss" : "ws") + extUrl + Config.getCheExtensionPath() + "/ws";
    }

    /**
     * Returns boolean which defines bounding workspace to current machine
     *
     * @return <code>true</code> machine is bounded to workspace,<code>false</code> machine isn't bounded to workspace
     */
    public boolean isDev() {
        return descriptor.isDev();
    }

    /** Returns information about machine. */
    public Map<String, String> getProperties() {
        return descriptor.getMetadata().getProperties();
    }

    public void setActiveTabName(String activeTabName) {
        this.activeTabName = activeTabName;
    }

    public String getActiveTabName() {
        return activeTabName;
    }

    public String getTerminalUrl() {
        Map<String, ServerDto> serverDescriptors = descriptor.getMetadata().getServers();

        for (ServerDto descriptor : serverDescriptors.values()) {

            if (TERMINAL_REF_KEY.equals(descriptor.getRef())) {
                String terminalUrl = descriptor.getUrl();

                terminalUrl = terminalUrl.substring(terminalUrl.indexOf(':'), terminalUrl.length());

                boolean isSecureConnection = Window.Location.getProtocol().equals("https:");

                return (isSecureConnection ? "wss" : "ws") + terminalUrl + "/pty";
            }
        }

        return "";
    }

    public String getWorkspaceId() {
        return descriptor.getWorkspaceId();
    }

    public List<Server> getServersList() {
        List<Server> serversList = new ArrayList<>();

        Map<String, ServerDto> servers = descriptor.getMetadata().getServers();

        for (Map.Entry<String, ServerDto> entry : servers.entrySet()) {
            String exposedPort = entry.getKey();
            ServerDto descriptor = entry.getValue();

            Server server = entityFactory.createServer(exposedPort, descriptor);

            serversList.add(server);
        }

        return serversList;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Machine otherMachine = (Machine)other;

        return Objects.equals(getId(), otherMachine.getId());

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
