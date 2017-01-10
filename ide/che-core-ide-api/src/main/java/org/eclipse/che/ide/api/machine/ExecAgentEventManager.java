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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;

/**
 * Manages registration of operations related to different kinds of even sent by exec agent
 *
 * @author Dmitry Kuleshov
 */
public interface ExecAgentEventManager {
    /**
     * Registers an operation that is performed when 'process died' event is received
     *
     * @param endpointId
     *         endpoint identifier
     * @param pid
     *         process identifier
     * @param operation
     *         operation to be performed
     */
    void registerProcessDiedOperation(String endpointId, int pid, Operation<ProcessDiedEventDto> operation);

    /**
     * Registers an operation that is performed when 'process started' event is received
     *
     * @param endpointId
     *         endpoint identifier
     * @param pid
     *         process identifier
     * @param operation
     *         operation to be performed
     */
    void registerProcessStartedOperation(String endpointId, int pid, Operation<ProcessStartedEventDto> operation);

    /**
     * Registers an operation that is performed when 'process standard error' event is received
     *
     * @param endpointId
     *         endpoint identifier
     * @param pid
     *         process identifier
     * @param operation
     *         operation to be performed
     */
    void registerProcessStdErrOperation(String endpointId, int pid, Operation<ProcessStdErrEventDto> operation);

    /**
     * Registers an operation that is performed when 'process standard output' event is received
     *
     * @param endpointId
     *         endpoint identifier
     * @param pid
     *         process identifier
     * @param operation
     *         operation to be performed
     */
    void registerProcessStdOutOperation(String endpointId, int pid, Operation<ProcessStdOutEventDto> operation);

    /**
     * Removes all registered event handler operations for the process associated with a PID
     *
     * @param endpointId
     *         endpoint identifier
     * @param pid
     *         process identifier
     */
    void cleanPidOperations(String endpointId, int pid);
}
