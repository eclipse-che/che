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
package org.eclipse.che.api.environment.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.CompositeLineConsumer;
import org.eclipse.che.api.core.util.FileLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.WebsocketLineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Facade for Machine process operations.
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@Singleton
public class MachineProcessManager {
    private static final Logger LOG = LoggerFactory.getLogger(MachineProcessManager.class);

    private final File                 machineLogsDir;
    private final CheEnvironmentEngine environmentEngine;
    private final EventService         eventService;

    @VisibleForTesting
    final ExecutorService executor;

    @Inject
    public MachineProcessManager(@Named("che.workspace.logs") String machineLogsDir,
                                 EventService eventService,
                                 CheEnvironmentEngine environmentEngine) {
        this.eventService = eventService;
        this.machineLogsDir = new File(machineLogsDir);
        this.environmentEngine = environmentEngine;

        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MachineProcessManager-%d")
                                                                           .setDaemon(false)
                                                                           .build());
    }

    /**
     * Execute a command in machine
     *
     * @param machineId
     *         id of the machine where command should be executed
     * @param command
     *         command that should be executed in the machine
     * @return {@link org.eclipse.che.api.machine.server.spi.InstanceProcess} that represents started process in machine
     * @throws NotFoundException
     *         if machine with specified id not found
     * @throws BadRequestException
     *         if value of required parameter is invalid
     * @throws MachineException
     *         if other error occur
     */
    public InstanceProcess exec(String workspaceId,
                                String machineId,
                                Command command,
                                @Nullable String outputChannel)
            throws NotFoundException, MachineException, BadRequestException {
        requiredNotNull(machineId, "Machine ID is required");
        requiredNotNull(command, "Command is required");
        requiredNotNull(command.getCommandLine(), "Command line is required");
        requiredNotNull(command.getName(), "Command name is required");
        requiredNotNull(command.getType(), "Command type is required");

        final Instance machine = environmentEngine.getMachine(workspaceId, machineId);
        final InstanceProcess instanceProcess = machine.createProcess(command, outputChannel);
        final int pid = instanceProcess.getPid();

        final LineConsumer processLogger = getProcessLogger(machineId, pid, outputChannel);

        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            try {
                eventService.publish(newDto(MachineProcessEvent.class)
                                             .withEventType(MachineProcessEvent.EventType.STARTED)
                                             .withMachineId(machineId)
                                             .withProcessId(pid));

                instanceProcess.start(processLogger);

                eventService.publish(newDto(MachineProcessEvent.class)
                                             .withEventType(MachineProcessEvent.EventType.STOPPED)
                                             .withMachineId(machineId)
                                             .withProcessId(pid));
            } catch (ConflictException | MachineException error) {
                eventService.publish(newDto(MachineProcessEvent.class)
                                             .withEventType(MachineProcessEvent.EventType.ERROR)
                                             .withMachineId(machineId)
                                             .withProcessId(pid)
                                             .withError(error.getLocalizedMessage()));

                try {
                    processLogger.writeLine(String.format("[ERROR] %s", error.getMessage()));
                } catch (IOException ignored) {
                }
            } finally {
                try {
                    processLogger.close();
                } catch (IOException ignored) {
                }
            }
        }));
        return instanceProcess;
    }

    /**
     * Get list of active processes from specific machine
     *
     * @param machineId
     *         id of machine to get processes information from
     * @return list of {@link org.eclipse.che.api.machine.server.spi.InstanceProcess}
     * @throws NotFoundException
     *         if machine with specified id not found
     * @throws MachineException
     *         if other error occur
     */
    public List<InstanceProcess> getProcesses(String workspaceId, String machineId) throws NotFoundException, MachineException {
        return environmentEngine.getMachine(workspaceId, machineId).getProcesses();
    }

    /**
     * Stop process in machine
     *
     * @param machineId
     *         if of the machine where process should be stopped
     * @param pid
     *         id of the process that should be stopped in machine
     * @throws NotFoundException
     *         if machine or process with specified id not found
     * @throws ForbiddenException
     *         if process is finished already
     * @throws MachineException
     *         if other error occur
     */
    public void stopProcess(String workspaceId,
                            String machineId,
                            int pid) throws NotFoundException, MachineException, ForbiddenException {
        final InstanceProcess process = environmentEngine.getMachine(workspaceId, machineId).getProcess(pid);
        if (!process.isAlive()) {
            throw new ForbiddenException("Process finished already");
        }

        process.kill();

        eventService.publish(newDto(MachineProcessEvent.class)
                                     .withEventType(MachineProcessEvent.EventType.STOPPED)
                                     .withMachineId(machineId)
                                     .withProcessId(pid));
    }

    /**
     * Gets process reader from machine by specified id.
     *
     * @param machineId
     *         machine id whose process reader will be returned
     * @param pid
     *         process id
     * @return reader for specified process on machine
     * @throws NotFoundException
     *         if machine with specified id not found
     * @throws MachineException
     *         if other error occur
     */
    public Reader getProcessLogReader(String machineId, int pid) throws NotFoundException, MachineException {
        final File processLogsFile = getProcessLogsFile(machineId, pid);
        if (processLogsFile.isFile()) {
            try {
                return Files.newBufferedReader(processLogsFile.toPath(), Charset.defaultCharset());
            } catch (IOException e) {
                throw new MachineException(
                        String.format("Unable read log file for process '%s' of machine '%s'. %s", pid, machineId, e.getMessage()));
            }
        }
        throw new NotFoundException(String.format("Logs for process '%s' of machine '%s' are not available", pid, machineId));
    }

    private File getProcessLogsFile(String machineId, int pid) {
        return new File(new File(machineLogsDir, machineId), Integer.toString(pid));
    }

    private FileLineConsumer getProcessFileLogger(String machineId, int pid) throws MachineException {
        try {
            return new FileLineConsumer(getProcessLogsFile(machineId, pid));
        } catch (IOException e) {
            throw new MachineException(
                    String.format("Unable create log file for process '%s' of machine '%s'. %s", pid, machineId, e.getMessage()));
        }
    }

    @VisibleForTesting
    LineConsumer getProcessLogger(String machineId, int pid, String outputChannel) throws MachineException {
        return getLogger(getProcessFileLogger(machineId, pid), outputChannel);
    }

    private LineConsumer getLogger(LineConsumer fileLogger, String outputChannel) throws MachineException {
        if (outputChannel != null) {
            return new CompositeLineConsumer(fileLogger, new WebsocketLineConsumer(outputChannel));
        }
        return fileLogger;
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param message
     *         used as subject of exception message "{subject} required"
     * @throws org.eclipse.che.api.core.BadRequestException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String message) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(message + " required");
        }
    }

    @PreDestroy
    private void cleanup() {
        boolean interrupted = false;

        executor.shutdown();

        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOG.warn("Unable terminate main pool");
                }
            }
        } catch (InterruptedException e) {
            interrupted = true;
            executor.shutdownNow();
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
