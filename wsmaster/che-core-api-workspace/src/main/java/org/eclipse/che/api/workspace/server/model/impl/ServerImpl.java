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
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;

import java.util.Objects;

/**
 * @author gazarenkov
 */
public class ServerImpl implements Server {

    private String       url;
    private ServerStatus status;

    public ServerImpl(String url) {
        this(url, ServerStatus.UNKNOWN);
    }

    public ServerImpl(String url, ServerStatus status) {
        this.url = url;
        this.status = status;
    }

    public ServerImpl(Server server) {
        this(server.getUrl(), server.getStatus());
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public ServerStatus getStatus() {
        return this.status;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return url;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Server)) {
            return false;
        }
        final Server that = (Server)obj;
        return Objects.equals(url, that.getUrl())
               && Objects.equals(status, that.getStatus());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(url);
        hash = 31 * hash + Objects.hashCode(status);
        return hash;
    }
}
