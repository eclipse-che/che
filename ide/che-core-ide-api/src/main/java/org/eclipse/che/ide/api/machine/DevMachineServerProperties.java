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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.core.model.machine.ServerProperties;

import java.util.Objects;

/**
 * Describe development machine server instance.
 *
 * @link ServerProperties
 * @author Mario Loriedo
 */
public class DevMachineServerProperties implements ServerProperties {

    private final String path;
    private final String internalAddress;
    private final String internalUrl;

    public DevMachineServerProperties(ServerProperties properties) {
        path = properties.getPath();
        internalAddress = properties.getInternalAddress();
        internalUrl = properties.getInternalUrl();
    }

    @Override
    public String getInternalAddress() {
        return internalAddress;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getInternalUrl() {
        return internalUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DevMachineServerProperties)) return false;
        final DevMachineServerProperties other = (DevMachineServerProperties)o;

        return Objects.equals(path, other.path) &&
                       Objects.equals(internalAddress, other.internalAddress) &&
                       Objects.equals(internalUrl, other.internalUrl);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(path);
        hash = hash * 31 + Objects.hashCode(internalAddress);
        hash = hash * 31 + Objects.hashCode(internalUrl);
        return hash;
    }

    @Override
    public String toString() {
        return "DevMachineServerProperties{" +
                       "path='" + path + '\'' +
                       ", internalAddress='" + internalAddress + '\'' +
                       ", internalUrl='" + internalUrl + '\'' +
                       '}';
    }

}
