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
package org.eclipse.che.ide.api.machine.execagent;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessLogsRequestDto;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessLogsResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessRequestDto;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesRequestDto;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessKillRequestDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessKillResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessStartRequestDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessStartResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessSubscribeRequestDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessSubscribeResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessUnSubscribeRequestDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessUnSubscribeResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.UpdateSubscriptionRequestDto;
import org.eclipse.che.api.machine.shared.dto.execagent.UpdateSubscriptionResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.DtoWithPid;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.ExecAgentEventManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.RequestTransmitter;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;
import java.util.List;

import static org.eclipse.che.ide.util.StringUtils.join;

/**
 * Implementation of exec-agent command manager based on JSON RPC protocol that
 * uses bi-directional request/response transporting. 
 */
@Singleton
public class JsonRpcExecAgentCommandManager implements ExecAgentCommandManager {
    public static final String PROCESS_START             = "process.start";
    public static final String PROCESS_KILL              = "process.kill";
    public static final String PROCESS_SUBSCRIBE         = "process.subscribe";
    public static final String PROCESS_UNSUBSCRIBE       = "process.unsubscribe";
    public static final String PROCESS_UPDATE_SUBSCRIBER = "process.updateSubscriber";
    public static final String PROCESS_GET_LOGS          = "process.getLogs";
    public static final String PROCESS_GET_PROCESS       = "process.getProcess";
    public static final String PROCESS_GET_PROCESSES     = "process.getProcesses";

    private final DtoFactory            dtoFactory;
    private final RequestTransmitter    transmitter;
    private final ExecAgentEventManager eventManager;

    @Inject
    protected JsonRpcExecAgentCommandManager(DtoFactory dtoFactory, RequestTransmitter transmitter, ExecAgentEventManager eventManager) {
        this.dtoFactory = dtoFactory;
        this.transmitter = transmitter;
        this.eventManager = eventManager;
    }

    @Override
    public ExecAgentPromise<ProcessStartResponseDto> startProcess(final String endpointId, Command command) {
        String name = command.getName();
        String commandLine = command.getCommandLine();
        String type = command.getType();

        Log.debug(getClass(), "Starting a process. Name: " + name + ", command line: " + commandLine + ", type: " + type);

        ProcessStartRequestDto dto = dtoFactory.createDto(ProcessStartRequestDto.class)
                                               .withCommandLine(commandLine)
                                               .withName(name)
                                               .withType(type);

        final ExecAgentPromise<ProcessStartResponseDto> execAgentPromise = new ExecAgentPromise<>();

        transmitter.transmitOneToOne(endpointId, PROCESS_START, dto, ProcessStartResponseDto.class)
                   .then(new Operation<ProcessStartResponseDto>() {
                       @Override
                       public void apply(ProcessStartResponseDto arg) throws OperationException {
                           subscribe(endpointId, execAgentPromise, arg);
                       }
                   });

        return execAgentPromise;
    }

    @Override
    public Promise<ProcessKillResponseDto> killProcess(String endpointId, final int pid) {
        Log.debug(getClass(), "Killing a process. PID: " + pid);

        ProcessKillRequestDto dto = dtoFactory.createDto(ProcessKillRequestDto.class).withPid(pid);

        return transmitter.transmitOneToOne(endpointId, PROCESS_KILL, dto, ProcessKillResponseDto.class);
    }

    @Override
    public ExecAgentPromise<ProcessSubscribeResponseDto> subscribe(final String endpointId, int pid, List<String> eventTypes,
                                                                   String after) {
        Log.debug(getClass(), "Subscribing to a process. PID: " + pid + ", event types: " + eventTypes + ", after timestamp: " + after);

        ProcessSubscribeRequestDto dto = dtoFactory.createDto(ProcessSubscribeRequestDto.class)
                                                   .withPid(pid)
                                                   .withEventTypes(join(eventTypes, ","))
                                                   .withAfter(after);

        final ExecAgentPromise<ProcessSubscribeResponseDto> execAgentPromise = new ExecAgentPromise<>();

        transmitter.transmitOneToOne(endpointId, PROCESS_SUBSCRIBE, dto, ProcessSubscribeResponseDto.class).then(
                new Operation<ProcessSubscribeResponseDto>() {
                    @Override
                    public void apply(ProcessSubscribeResponseDto arg) throws OperationException {
                        subscribe(endpointId, execAgentPromise, arg);
                    }
                });

        return execAgentPromise;
    }

