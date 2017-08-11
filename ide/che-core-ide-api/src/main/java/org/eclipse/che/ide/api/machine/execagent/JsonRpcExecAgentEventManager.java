/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.machine.execagent;

import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.ide.api.machine.ExecAgentEventManager;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import java.util.function.Consumer;

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
    public void registerProcessDiedConsumer(String endpointId, int pid, Consumer<ProcessDiedEventDto> consumer) {
        Log.debug(getClass(), "Registering consumer for process died event for PID: " + pid);
        processDiedEventHandler.registerConsumer(endpointId, pid, consumer);
    }

    @Override
    public void registerProcessStartedConsumer(String endpointId, int pid, Consumer<ProcessStartedEventDto> consumer) {
        Log.debug(getClass(), "Registering consumer for process started event for PID: " + pid);
        processStartedEventHandler.registerConsumer(endpointId, pid, consumer);
    }

    @Override
    public void registerProcessStdErrConsumer(String endpointId, int pid, Consumer<ProcessStdErrEventDto> consumer) {
        Log.debug(getClass(), "Registering consumer for process standard output event for PID: " + pid);
        processStdErrEventHandler.registerConsumer(endpointId, pid, consumer);
    }

    @Override
    public void registerProcessStdOutConsumer(String endpointId, int pid, Consumer<ProcessStdOutEventDto> consumer) {
        Log.debug(getClass(), "Registering consumer for process error output event for PID: " + pid);
        processStdOutEventHandler.registerConsumer(endpointId, pid, consumer);
    }

    @Override
    public void cleanPidConsumer(String endpointId, int pid) {
        processDiedEventHandler.unregisterConsumers(endpointId, pid);
        processStartedEventHandler.unregisterConsumers(endpointId, pid);
        processStdErrEventHandler.unregisterConsumers(endpointId, pid);
        processStdOutEventHandler.unregisterConsumers(endpointId, pid);
    }
}
