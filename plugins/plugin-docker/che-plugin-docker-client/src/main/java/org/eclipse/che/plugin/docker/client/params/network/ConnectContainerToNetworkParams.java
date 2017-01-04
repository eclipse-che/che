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
package org.eclipse.che.plugin.docker.client.params.network;

import org.eclipse.che.plugin.docker.client.json.network.ConnectContainer;

import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#connectContainerToNetwork(ConnectContainerToNetworkParams)}.
 *
 * @author Alexander Garagatyi
 */
public class ConnectContainerToNetworkParams {
    private String           netId;
    private ConnectContainer connectContainer;

    private ConnectContainerToNetworkParams() {}

    /**
     * Creates arguments holder with required parameters.
     *
     * @param netId
     *         network identifier
     * @param connectContainer
     *         container connection configuration
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code netId} or {@code container} is null
     */
    public static ConnectContainerToNetworkParams create(@NotNull String netId, @NotNull ConnectContainer connectContainer) {
        return new ConnectContainerToNetworkParams().withNetworkId(netId)
                                                    .withConnectContainer(connectContainer);
    }

    /**
     * Adds network identifier to this parameters.
     *
     * @param netId
     *         network identifier
     * @return this params instance
     * @throws NullPointerException
     *         if {@code netId} is null
     */
    public ConnectContainerToNetworkParams withNetworkId(@NotNull String netId) {
        requireNonNull(netId);
        this.netId = netId;
        return this;
    }

    /**
     * Adds container identifier to this parameters.
     *
     * @param connectContainer
     *         container connection configuration
     * @return this params instance
     * @throws NullPointerException
     *         if {@code connectContainer} is null
     */
    public ConnectContainerToNetworkParams withConnectContainer(@NotNull ConnectContainer connectContainer) {
        requireNonNull(connectContainer);
        this.connectContainer = connectContainer;
        return this;
    }

    public String getNetworkId() {
        return netId;
    }

    public ConnectContainer getConnectContainer() {
        return connectContainer;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConnectContainerToNetworkParams)) {
            return false;
        }
        final ConnectContainerToNetworkParams that = (ConnectContainerToNetworkParams)obj;
        return Objects.equals(netId, that.netId)
               && Objects.equals(connectContainer, that.connectContainer);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(netId);
        hash = 31 * hash + Objects.hashCode(connectContainer);
        return hash;
    }

    @Override
    public String toString() {
        return "ConnectContainerToNetworkParams{" +
               "netId='" + netId + '\'' +
               ", connectContainer=" + connectContainer +
               '}';
    }
}
