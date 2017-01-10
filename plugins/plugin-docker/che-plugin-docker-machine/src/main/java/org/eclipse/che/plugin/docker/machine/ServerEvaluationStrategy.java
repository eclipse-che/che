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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.che.api.core.model.machine.ServerProperties;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerPropertiesImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.PortBinding;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Represents a strategy for resolving Servers associated with workspace containers.
 * Used to extract relevant information from e.g. {@link ContainerInfo} into a map of
 * {@link ServerImpl} objects.
 *
 * @author Angel Misevski <amisevsk@redhat.com>
 * @see ServerEvaluationStrategyProvider
 */
public abstract class ServerEvaluationStrategy {

    protected static final String SERVER_CONF_LABEL_REF_KEY      = "che:server:%s:ref";
    protected static final String SERVER_CONF_LABEL_PROTOCOL_KEY = "che:server:%s:protocol";
    protected static final String SERVER_CONF_LABEL_PATH_KEY     = "che:server:%s:path";

    /**
     * Gets a map of all <strong>internal</strong> addresses exposed by the container in the form of
     * {@code "<address>:<port>"}
     *
     * @param containerInfo the ContainerInfo object that describes the container.
     * @param internalAddress address passed into {@code getServers}; used as fallback if address cannot be
     *        retrieved from containerInfo.
     * @return a Map of port protocol (e.g. "4401/tcp") to address (e.g. "172.17.0.1:32317")
     */
    protected abstract Map<String, String> getInternalAddressesAndPorts(ContainerInfo containerInfo,
                                                                        String internalAddress);

    /**
     * Gets a map of all <strong>external</strong> addresses exposed by the container in the form of
     * {@code "<address>:<port>"}
     *
     * @param containerInfo the ContainerInfo object that describes the container.
     * @param internalAddress address passed into {@code getServers}; used as fallback if address cannot be
     *        retrieved from containerInfo.
     * @return a Map of port protocol (e.g. "4401/tcp") to address (e.g. "localhost:32317")
     */
    protected abstract Map<String, String> getExternalAddressesAndPorts(ContainerInfo containerInfo,
                                                                        String internalAddress);

    /**
     * Constructs a map of {@link ServerImpl} from provided parameters, using selected strategy
     * for evaluating addresses and ports.
     *
     * <p>Keys consist of port number and transport protocol (tcp or udp) separated by
     * a forward slash (e.g. 8080/tcp)
     *
     * @param containerInfo the {@link ContainerInfo} describing the container.
     * @param internalHost alternative hostname to use, if address cannot be obtained from containerInfo
     * @param serverConfMap additional Map of {@link ServerConfImpl}. Configurations here override those found
     *        in containerInfo.
     * @return a Map of the servers exposed by the container.
     */
    public Map<String, ServerImpl> getServers(ContainerInfo containerInfo,
                                              String internalHost,
                                              Map<String, ServerConfImpl> serverConfMap) {

        Map<String, List<PortBinding>> portBindings;
        Map<String, String> labels = Collections.emptyMap();;

        if (containerInfo.getNetworkSettings() != null && containerInfo.getNetworkSettings().getPorts() != null) {
            portBindings = containerInfo.getNetworkSettings().getPorts();
        } else {
            // If we can't get PortBindings, we can't return servers.
            return Collections.emptyMap();
        }
        if (containerInfo.getConfig() != null && containerInfo.getConfig().getLabels() != null) {
            labels = containerInfo.getConfig().getLabels();
        }

        Map<String, String> internalAddressesAndPorts = getInternalAddressesAndPorts(containerInfo, internalHost);
        Map<String, String> externalAddressesAndPorts = getExternalAddressesAndPorts(containerInfo, internalHost);

        Map<String, ServerImpl> servers = new LinkedHashMap<>();

        for (String portProtocol : portBindings.keySet()) {
            String internalAddressAndPort = internalAddressesAndPorts.get(portProtocol);
            String externalAddressAndPort = externalAddressesAndPorts.get(portProtocol);
            ServerConfImpl serverConf = getServerConfImpl(portProtocol, labels, serverConfMap);

            // Add protocol and path to internal/external address, if applicable
            String internalUrl = null;
            String externalUrl = null;
            if (serverConf.getProtocol() != null) {
                String pathSuffix = serverConf.getPath();
                if (pathSuffix != null && !pathSuffix.isEmpty()) {
                    if (pathSuffix.charAt(0) != '/') {
                        pathSuffix = "/" + pathSuffix;
                    }
                } else {
                    pathSuffix = "";
                }
                internalUrl = serverConf.getProtocol() + "://" + internalAddressAndPort + pathSuffix;
                externalUrl = serverConf.getProtocol() + "://" + externalAddressAndPort + pathSuffix;
            }

            ServerProperties properties = new ServerPropertiesImpl(serverConf.getPath(),
                                                                   internalAddressAndPort,
                                                                   internalUrl);

            servers.put(portProtocol, new ServerImpl(serverConf.getRef(),
                                                     serverConf.getProtocol(),
                                                     externalAddressAndPort,
                                                     externalUrl,
                                                     properties));
        }

        return servers;
    }

    /**
     * Gets the {@link ServerConfImpl} object associated with {@code portProtocol}.
     * The provided labels should have keys matching e.g.
     *
     * <p>{@code che:server:<portProtocol>:[ref|path|protocol]}
     *
     * @param portProtocol the port binding associated with the server
     * @param labels a map holding the relevant values for reference, protocol, and path
     *        for the given {@code portProtocol}
     * @param serverConfMap a map of {@link ServerConfImpl} with {@code portProtocol} as
     *        keys.
     * @return {@code ServerConfImpl}, obtained from {@code serverConfMap} if possible,
     *         or from {@code labels} if there is no entry in {@code serverConfMap}.
     */
    private ServerConfImpl getServerConfImpl(String portProtocol,
                                             Map<String, String> labels,
                                             Map<String, ServerConfImpl> serverConfMap) {
        // Label can be specified without protocol -- e.g. 4401 refers to 4401/tcp
        String port = portProtocol.substring(0, portProtocol.length() - 4);

        ServerConfImpl serverConf;
        // provided serverConf map takes precedence
        if (serverConfMap.get(portProtocol) != null) {
            serverConf = serverConfMap.get(portProtocol);
        } else if (serverConfMap.get(port) != null) {
            serverConf = serverConfMap.get(port);
        } else {
            String ref, protocol, path;

            ref = labels.get(String.format(SERVER_CONF_LABEL_REF_KEY, portProtocol));
            if (ref == null) {
                ref = labels.get(String.format(SERVER_CONF_LABEL_REF_KEY, port));
            }

            protocol = labels.get(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, portProtocol));
            if (protocol == null) {
                protocol = labels.get(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, port));
            }

            path = labels.get(String.format(SERVER_CONF_LABEL_PATH_KEY, portProtocol));
            if (path == null) {
                path = labels.get(String.format(SERVER_CONF_LABEL_PATH_KEY, port));
            }

            serverConf = new ServerConfImpl(ref, portProtocol, protocol, path);
        }

        if (serverConf.getRef() == null) {
            // Add default reference to server if it was not set above
            serverConf.setRef("Server-" + portProtocol.replace('/', '-'));
        }
        return serverConf;
    }
}
