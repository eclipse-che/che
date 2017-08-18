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
package org.eclipse.che.api.installer.server.model.impl;

import com.google.common.base.Objects;

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Anatolii Bazko
 */
@Entity(name = "InstallerServerConf")
@Table(name = "installer_servers")
public class InstallerServerConfigImpl implements ServerConfig {

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

    public InstallerServerConfigImpl() {}

    public InstallerServerConfigImpl(String port, String protocol, String path) {
        this.port = port;
        this.protocol = protocol;
        this.path = path;
    }

    public InstallerServerConfigImpl(ServerConfig serverConfig) {
        this.port = serverConfig.getPort();
        this.protocol = serverConfig.getProtocol();
        this.path = serverConfig.getPath();
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstallerServerConfigImpl)) return false;
        InstallerServerConfigImpl that = (InstallerServerConfigImpl)o;
        return Objects.equal(id, that.id) &&
               Objects.equal(port, that.port) &&
               Objects.equal(protocol, that.protocol) &&
               Objects.equal(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, port, protocol, path);
    }

    @Override
    public String toString() {
        return "InstallerServerConfigImpl{" +
               "id=" + id +
               ", port='" + port + '\'' +
               ", protocol='" + protocol + '\'' +
               ", path='" + path + '\'' +
               '}';
    }
}
