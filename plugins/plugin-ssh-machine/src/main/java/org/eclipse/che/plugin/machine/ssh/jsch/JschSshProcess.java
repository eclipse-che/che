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
package org.eclipse.che.plugin.machine.ssh.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.machine.ssh.SshProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    // todo how to manage disconnections due to network failures?

    @Override
    public void start(LineConsumer output) throws MachineException {
        try (PipedOutputStream pipedOS = new PipedOutputStream();
             PipedInputStream pipedIS = new PipedInputStream(pipedOS);
             BufferedReader outReader = new BufferedReader(new InputStreamReader(pipedIS))) {

            exec.setOutputStream(pipedOS);
            exec.setExtOutputStream(pipedOS);
            exec.connect();

            String outLine;
            while ((outLine = outReader.readLine()) != null) {
                output.writeLine(outLine);
            }
        } catch (IOException | JSchException e) {
            throw new MachineException("Ssh machine command execution error:" + e.getLocalizedMessage());
        } finally {
            exec.disconnect();
        }
    }

    @Override
    public void start(LineConsumer out, LineConsumer err) throws MachineException {
        try (BufferedReader outReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(exec.getErrStream()))) {

            exec.connect();

            // read stderr in separate thread
            CompletableFuture<Optional<IOException>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String line;
                    while ((line = errReader.readLine()) != null) {
                        err.writeLine(line);
                    }
                    return Optional.empty();
                } catch (IOException e) {
                    return Optional.of(e);
                }
            });

            String line;
            while ((line = outReader.readLine()) != null) {
                out.writeLine(line);
            }

            final Optional<IOException> excOptional = future.get();
            if (excOptional.isPresent()) {
                throw new MachineException("Ssh machine command execution error:" + excOptional.get().getLocalizedMessage());
            }
        } catch (IOException | JSchException | ExecutionException | InterruptedException e) {
            throw new MachineException("Ssh machine command execution error:" + e.getLocalizedMessage());
        } finally {
            exec.disconnect();
        }
    }

    @Override
    public int getExitCode() {
        return exec.getExitStatus();
    }

    @Override
    public void kill() throws MachineException {
        try {
            exec.sendSignal("KILL");
        } catch (Exception e) {
            throw new MachineException("Ssh machine signal sending error:" + e.getLocalizedMessage());
        } finally {
            exec.disconnect();
        }
    }
}
