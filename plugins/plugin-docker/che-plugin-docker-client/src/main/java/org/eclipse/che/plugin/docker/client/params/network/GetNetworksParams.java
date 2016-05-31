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

import org.eclipse.che.plugin.docker.client.json.Filters;

import java.util.Objects;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#getNetworks(GetNetworksParams)}.
 *
 * @author Alexander Garagatyi
 */
public class GetNetworksParams {

    private Filters filters;

    /**
     * Creates arguments holder.
     */
    public static GetNetworksParams create() {
        return new GetNetworksParams();
    }

    private GetNetworksParams() {}

    /**
     * Adds filters to this parameters.
     *
     * @param filters
     *         filter of needed networks. Available filters: {@code name=<string>},
     *         {@code id=<string>}, {@code type=<string>}
     * @return this params instance
     */
    public GetNetworksParams withFilters(Filters filters) {
        this.filters = filters;
        return this;
    }

    public Filters getFilters() {
        return filters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GetNetworksParams)) return false;
        GetNetworksParams that = (GetNetworksParams)o;
        return Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filters);
    }

    @Override
    public String toString() {
        return "GetNetworksParams{" +
               "filters=" + filters +
               '}';
    }
}
