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

import org.eclipse.che.api.core.model.machine.Server;

import java.util.Objects;

/**
 * Data object for {@link Server}.
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
public class ServerImpl implements Server {

    private String ref;
    private String address;
    private String url;

    public ServerImpl(String ref, String address, String url) {
        this.ref = ref;
        this.address = address;
        this.url = url;
    }

    public ServerImpl(Server server) {
        this(server.getRef(), server.getAddress(), server.getUrl());
    }

    @Override
    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerImpl)) return false;
        final ServerImpl other = (ServerImpl)o;
        return Objects.equals(ref, other.ref) &&
               Objects.equals(address, other.address) &&
               Objects.equals(url, other.url);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(ref);
        hash = hash * 31 + Objects.hashCode(address);
        hash = hash * 31 + Objects.hashCode(url);
        return hash;
    }

    @Override
    public String toString() {
        return "ServerImpl{" +
               "ref='" + ref + '\'' +
               ", address='" + address + '\'' +
               ", url='" + url + '\'' +
               '}';
    }
}
