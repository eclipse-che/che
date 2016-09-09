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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.ServerProperties;

import java.util.Objects;

/**
 * Data object for {@link ServerProperties}.
 *
 * @author Mario Loriedo
 */
public class ServerPropertiesImpl implements ServerProperties {

    private String               path;
    private String               internalAddress;
    private String               internalUrl;

    public ServerPropertiesImpl(String path, String internalAddress, String internalUrl) {
        this.internalAddress = internalAddress;
        this.internalUrl = internalUrl;
        this.path = path;
    }

    public ServerPropertiesImpl(ServerProperties properties) {
        this(properties.getPath(), properties.getInternalAddress(), properties.getInternalUrl());
    }


    @Override
    public String getPath() { return path; }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getInternalAddress() { return internalAddress; }

    public void setInternalAddress(String internalAddress) {
        this.internalAddress = internalAddress;
    }

    @Override
    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerPropertiesImpl)) return false;
        final ServerPropertiesImpl other = (ServerPropertiesImpl)o;

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
        return "ServerImpl{" +
                       "path='" + path + '\'' +
                       ", internalAddress='" + internalAddress + '\'' +
                       ", internalUrl='" + internalUrl + '\'' +
                       '}';
    }
}
