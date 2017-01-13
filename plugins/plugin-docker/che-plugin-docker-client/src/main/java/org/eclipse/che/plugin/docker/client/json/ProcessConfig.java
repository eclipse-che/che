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
package org.eclipse.che.plugin.docker.client.json;

import java.util.Arrays;

/**
 * @author Eugene Voevodin
 */
public class ProcessConfig {

    private String[] arguments;
    private String   entrypoint;
    private String   user;
    private boolean  tty;
    private boolean  privileged;

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public void setEntrypoint(String entrypoint) {
        this.entrypoint = entrypoint;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isTty() {
        return tty;
    }

    public void setTty(boolean tty) {
        this.tty = tty;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    @Override
    public String toString() {
        return "ProcessConfig{" +
               "arguments=" + Arrays.toString(arguments) +
               ", entrypoint='" + entrypoint + '\'' +
               ", user='" + user + '\'' +
               ", tty=" + tty +
               ", privileged=" + privileged +
               '}';
    }
}
