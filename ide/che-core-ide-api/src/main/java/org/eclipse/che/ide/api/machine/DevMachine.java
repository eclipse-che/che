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
package org.eclipse.che.ide.api.machine;

import com.google.common.base.Strings;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Describe development machine instance.
 * Must contains all information that need to communicate with dev machine such as links, type, environment variable and etc.
 *
 * @author Vitalii Parfonov
 */
public class DevMachine {

    private final MachineDto                    devMachineDescriptor;
    private final Map<String, DevMachineServer> servers;
    private final Map<String, String>           runtimeProperties;
    private final Map<String, String>           envVariables;
    private final List<Link>                    devMachineLinks;

    public DevMachine(@NotNull MachineDto devMachineDescriptor) {
        this.devMachineDescriptor = devMachineDescriptor;
        this.devMachineLinks = devMachineDescriptor.getLinks();

        Map<String, ServerDto> serverDtoMap = devMachineDescriptor.getRuntime().getServers();
        servers = new HashMap<>(serverDtoMap.size());
        for (String s : serverDtoMap.keySet()) {
            servers.put(s, new DevMachineServer(serverDtoMap.get(s)));
        }
        runtimeProperties = devMachineDescriptor.getRuntime().getProperties();
        envVariables = devMachineDescriptor.getRuntime().getEnvVariables();
    }

    public Map<String, String> getEnvVariables() {
        return envVariables;
    }

    public Map<String, String> getRuntimeProperties() {
        return runtimeProperties;
    }

    public String getType() {
        return devMachineDescriptor.getConfig().getType();
    }

    public String getWsAgentWebSocketUrl() {
        for (Link link : devMachineLinks) {
            if (Constants.WSAGENT_WEBSOCKET_REFERENCE.equals(link.getRel())) {
                return link.getHref();
            }
        }
        //should not be
        final String message = "Reference " + Constants.WSAGENT_WEBSOCKET_REFERENCE + " not found in DevMachine description";
        Log.error(getClass(), message);
        throw new RuntimeException(message);
    }

    public String getTerminalUrl() {
        for (Link link : devMachineLinks) {
            if (Constants.TERMINAL_REFERENCE.equals(link.getRel())) {
                return link.getHref();
            }
        }
        //should not be
        final String message = "Reference " + Constants.TERMINAL_REFERENCE + " not found in DevMachine description";
        Log.error(getClass(), message);
        throw new RuntimeException(message);
    }

    /**
     *
     * @return return base URL to the ws agent REST services. URL will be always without trailing slash
     */
    public String getWsAgentBaseUrl() {
        DevMachineServer server = getServer(Constants.WSAGENT_REFERENCE);
        if (server != null) {
            String url = server.getUrl();
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            return url;
        } else {
            //should not be
            String message = "Reference " + Constants.WSAGENT_REFERENCE + " not found in DevMachine description";
            Log.error(getClass(), message);
            throw new RuntimeException(message);
        }
    }

    public Map<String, DevMachineServer> getServers() {
        return servers;
    }

    public DevMachineServer getServer(String reference) {
        if (!Strings.isNullOrEmpty(reference)) {
            for (DevMachineServer server : servers.values()) {
                if (reference.equals(server.getRef())) {
                    return server;
                }
            }
        }
        return null;
    }

    public String getWorkspace() {
        return devMachineDescriptor.getWorkspaceId();
    }

    public String getId() {
        return devMachineDescriptor.getId();
    }

    public List<Link> getDevMachineLinks() {
        return devMachineLinks;
    }

    /** Returns address (protocol://host:port) of the Workspace Agent. */
    public String getAddress() {
        final DevMachineServer server = getServer(Constants.WSAGENT_REFERENCE);
        return server.getProtocol() + "://" + server.getAddress();
    }

    /** Returns {@link Machine descriptor} of the Workspace Agent. */
    public Machine getDescriptor() {
        return devMachineDescriptor;
    }
}
