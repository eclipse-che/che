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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventWithPidDto;
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
     * @param pid process identifier
     * @param operation operation to be performed
     */
    void registerProcessDiedOperation(int pid, Operation<ProcessDiedEventWithPidDto> operation);

    /**
     * Registers an operation that is performed when 'process started' event is received
     *
     * @param pid process identifier
     * @param operation operation to be performed
     */
    void registerProcessStartedOperation(int pid, Operation<ProcessStartedEventWithPidDto> operation);

    /**
     * Registers an operation that is performed when 'process standard error' event is received
     *
     * @param pid process identifier
     * @param operation operation to be performed
     */
    void registerProcessStdErrOperation(int pid, Operation<ProcessStdErrEventWithPidDto> operation);

    /**
     * Registers an operation that is performed when 'process standard output' event is received
     *
     * @param pid process identifier
     * @param operation operation to be performed
     */
    void registerProcessStdOutOperation(int pid, Operation<ProcessStdOutEventWithPidDto> operation);
}
