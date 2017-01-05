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

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.server.spi.impl.AbstractMachineProcess;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;

import java.io.IOException;

import static java.lang.String.format;

/**
 * Ssh machine implementation of {@link InstanceProcess}
 *
 * @author Alexander Garagatyi
 */
public class SshMachineProcess extends AbstractMachineProcess implements InstanceProcess {
    private final String    commandLine;
    private final SshClient sshClient;

    private volatile boolean started;

    private SshProcess sshProcess;

    @Inject
    public SshMachineProcess(@Assisted Command command,
                             @Nullable @Assisted("outputChannel") String outputChannel,
                             @Assisted int pid,
                             @Assisted SshClient sshClient) {
        super(command, pid, outputChannel);
        this.sshClient = sshClient;
        this.commandLine = command.getCommandLine();
        this.started = false;
    }

    @Override
    public boolean isAlive() {
        if (!started) {
            return false;
        }
        try {
            checkAlive();
            return true;
        } catch (MachineException | NotFoundException e) {
            // when process is not found (may be finished or killed)
            // when ssh is not accessible or responds in an unexpected way
            return false;
        }
    }

    @Override
    public void start() throws ConflictException, MachineException {
        start(null);
    }

    @Override
    public void start(LineConsumer output) throws ConflictException, MachineException {
        if (started) {
            throw new ConflictException("Process already started.");
        }

        sshProcess = sshClient.createProcess(commandLine);

        started = true;

        if (output == null) {
            sshProcess.start();
        } else {
            sshProcess.start(new PrefixingLineConsumer("[STDOUT] ", output),
                             new PrefixingLineConsumer("[STDERR] ", output));
        }
    }

    @Override
    public void checkAlive() throws MachineException, NotFoundException {
        if (!started) {
            throw new NotFoundException("Process is not started yet");
        }

        if (sshProcess.getExitCode() != -1) {
            throw new NotFoundException(format("Process with pid %s not found", getPid()));
        }
    }

    @Override
    public void kill() throws MachineException {
        sshProcess.kill();
    }

    private static class PrefixingLineConsumer implements LineConsumer {
        private final String       prefix;
        private final LineConsumer lineConsumer;

        public PrefixingLineConsumer(String prefix, LineConsumer lineConsumer) {
            this.prefix = prefix;
            this.lineConsumer = lineConsumer;
        }


        @Override
        public void writeLine(String line) throws IOException {
            lineConsumer.writeLine(prefix + line);
        }

        @Override
        public void close() throws IOException {
            lineConsumer.close();
        }
    }
}
