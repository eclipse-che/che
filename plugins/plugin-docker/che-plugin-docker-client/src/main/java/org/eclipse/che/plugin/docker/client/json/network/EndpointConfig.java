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
package org.eclipse.che.plugin.docker.client.json.network;

import java.util.List;
import java.util.Objects;

/**
 * Represents description of network needed to connect container into.
 *
 * author Alexander Garagatyi
 */
public class EndpointConfig {
    private NewIpamConfig iPAMConfig;
    private List<String>  links;
    private List<String>  aliases;

    public NewIpamConfig getIPAMConfig() {
        return iPAMConfig;
    }

    public void setIPAMConfig(NewIpamConfig iPAMConfig) {
        this.iPAMConfig = iPAMConfig;
    }

    public EndpointConfig withIPAMConfig(NewIpamConfig iPAMConfig) {
        this.iPAMConfig = iPAMConfig;
        return this;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public EndpointConfig withLinks(List<String> links) {
        this.links = links;
        return this;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public EndpointConfig withAliases(List<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndpointConfig)) return false;
        EndpointConfig that = (EndpointConfig)o;
        return Objects.equals(iPAMConfig, that.iPAMConfig) &&
               Objects.equals(links, that.links) &&
               Objects.equals(aliases, that.aliases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iPAMConfig, links, aliases);
    }

    @Override
    public String toString() {
        return "EndpointConfig{" +
               "iPAMConfig=" + iPAMConfig +
               ", links=" + links +
               ", aliases=" + aliases +
               '}';
    }
}
