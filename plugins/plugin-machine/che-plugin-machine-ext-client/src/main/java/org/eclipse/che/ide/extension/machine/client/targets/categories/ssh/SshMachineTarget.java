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
package org.eclipse.che.ide.extension.machine.client.targets.categories.ssh;

import org.eclipse.che.ide.extension.machine.client.targets.BaseTarget;
import org.eclipse.che.ide.extension.machine.client.targets.Target;

import java.util.Objects;

/**
 * The implementation of {@link Target}.
 *
 * @author Vitaliy Guliy
 * @author Oleksii Orel
 */
public class SshMachineTarget extends BaseTarget {
    private String host;
    private String port;
    private String userName;
    private String password;


    /** Returns ssh host. */
    public String getHost() {
        return host;
    }

    /**
     * Sets SSH host.
     *
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /** Returns ssh port. */
    public String getPort() {
        return port;
    }

    /**
     * Sets SSH port.
     *
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /** Returns user name. */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets SSH userName.
     *
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** Returns user password. */
    public String getPassword() {
        return password;
    }

    /**
     * Sets SSH password.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof SshMachineTarget)) {
            return false;
        }

        SshMachineTarget other = (SshMachineTarget)o;

        return Objects.equals(getName(), other.getName())
               && Objects.equals(getCategory(), other.getCategory())
               && Objects.equals(getRecipe(), other.getRecipe())
               && Objects.equals(getHost(), other.getHost())
               && Objects.equals(getPort(), other.getPort())
               && Objects.equals(getUserName(), other.getUserName())
               && Objects.equals(getPassword(), other.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCategory(), getRecipe(), getHost(), getPort(), getUserName(), getPassword());
    }
}
