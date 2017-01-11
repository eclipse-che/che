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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.PortBinding;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents a server evaluation strategy for the configuration where the workspace server and
 * workspace containers are running on the same Docker network. Calling
 * {@link ServerEvaluationStrategy#getServers(ContainerInfo, String, Map)} will return a completed
 * {@link ServerImpl} with internal addresses set to the container's address within the internal
 * docker network. If the server cannot directly ping the container, communication will fail.
 *
 * <p>The addresses used for external address can be overridden via the property {@code che.docker.ip.external}.
 *
 * @author Angel Misevski <amisevsk@redhat.com>
 * @see ServerEvaluationStrategy
 */
public class LocalDockerServerEvaluationStrategy extends ServerEvaluationStrategy {

    /**
     * Used to store the address set by property {@code che.docker.ip}, if applicable.
     */
    protected String internalAddressProperty;

    /**
     * Used to store the address set by property {@code che.docker.ip.external}. if applicable.
     */
    protected String externalAddressProperty;

    @Inject
    public LocalDockerServerEvaluationStrategy (@Nullable @Named("che.docker.ip") String internalAddress,
                                                @Nullable @Named("che.docker.ip.external") String externalAddress) {
        this.internalAddressProperty = internalAddress;
        this.externalAddressProperty = externalAddress;
    }

    @Override
    protected Map<String, String> getInternalAddressesAndPorts(ContainerInfo containerInfo, String internalHost) {
        String internalAddressContainer = containerInfo.getNetworkSettings().getIpAddress();

        String internalAddress;
        boolean useExposedPorts = true;
        if (!isNullOrEmpty(internalAddressContainer)) {
            internalAddress = internalAddressContainer;
        } else {
            internalAddress = internalHost;
            useExposedPorts = false;
        }

        Map<String, List<PortBinding>> portBindings = containerInfo.getNetworkSettings().getPorts();

        Map<String, String> addressesAndPorts = new HashMap<>();
        for (Map.Entry<String, List<PortBinding>> portEntry : portBindings.entrySet()) {
            String exposedPort = portEntry.getKey().split("/")[0];
            String ephemeralPort = portEntry.getValue().get(0).getHostPort();
            if (useExposedPorts) {
                addressesAndPorts.put(portEntry.getKey(), internalAddress + ":" + exposedPort);
            } else {
                addressesAndPorts.put(portEntry.getKey(), internalAddress + ":" + ephemeralPort);
            }
        }
        return addressesAndPorts;
    }

    @Override
    protected Map<String, String> getExternalAddressesAndPorts(ContainerInfo containerInfo, String internalHost) {
        String externalAddressContainer = containerInfo.getNetworkSettings().getGateway();

        String externalAddress = externalAddressProperty != null ?
                                 externalAddressProperty :
                                 !isNullOrEmpty(externalAddressContainer) ?
                                 externalAddressContainer :
                                 internalHost;

        Map<String, List<PortBinding>> portBindings = containerInfo.getNetworkSettings().getPorts();

        Map<String, String> addressesAndPorts = new HashMap<>();
        for (String portKey : portBindings.keySet()) {
            String port = portBindings.get(portKey).get(0).getHostPort();
            addressesAndPorts.put(portKey, externalAddress + ":" + port);
        }
        return addressesAndPorts;
    }
}
