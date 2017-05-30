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
package org.eclipse.che.api.machine.server.spi;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;

import java.util.List;

/**
 * Representation of machine instance in implementation specific way.
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 */
public interface Instance extends Machine {

    void setStatus(MachineStatus status);

    LineConsumer getLogger();

    /**
     * Get {@link InstanceProcess} by its id
     *
     * @param pid
     *         id of the process
     * @throws NotFoundException
     *         if process with specified id is not found. Process can be finished already or doesn't exist.
     * @throws MachineException
     *         if any other error occurs
     */
    InstanceProcess getProcess(int pid) throws NotFoundException, MachineException;

    /**
     * Get list of all running processes in the instance
     *
     * @return list of running processes or empty list if no process is running
     * @throws MachineException
     *         if any error occurs on the processes list retrieving
     */
    List<InstanceProcess> getProcesses() throws MachineException;

    /**
     * Create process from command line.
     * Returned {@link InstanceProcess#getPid()} should return unique pid on this stage.
     * This pid allow to control process from clients and save process logs if needed.
     *
     * @param command
     *         command from which process should be created
     * @param outputChannel
     *         websocket chanel for execution logs
     * @return {@link InstanceProcess} with unique pid, that can't be used in future for other process of instance
     * @throws MachineException
     *         if error occurs on creating process
     */
    InstanceProcess createProcess(Command command, String outputChannel) throws MachineException;

    /**
     * Save state of the instance
     *
     * @return {@code InstanceSnapshotKey} that describe implementation specific keys of snapshot
     * @throws MachineException
     *         if error occurs on storing state of the instance
     */
    MachineSource saveToSnapshot() throws MachineException;

    /**
     * Destroy instance
     *
     * @throws MachineException
     *         if error occurs on instance destroying
     */
    void destroy() throws MachineException;

    /**
     * Returns {@link InstanceNode} that represents server where machine is launched
     */
    InstanceNode getNode();

    /**
     * Reads file content from machine by specified path.
     *
     * @param filePath
     *         path to file on machine instance
     * @param startFrom
     *         line number to start reading from
     * @param limit
     *         limitation on line
     * @return file content
     * @throws MachineException
     *         if any error occurs with file reading
     */
    String readFileContent(String filePath, int startFrom, int limit) throws MachineException;


    /**
     * Copies files from specified machine into current machine.
     *
     * @param sourceMachine
     *         source machine
     * @param sourcePath
     *         path to file or directory inside specified machine
     * @param targetPath
     *         path to destination file or directory inside machine
     * @param overwriteDirNonDir
     *         If "false" then it will be an error if unpacking the given content would cause
     *         an existing directory to be replaced with a non-directory and vice versa.
     * @throws MachineException
     *         if any error occurs when files are being copied
     */
    void copy(Instance sourceMachine, String sourcePath, String targetPath, boolean overwriteDirNonDir) throws MachineException;

    /**
     * Copies files from CHE server into current machine.
     *
     * @param sourcePath
     *         path to file or directory inside CHE server
     * @param targetPath
     *         path to destination file or directory inside machine
     * @throws MachineException
     *         if any error occurs when files are being copied
     */
    void copy(String sourcePath, String targetPath) throws MachineException;
}
