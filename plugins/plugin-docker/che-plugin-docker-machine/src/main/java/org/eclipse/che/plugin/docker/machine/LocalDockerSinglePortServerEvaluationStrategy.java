/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.plugin.docker.machine;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.PortBinding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 * Represents a server evaluation strategy for the configuration where the workspace server and workspace
 * containers are running on the same Docker network and are exposed through the same single port.
 *
 * This server evaluation strategy will return a completed {@link ServerImpl} with internal addresses set
 * as {@link LocalDockerServerEvaluationStrategy} does. Contrary external addresses will have the following format:
 *
 *       serverName-workspaceID-cheExternalAddress (e.g. terminal-79rfwhqaztq2ru2k-che.local)
 *
 * <p>cheExternalAddress can be set using property {@code che.docker.ip.external}.
 * This strategy is useful when Che and the workspace servers need to be exposed on the same single TCP port
 *
 * @author Mario Loriedo <mloriedo@redhat.com>
 * @see ServerEvaluationStrategy
 */
public class LocalDockerSinglePortServerEvaluationStrategy extends LocalDockerServerEvaluationStrategy {

    private static final String CHE_WORKSPACE_ID_ENV_VAR = "CHE_WORKSPACE_ID";

    private boolean secureExternalUrls;

    @Inject
    public LocalDockerSinglePortServerEvaluationStrategy(@Nullable @Named("che.docker.ip") String internalAddress,
                                                         @Nullable @Named("che.docker.ip.external") String externalAddress,
                                                         @Named("che.docker.server_evaluation_strategy.secure.external.urls") boolean secureExternalUrls) {
        super(internalAddress, externalAddress);
        this.secureExternalUrls = secureExternalUrls;
    }

    @Override
    protected boolean useHttpsForExternalUrls() {
        return this.secureExternalUrls;
    }

    @Override
    protected Map<String, String> getExternalAddressesAndPorts(ContainerInfo containerInfo,
                                                               String internalHost) {
        String cheExternalAddress = getCheExternalAddress(containerInfo, internalHost);
        String workspaceID        = getWorkspaceID(containerInfo.getConfig().getEnv());
        Map<String, String> labels= containerInfo.getConfig().getLabels();

        Map<String, List<PortBinding>> portBindings = containerInfo.getNetworkSettings().getPorts();
        Map<String, String> addressesAndPorts = new HashMap<>();
        for (String serverKey : portBindings.keySet()) {
            String serverName = getWorkspaceServerName(labels, serverKey);
            String serverURL = serverName + "-" + workspaceID + "-" + cheExternalAddress;
            addressesAndPorts.put(serverKey, serverURL);
        }

        return addressesAndPorts;
    }

    private String getWorkspaceServerName(Map<String, String> labels, String portKey) {
        ServerConfImpl serverConf = getServerConfImpl(portKey, labels, new HashMap<>());
        if (serverConf == null) {
            return "server-" + portKey.split("/",2)[0];
        }
        return serverConf.getRef();
    }

    private String getWorkspaceID(String[] env) {
        Stream<String> envStream = Arrays.stream(env);
        return envStream.filter(v -> v.startsWith(CHE_WORKSPACE_ID_ENV_VAR) && v.contains("=")).
                                         map(v -> v.split("=",2)[1]).
                                         findFirst().
                                         orElse("unknown-ws").
                                         replaceFirst("workspace","");
    }
}
