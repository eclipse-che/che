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
package org.eclipse.che.plugin.machine.ssh;

import java.util.Objects;

/**
 * Recipe of connection to ssh machine using ssh protocol.
 *
 * @author Alexander Garagatyi
 */
public class SshMachineRecipe {

    private final String  host;
    private final Integer port;
    private final String  username;
    private final String  password;

    public SshMachineRecipe(String host,
                            Integer port,
                            String username,
                            String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SshMachineRecipe)) return false;
        SshMachineRecipe that = (SshMachineRecipe)o;
        return Objects.equals(host, that.host) &&
               Objects.equals(port, that.port) &&
               Objects.equals(username, that.username) &&
               Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, username, password);
    }

    @Override
    public String toString() {
        return "SshMachineRecipe{" +
               "host='" + host + '\'' +
               ", port=" + port +
               ", username='" + username + '\'' +
               ", password='" + password + '\'' +
               '}';
    }
}
