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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.ServerConf;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@Embeddable
public class ServerConfImpl implements ServerConf {

    @Basic
    private String ref;

    @Basic
    private String port;

    @Basic
    private String protocol;

    @Basic
    private String path;

    public ServerConfImpl() {}

    public ServerConfImpl(String ref, String port, String protocol, String path) {
        this.ref = ref;
        this.port = port;
        this.protocol = protocol;
        this.path = path;
    }

    public ServerConfImpl(ServerConf serverConf) {
        this.ref = serverConf.getRef();
        this.port = serverConf.getPort();
        this.protocol = serverConf.getProtocol();
        this.path = serverConf.getPath();
    }

    @Override
    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerConfImpl)) return false;
        ServerConfImpl that = (ServerConfImpl)o;
        return Objects.equals(ref, that.ref) &&
               Objects.equals(port, that.port) &&
               Objects.equals(protocol, that.protocol) &&
               Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, port, protocol, path);
    }

    @Override
    public String toString() {
        return "ServerConfImpl{" +
               "ref='" + ref + '\'' +
               ", port='" + port + '\'' +
               ", protocol='" + protocol + '\'' +
               ", path='" + path + '\'' +
               '}';
    }
}
