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

import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;

import java.util.Map;

/**
 * Represents the default server evaluation strategy. By default, calling
 * {@link ServerEvaluationStrategy#getServers(ContainerInfo, String, Map)} will return a completed
 * {@link ServerImpl} with internal and external address set to the address of the Docker host.
 *
 * <p>The addresses used for internal and external address can be overridden via the properties
 * {@code che.docker.ip} and {@code che.docker.ip.external}, respectively.
 *
 * @author Angel Misevski <amisevsk@redhat.com>
 * @author Alexander Garagatyi
 * @see ServerEvaluationStrategy
 */
public class DefaultServerEvaluationStrategy extends ServerEvaluationStrategy {

    /**
     * Used to store the address set by property {@code che.docker.ip}, if applicable.
     */
    protected String internalAddressProperty;

    /**
     * Used to store the address set by property {@code che.docker.ip.external}. if applicable.
     */
    protected String externalAddressProperty;

    @Inject
    public DefaultServerEvaluationStrategy(@Nullable @Named("che.docker.ip") String internalAddress,
                                           @Nullable @Named("che.docker.ip.external") String externalAddress) {
        this.internalAddressProperty = internalAddress;
        this.externalAddressProperty = externalAddress;
    }

    @Override
    protected Map<String, String> getInternalAddressesAndPorts(ContainerInfo containerInfo, String internalHost) {
        String internalAddress = internalAddressProperty != null ?
                                 internalAddressProperty :
                                 internalHost;

        return getExposedPortsToAddressPorts(internalAddress, containerInfo.getNetworkSettings().getPorts());
    }

    @Override
    protected Map<String, String> getExternalAddressesAndPorts(ContainerInfo containerInfo,
                                                               String internalHost) {
        String externalAddress = externalAddressProperty != null ?
                                 externalAddressProperty :
                                 internalAddressProperty != null ?
                                 internalAddressProperty :
                                 internalHost;

        return super.getExposedPortsToAddressPorts(externalAddress, containerInfo.getNetworkSettings().getPorts());
    }

    @Override
    protected boolean useHttpsForExternalUrls() {
        return false;
    }
}
