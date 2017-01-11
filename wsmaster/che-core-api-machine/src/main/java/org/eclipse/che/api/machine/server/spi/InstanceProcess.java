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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.core.model.machine.MachineProcess;

/**
 * Represents process in the machine created by command.
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public interface InstanceProcess extends MachineProcess {
    /**
     * Starts process in the background.
     *
     * @throws org.eclipse.che.api.core.ConflictException
     *         if process is started already
     * @throws MachineException
     *         if internal error occurs
     * @see #start()
     * @see #isAlive()
     */
    void start() throws ConflictException, MachineException;

    /**
     * Starts process.
     *
     * @param output
     *         consumer for process' output. If this parameter is {@code null} process started in the background. If this parameter is
     *         specified then this method is blocked until process is running.
     * @throws org.eclipse.che.api.core.ConflictException
     *         if process is started already
     * @throws MachineException
     *         if internal error occurs
     */
    void start(LineConsumer output) throws ConflictException, MachineException;

    /**
     * Kills this process.
     *
     * @throws MachineException
     *         if internal error occurs
     */
    void kill() throws MachineException;

    /**
     * Check if process is alive
     *
     * @throws NotFoundException
     *         if process is not found. It possible if it is finished, was killed.
     * @throws MachineException
     *         if internal error occurs
     */
    void checkAlive() throws NotFoundException, MachineException;
}
