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
package org.eclipse.che.plugin.machine.ssh.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.machine.ssh.SshProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * JSch implementation of {@link SshProcess}
 *
 * @author Alexander Garagatyi
 */
public class JschSshProcess implements SshProcess {
    private final ChannelExec exec;

    public JschSshProcess(ChannelExec exec) {
        this.exec = exec;
    }

    @Override
    public void start() throws MachineException {
        try {
            exec.connect();
        } catch (JSchException e) {
            throw new MachineException("Ssh machine command execution error:" + e.getLocalizedMessage());
        }
    }

    @Override
    public void start(LineConsumer output) throws MachineException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(exec.getErrStream()))) {

            exec.connect();

            String line;
            while ((line = reader.readLine()) != null) {
                // todo format output as it is done in docker impl
                // todo use async streams?
                // todo how to manage disconnections due to network failures?
                output.writeLine(line);
            }
            while ((line = errReader.readLine()) != null) {
                output.writeLine(line);
            }
        } catch (IOException | JSchException e) {
            throw new MachineException("Ssh machine command execution error:" + e.getLocalizedMessage());
        }
    }

    @Override
    public int getExitCode() {
        return exec.getExitStatus();
    }

    @Override
    public void kill() throws MachineException {
        exec.disconnect();
    }
}
