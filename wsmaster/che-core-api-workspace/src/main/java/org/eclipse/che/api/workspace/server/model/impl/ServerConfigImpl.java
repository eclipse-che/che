/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author Alexander Garagatyi
 */
@Entity(name = "ServerConf")
@Table(name = "serverconf")
public class ServerConfigImpl implements ServerConfig {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "port")
    private String port;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "path")
    private String path;

    public ServerConfigImpl() {}

    public ServerConfigImpl(String port,
                            String protocol,
                            String path) {
        this.port = port;
        this.protocol = protocol;
        this.path = path;
    }

    public ServerConfigImpl(ServerConfig serverConf) {
        this.port = serverConf.getPort();
        this.protocol = serverConf.getProtocol();
        this.path = serverConf.getPath();
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServerConfigImpl)) {
            return false;
        }
        final ServerConfigImpl that = (ServerConfigImpl)obj;
        return Objects.equals(id, that.id)
               && Objects.equals(port, that.port)
               && Objects.equals(protocol, that.protocol)
               && getPath().equals(that.getPath());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(port);
        hash = 31 * hash + Objects.hashCode(protocol);
        hash = 31 * hash + Objects.hashCode(path);
        return hash;
    }

    @Override
    public String toString() {
        return "ServerConfigImpl{" +
               "id=" + id +
               ", port='" + port + '\'' +
               ", protocol='" + protocol + '\'' +
               ", path=" + path +
               '}';
    }
}
