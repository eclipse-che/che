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
package org.eclipse.che.plugin.docker.client.json.network;

import java.util.Objects;

/**
 * @author Alexander Garagatyi
 */
public class NewIpamConfig {
    private String iPv4Address;
    private String iPv6Address;

    public String getIPv4Address() {
        return iPv4Address;
    }

    public void setIPv4Address(String iPv4Address) {
        this.iPv4Address = iPv4Address;
    }

    public NewIpamConfig withIPv4Address(String iPv4Address) {
        this.iPv4Address = iPv4Address;
        return this;
    }

    public String getIPv6Address() {
        return iPv6Address;
    }

    public void setIPv6Address(String iPv6Address) {
        this.iPv6Address = iPv6Address;
    }

    public NewIpamConfig withIPv6Address(String iPv6Address) {
        this.iPv6Address = iPv6Address;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NewIpamConfig)) {
            return false;
        }
        final NewIpamConfig that = (NewIpamConfig)obj;
        return Objects.equals(iPv4Address, that.iPv4Address)
               && Objects.equals(iPv6Address, that.iPv6Address);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(iPv4Address);
        hash = 31 * hash + Objects.hashCode(iPv6Address);
        return hash;
    }

    @Override
    public String toString() {
        return "NewIpamConfig{" +
               "iPv4Address='" + iPv4Address + '\'' +
               ", iPv6Address='" + iPv6Address + '\'' +
               '}';
    }
}
