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

import org.eclipse.che.plugin.docker.client.json.network.NewNetwork;

import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#createNetwork(CreateNetworkParams)}.
 *
 * author Alexander Garagatyi
 */
public class CreateNetworkParams {
    // todo consider validation that network config has all required fields
    private NewNetwork network;

    private CreateNetworkParams() {}

    /**
     * Creates arguments holder with required parameters.
     *
     * @param newNetwork
     *         network configuration
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code newNetwork} is null
     */
    public static CreateNetworkParams create(@NotNull NewNetwork newNetwork) {
        return new CreateNetworkParams().withNetwork(newNetwork);
    }

    /**
     * Adds network name to this parameters.
     *
     * @param newNetwork
     *         network configuration
     * @return this params instance
     * @throws NullPointerException
     *         if {@code newNetwork} is null
     */
    public CreateNetworkParams withNetwork(@NotNull NewNetwork newNetwork) {
        requireNonNull(newNetwork);
        this.network = newNetwork;
        return this;
    }

    public NewNetwork getNetwork() {
        return network;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateNetworkParams)) return false;
        CreateNetworkParams that = (CreateNetworkParams)o;
        return Objects.equals(network, that.network);
    }

    @Override
    public int hashCode() {
        return Objects.hash(network);
    }

    @Override
    public String toString() {
        return "CreateNetworkParams{" +
               "network=" + network +
               '}';
    }
}
