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
package org.eclipse.che.plugin.docker.client.params.network;

import org.eclipse.che.plugin.docker.client.json.network.DisconnectContainer;

import javax.validation.constraints.NotNull;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#disconnectContainerFromNetwork(DisconnectContainerFromNetworkParams)}.
 *
 * author Alexander Garagatyi
 */
public class DisconnectContainerFromNetworkParams {
    private String              netId;
    private DisconnectContainer disconnectContainer;

    private DisconnectContainerFromNetworkParams() {}

    /**
     * Creates arguments holder with required parameters.
     *
     * @param netId
     *         network identifier
     * @param disconnectContainer
     *         container disconnection configuration
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code netId} or {@code disconnectContainer} is null
     */
    public static DisconnectContainerFromNetworkParams create(@NotNull String netId, @NotNull DisconnectContainer disconnectContainer) {
        return new DisconnectContainerFromNetworkParams().withNetworkId(netId)
                                                         .withDisconnectContainer(disconnectContainer);
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
    public DisconnectContainerFromNetworkParams withNetworkId(@NotNull String netId) {
        requireNonNull(netId);
        this.netId = netId;
        return this;
    }

    public String getNetworkId() {
        return netId;
    }

    /**
     * Adds container identifier to this parameters.
     *
     * @param disconnectContainer
     *         container disconnection configuration
     * @return this params instance
     * @throws NullPointerException
     *         if {@code disconnectContainer} is null
     */
    public DisconnectContainerFromNetworkParams withDisconnectContainer(@NotNull DisconnectContainer disconnectContainer) {
        requireNonNull(disconnectContainer);
        this.disconnectContainer = disconnectContainer;
        return this;
    }

    public DisconnectContainer getDisconnectContainer() {
        return disconnectContainer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisconnectContainerFromNetworkParams)) return false;
        DisconnectContainerFromNetworkParams that = (DisconnectContainerFromNetworkParams)o;
        return Objects.equals(netId, that.netId) &&
               Objects.equals(disconnectContainer, that.disconnectContainer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(netId, disconnectContainer);
    }

    @Override
    public String toString() {
        return "DisconnectContainerFromNetworkParams{" +
               "netId='" + netId + '\'' +
               ", disconnectContainer=" + disconnectContainer +
               '}';
    }
}
