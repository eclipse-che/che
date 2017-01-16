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
package org.eclipse.che.api.debug.shared.model.impl;

import org.eclipse.che.api.debug.shared.model.DebuggerInfo;

/**
 * Summary of debugger information.
 *
 * @author Anatoliy Bazko
 */
public class DebuggerInfoImpl implements DebuggerInfo {
    private final String host;
    private final int    port;
    private final String name;
    private final String version;
    private final int    pid;
    private final String file;

    public DebuggerInfoImpl(String host, int port, String name, String version, int pid, String file) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.version = version;
        this.pid = pid;
        this.file = file;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public String getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DebuggerInfoImpl)) return false;

        DebuggerInfoImpl that = (DebuggerInfoImpl)o;

        if (port != that.port) return false;
        if (pid != that.pid) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        return !(file != null ? !file.equals(that.file) : that.file != null);

    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + pid;
        result = 31 * result + (file != null ? file.hashCode() : 0);
        return result;
    }
}
