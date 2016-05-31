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
package org.eclipse.che.plugin.docker.client.params;

import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#removeNetwork(RemoveNetworkParams)}.
 *
 * author Alexander Garagatyi
 */
public class RemoveNetworkParams {
    private String netId;

    private RemoveNetworkParams() {}

    /**
     * Creates arguments holder with required parameters.
     *
     * @param netId
     *         network identifier
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code netId} is null
     */
    public static RemoveNetworkParams create(@NotNull String netId) {
        return new RemoveNetworkParams().withNetworkId(netId);
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
    public RemoveNetworkParams withNetworkId(@NotNull String netId) {
        requireNonNull(netId);
        this.netId = netId;
        return this;
    }

    public String getNetworkId() {
        return netId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemoveNetworkParams)) return false;
        RemoveNetworkParams that = (RemoveNetworkParams)o;
        return Objects.equals(netId, that.netId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(netId);
    }

    @Override
    public String toString() {
        return "RemoveNetworkParams{" +
               "netId='" + netId + '\'' +
               '}';
    }
}
