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

import org.eclipse.che.api.core.model.machine.ServerConf;

import java.util.Objects;

/**
 * @author Alexander Garagatyi
 */
public class ServerConfImpl implements ServerConf {
    private final String ref;
    private final String port;
    private final String protocol;

    public ServerConfImpl(String ref, String port, String protocol) {
        this.ref = ref;
        this.port = port;
        this.protocol = protocol;
    }

    public ServerConfImpl(ServerConf serverConf) {
        this.ref = serverConf.getRef();
        this.port = serverConf.getPort();
        this.protocol = serverConf.getProtocol();
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerConfImpl)) return false;
        ServerConfImpl that = (ServerConfImpl)o;
        return Objects.equals(ref, that.ref) &&
               Objects.equals(port, that.port) &&
               Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, port, protocol);
    }

    @Override
    public String toString() {
        return "ServerConfImpl{" +
               "ref='" + ref + '\'' +
               ", port='" + port + '\'' +
               ", protocol='" + protocol + '\'' +
               '}';
    }
}
