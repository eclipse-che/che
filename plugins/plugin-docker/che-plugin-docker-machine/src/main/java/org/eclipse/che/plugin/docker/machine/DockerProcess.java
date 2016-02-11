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
package org.eclipse.che.plugin.docker.machine;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;

import javax.inject.Inject;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Map;

import static java.lang.String.format;

/**
 * Docker implementation of {@link InstanceProcess}
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public class DockerProcess implements InstanceProcess {
    private final DockerConnector     docker;
    private final String              container;
    private final String              pidFilePath;
    private final int                 pid;
    private final String              commandLine;
    private final String              commandName;
    private final String              commandType;
    private final Map<String, String> attributes;
    private final String              outputChannel;

    private volatile boolean started;

    @Inject
    public DockerProcess(DockerConnector docker,
                         @Assisted Command command,
                         @Assisted("container") String container,
                         @Assisted("outputChannel") String outputChannel,
                         @Assisted("pid_file_path") String pidFilePath,
                         @Assisted int pid) {
        this.docker = docker;
        this.container = container;
        this.commandLine = command.getCommandLine();
        this.commandName = command.getName();
        this.commandType = command.getType();
        this.attributes = command.getAttributes();
        this.outputChannel = outputChannel;
        this.pidFilePath = pidFilePath;
        this.pid = pid;
        this.started = false;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public String getName() {
        return commandName;
    }

    @Override
    public String getCommandLine() {
        return commandLine;
    }

    @Override
    public String getType() {
        return commandType;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String getOutputChannel() {
        return outputChannel;
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
            // when process is not running yet
            // when docker is not accessible or responds in an unexpected way - should never happen
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
        // Trap is invoked when bash session ends. Here we kill all sub-processes of shell and remove pid-file.
        final String trap = format("trap '[ -z \"$(jobs -p)\" ] || kill $(jobs -p); [ -e %1$s ] && rm %1$s' EXIT", pidFilePath);
        // 'echo' saves shell pid in file, then run command
        final String shellCommand = trap + "; echo $$>" + pidFilePath + "; " + commandLine;
        final String[] command = {"/bin/bash", "-c", shellCommand};
        Exec exec;
        try {
            exec = docker.createExec(container, output == null, command);
        } catch (IOException e) {
            throw new MachineException(format("Error occurs while initializing command %s in docker container %s: %s",
                                              Arrays.toString(command), container, e.getMessage()), e);
        }
        started = true;
        try {
            docker.startExec(exec.getId(), output == null ? null : new LogMessagePrinter(output));
        } catch (IOException e) {
            if (output != null && e instanceof SocketTimeoutException) {
                throw new MachineException(getErrorMessage());
            } else {
                throw new MachineException(format("Error occurs while executing command %s: %s",
                                                  Arrays.toString(exec.getCommand()), e.getMessage()), e);
            }
        }
    }

    @Override
    public void checkAlive() throws MachineException, NotFoundException {
        // Read pid from file and run 'kill -0 [pid]' command.
        final String isAliveCmd = format("[ -r %1$s ] && kill -0 $(cat %1$s) || echo 'Unable read PID file'", pidFilePath);
        final ListLineConsumer output = new ListLineConsumer();
        final String[] command = {"/bin/bash", "-c", isAliveCmd};
        Exec exec;
        try {
            exec = docker.createExec(container, false, command);
        } catch (IOException e) {
            throw new MachineException(format("Error occurs while initializing command %s in docker container %s: %s",
                                              Arrays.toString(command), container, e.getMessage()), e);
        }
        try {
            docker.startExec(exec.getId(), new LogMessagePrinter(output));
        } catch (IOException e) {
            throw new MachineException(format("Error occurs while executing command %s in docker container %s: %s",
                                              Arrays.toString(exec.getCommand()), container, e.getMessage()), e);
        }
        // 'kill -0 [pid]' is silent if process is running or print "No such process" message otherwise
        if (!output.getText().isEmpty()) {
            throw new NotFoundException(format("Process with pid %s not found", pid));
        }
    }

    @Override
    public void kill() throws MachineException {
        if (started) {
            // Read pid from file and run 'kill [pid]' command.
            final String killCmd = format("[ -r %1$s ] && kill $(cat %1$s)", pidFilePath);
            final String[] command = {"/bin/bash", "-c", killCmd};
            Exec exec;
            try {
                exec = docker.createExec(container, true, command);
            } catch (IOException e) {
                throw new MachineException(format("Error occurs while initializing command %s in docker container %s: %s",
                                                  Arrays.toString(command), container, e.getMessage()), e);
            }
            try {
                docker.startExec(exec.getId(), MessageProcessor.DEV_NULL);
            } catch (IOException e) {
                throw new MachineException(format("Error occurs while executing command %s in docker container %s: %s",
                                                  Arrays.toString(exec.getCommand()), container, e.getMessage()), e);
            }
        }
    }

    private String getErrorMessage() {
        final StringBuilder errorMessage = new StringBuilder("Command output read timeout is reached.");
        try {
            // check if process is alive
            final Exec checkProcessExec =
                    docker.createExec(container,
                                      false,
                                      "/bin/bash",
                                      "-c",
                                      format("if kill -0 $(cat %1$s 2>/dev/null) 2>/dev/null; then cat %1$s; fi", pidFilePath));
            ValueHolder<String> pidHolder = new ValueHolder<>();
            docker.startExec(checkProcessExec.getId(), message -> {
                if (message.getType() == LogMessage.Type.STDOUT) {
                    pidHolder.set(message.getContent());
                }
            });
            if (pidHolder.get() != null) {
                errorMessage.append(" Process is still running and has id ").append(pidHolder.get()).append(" inside machine");
            }
        } catch (IOException ignore) {
        }
        return errorMessage.toString();
    }
}