    @Override
    public Promise<ProcessUnSubscribeResponseDto> unsubscribe(String endpointId, int pid, List<String> eventTypes, String after) {
        Log.debug(getClass(), "Unsubscribing to a process. PID: " + pid + ", event types: " + eventTypes + ", after timestamp: " + after);

        final ProcessUnSubscribeRequestDto dto = dtoFactory.createDto(ProcessUnSubscribeRequestDto.class)
                                                           .withPid(pid)
                                                           .withEventTypes(join(eventTypes, ","))
                                                           .withAfter(after);

        return transmitter.transmitOneToOne(endpointId, PROCESS_UNSUBSCRIBE, dto, ProcessUnSubscribeResponseDto.class);
    }

    @Override
    public Promise<UpdateSubscriptionResponseDto> updateSubscription(String endpointId, int pid, List<String> eventTypes) {
        Log.debug(getClass(), "Updating subscription to a process. PID: " + pid + ", event types: " + eventTypes);

        final UpdateSubscriptionRequestDto dto = dtoFactory.createDto(UpdateSubscriptionRequestDto.class)
                                                           .withPid(pid)
                                                           .withEventTypes(join(eventTypes, ","));

        return transmitter.transmitOneToOne(endpointId, PROCESS_UPDATE_SUBSCRIBER, dto, UpdateSubscriptionResponseDto.class);
    }

    @Override
    public Promise<List<GetProcessLogsResponseDto>> getProcessLogs(String endpointId, int pid, String from, String till, int limit,
                                                                   int skip) {
        Log.debug(getClass(),
                  "Getting process logs" +
                  ". PID: " + pid +
                  ", from: " + from +
                  ", till: " + till +
                  ", limit: " + limit +
                  ", skip: " + skip);


        GetProcessLogsRequestDto dto = dtoFactory.createDto(GetProcessLogsRequestDto.class)
                                                 .withPid(pid)
                                                 .withFrom(from)
                                                 .withTill(till)
                                                 .withLimit(limit)
                                                 .withSkip(skip);

        return transmitter.transmitOneToMany(endpointId, PROCESS_GET_LOGS, dto, GetProcessLogsResponseDto.class);
    }

    @Override
    public Promise<GetProcessResponseDto> getProcess(String endpointId, int pid) {
        Log.debug(getClass(), "Getting process info. PID: " + pid);

        GetProcessRequestDto dto = dtoFactory.createDto(GetProcessRequestDto.class).withPid(pid);

        return transmitter.transmitOneToOne(endpointId, PROCESS_GET_PROCESS, dto, GetProcessResponseDto.class);
    }

    @Override
    public Promise<List<GetProcessesResponseDto>> getProcesses(String endpointId, boolean all) {
        Log.debug(getClass(), "Getting processes info. All: " + all);

        GetProcessesRequestDto dto = dtoFactory.createDto(GetProcessesRequestDto.class).withAll(all);

        return transmitter.transmitOneToMany(endpointId, PROCESS_GET_PROCESSES, dto, GetProcessesResponseDto.class);
    }

    private <T extends DtoWithPid> void subscribe(String endpointId, ExecAgentPromise<T> promise, T arg) throws OperationException {
        final int pid = arg.getPid();

        if (promise.hasProcessDiedEventOperation()) {
            final Operation<ProcessDiedEventDto> operation = promise.getProcessDiedEventDtoOperation();
            eventManager.registerProcessDiedOperation(endpointId, pid, operation);
        }

        if (promise.hasProcessStartedEventOperation()) {
            final Operation<ProcessStartedEventDto> operation = promise.getProcessStartedEventDtoOperation();
            eventManager.registerProcessStartedOperation(endpointId, pid, operation);
        }

        if (promise.hasProcessStdOutEventOperation()) {
            final Operation<ProcessStdOutEventDto> operation = promise.getProcessStdOutEventDtoOperation();
            eventManager.registerProcessStdOutOperation(endpointId, pid, operation);
        }

        if (promise.hasProcessStdErrEventOperation()) {
            final Operation<ProcessStdErrEventDto> operation = promise.getProcessStdErrEventDtoOperation();
            eventManager.registerProcessStdErrOperation(endpointId, pid, operation);
        }

        if (promise.hasOperation()) {
            promise.getOperation().apply(arg);
        }
    }
}
