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
package org.eclipse.che.ide.api.machine.execagent;

import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventWithPidDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.machine.ExecAgentEventManager;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;

/**
 * Implementation based on json rpc protocol calls
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class JsonRpcExecAgentEventManager implements ExecAgentEventManager {
    private final ProcessDiedEventHandler    processDiedEventHandler;
    private final ProcessStartedEventHandler processStartedEventHandler;
    private final ProcessStdErrEventHandler  processStdErrEventHandler;
    private final ProcessStdOutEventHandler  processStdOutEventHandler;

    @Inject
    public JsonRpcExecAgentEventManager(ProcessDiedEventHandler processDiedEventHandler,
                                        ProcessStartedEventHandler processStartedEventHandler,
                                        ProcessStdErrEventHandler processStdErrEventHandler,
                                        ProcessStdOutEventHandler processStdOutEventHandler) {
        this.processDiedEventHandler = processDiedEventHandler;
        this.processStartedEventHandler = processStartedEventHandler;
        this.processStdErrEventHandler = processStdErrEventHandler;
        this.processStdOutEventHandler = processStdOutEventHandler;
    }

    @Override
    public void registerProcessDiedOperation(int pid, Operation<ProcessDiedEventWithPidDto> operation) {
        Log.debug(getClass(), "Registering operation for process died event for PID: " + pid);
        processDiedEventHandler.registerOperation(pid, operation);
    }

    @Override
    public void registerProcessStartedOperation(int pid, Operation<ProcessStartedEventWithPidDto> operation) {
        Log.debug(getClass(), "Registering operation for process started event for PID: " + pid);
        processStartedEventHandler.registerOperation(pid, operation);
    }

    @Override
    public void registerProcessStdErrOperation(int pid, Operation<ProcessStdErrEventWithPidDto> operation) {
        Log.debug(getClass(), "Registering operation for process standard output event for PID: " + pid);
        processStdErrEventHandler.registerOperation(pid, operation);
    }

    @Override
    public void registerProcessStdOutOperation(int pid, Operation<ProcessStdOutEventWithPidDto> operation) {
        Log.debug(getClass(), "Registering operation for process error output event for PID: " + pid);
        processStdOutEventHandler.registerOperation(pid, operation);
    }
}
