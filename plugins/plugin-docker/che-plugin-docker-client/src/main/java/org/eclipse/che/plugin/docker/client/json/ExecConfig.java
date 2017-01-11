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
 * @author andrew00x
 */
public class ExecConfig {
    private boolean  attachStdin;
    private boolean  attachStdout;
    private boolean  attachStderr;
    private boolean  tty;
    private String[] cmd;

    public boolean isAttachStdin() {
        return attachStdin;
    }

    public void setAttachStdin(boolean attachStdin) {
        this.attachStdin = attachStdin;
    }

    public boolean isAttachStdout() {
        return attachStdout;
    }

    public void setAttachStdout(boolean attachStdout) {
        this.attachStdout = attachStdout;
    }

    public boolean isAttachStderr() {
        return attachStderr;
    }

    public void setAttachStderr(boolean attachStderr) {
        this.attachStderr = attachStderr;
    }

    public boolean isTty() {
        return tty;
    }

    public void setTty(boolean tty) {
        this.tty = tty;
    }

    public String[] getCmd() {
        return cmd;
    }

    public void setCmd(String[] cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return "ExecConfig{" +
               "attachStdin=" + attachStdin +
               ", attachStdout=" + attachStdout +
               ", attachStderr=" + attachStderr +
               ", tty=" + tty +
               ", cmd=" + Arrays.toString(cmd) +
               '}';
    }

    // -------------------

    public ExecConfig withAttachStdin(boolean attachStdin) {
        this.attachStdin = attachStdin;
        return this;
    }

    public ExecConfig withAttachStdout(boolean attachStdout) {
        this.attachStdout = attachStdout;
        return this;
    }

    public ExecConfig withAttachStderr(boolean attachStderr) {
        this.attachStderr = attachStderr;
        return this;
    }

    public ExecConfig withTty(boolean tty) {
        this.tty = tty;
        return this;
    }

    public ExecConfig withCmd(String[] cmd) {
        this.cmd = cmd;
        return this;
    }
}
