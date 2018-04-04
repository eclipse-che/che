/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.execute;

import static org.eclipse.che.ide.util.StringUtils.join;

import com.google.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Singleton;
import org.eclipse.che.agent.exec.shared.dto.DtoWithPid;
import org.eclipse.che.agent.exec.shared.dto.GetProcessLogsRequestDto;
import org.eclipse.che.agent.exec.shared.dto.GetProcessLogsResponseDto;
import org.eclipse.che.agent.exec.shared.dto.GetProcessRequestDto;
import org.eclipse.che.agent.exec.shared.dto.GetProcessResponseDto;
import org.eclipse.che.agent.exec.shared.dto.GetProcessesRequestDto;
import org.eclipse.che.agent.exec.shared.dto.GetProcessesResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessKillRequestDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessKillResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessStartRequestDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessStartResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessSubscribeRequestDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessSubscribeResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessUnSubscribeRequestDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessUnSubscribeResponseDto;
import org.eclipse.che.agent.exec.shared.dto.UpdateSubscriptionRequestDto;
import org.eclipse.che.agent.exec.shared.dto.UpdateSubscriptionResponseDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessDiedEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStartedEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStdErrEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStdOutEventDto;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.ide.api.command.exec.ExecAgentCommandManager;
import org.eclipse.che.ide.api.command.exec.ExecAgentConsumer;
import org.eclipse.che.ide.api.command.exec.ExecAgentEventManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Implementation of exec-agent command manager based on JSON RPC protocol that uses bi-directional
 * request/response transporting.
 */
@Singleton
public class JsonRpcExecAgentCommandManager implements ExecAgentCommandManager {
  public static final String PROCESS_START = "process.start";
  public static final String PROCESS_KILL = "process.kill";
  public static final String PROCESS_SUBSCRIBE = "process.subscribe";
  public static final String PROCESS_UNSUBSCRIBE = "process.unsubscribe";
  public static final String PROCESS_UPDATE_SUBSCRIBER = "process.updateSubscriber";
  public static final String PROCESS_GET_LOGS = "process.getLogs";
  public static final String PROCESS_GET_PROCESS = "process.getProcess";
  public static final String PROCESS_GET_PROCESSES = "process.getProcesses";

  private final DtoFactory dtoFactory;
  private final RequestTransmitter transmitter;
  private final ExecAgentEventManager eventManager;

  @Inject
  protected JsonRpcExecAgentCommandManager(
      DtoFactory dtoFactory, RequestTransmitter transmitter, ExecAgentEventManager eventManager) {
    this.dtoFactory = dtoFactory;
    this.transmitter = transmitter;
    this.eventManager = eventManager;
  }

  @Override
  public ExecAgentConsumer<ProcessStartResponseDto> startProcess(
      final String endpointId, Command command) {
    String name = command.getName();
    String commandLine = command.getCommandLine();
    String type = command.getType();

    Log.debug(
        getClass(),
        "Starting a process. Name: " + name + ", command line: " + commandLine + ", type: " + type);

    ProcessStartRequestDto dto =
        dtoFactory
            .createDto(ProcessStartRequestDto.class)
            .withCommandLine(commandLine)
            .withName(name)
            .withType(type);

    final ExecAgentConsumer<ProcessStartResponseDto> execAgentConsumer = new ExecAgentConsumer<>();

    transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(PROCESS_START)
        .paramsAsDto(dto)
        .sendAndReceiveResultAsDto(ProcessStartResponseDto.class)
        .onSuccess(
            processStartResponseDto ->
                subscribe(endpointId, execAgentConsumer, processStartResponseDto));

    return execAgentConsumer;
  }

  @Override
  public JsonRpcPromise<ProcessKillResponseDto> killProcess(String endpointId, final int pid) {
    Log.debug(getClass(), "Killing a process. PID: " + pid);

    ProcessKillRequestDto dto = dtoFactory.createDto(ProcessKillRequestDto.class).withPid(pid);

    return transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(PROCESS_KILL)
        .paramsAsDto(dto)
        .sendAndReceiveResultAsDto(ProcessKillResponseDto.class);
  }

  @Override
  public ExecAgentConsumer<ProcessSubscribeResponseDto> subscribe(
      final String endpointId, int pid, List<String> eventTypes, String after) {
    Log.debug(
        getClass(),
        "Subscribing to a process. PID: "
            + pid
            + ", event types: "
            + eventTypes
            + ", after timestamp: "
            + after);

    ProcessSubscribeRequestDto dto =
        dtoFactory
            .createDto(ProcessSubscribeRequestDto.class)
            .withPid(pid)
            .withEventTypes(join(eventTypes, ","))
            .withAfter(after);

    final ExecAgentConsumer<ProcessSubscribeResponseDto> execAgentConsumer =
        new ExecAgentConsumer<>();

    transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(PROCESS_SUBSCRIBE)
        .paramsAsDto(dto)
        .sendAndReceiveResultAsDto(ProcessSubscribeResponseDto.class)
        .onSuccess(
            (s, processSubscribeResponseDto) ->
                subscribe(endpointId, execAgentConsumer, processSubscribeResponseDto));
    return execAgentConsumer;
  }

  @Override
  public JsonRpcPromise<ProcessUnSubscribeResponseDto> unsubscribe(
      String endpointId, int pid, List<String> eventTypes, String after) {
    Log.debug(
        getClass(),
        "Unsubscribing to a process. PID: "
            + pid
            + ", event types: "
            + eventTypes
            + ", after timestamp: "
            + after);

    final ProcessUnSubscribeRequestDto dto =
        dtoFactory
            .createDto(ProcessUnSubscribeRequestDto.class)
            .withPid(pid)
            .withEventTypes(join(eventTypes, ","))
            .withAfter(after);

    return transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(PROCESS_UNSUBSCRIBE)
        .paramsAsDto(dto)
        .sendAndReceiveResultAsDto(ProcessUnSubscribeResponseDto.class);
  }

  @Override
  public JsonRpcPromise<UpdateSubscriptionResponseDto> updateSubscription(
      String endpointId, int pid, List<String> eventTypes) {
    Log.debug(
        getClass(),
        "Updating subscription to a process. PID: " + pid + ", event types: " + eventTypes);

    final UpdateSubscriptionRequestDto dto =
        dtoFactory
            .createDto(UpdateSubscriptionRequestDto.class)
            .withPid(pid)
            .withEventTypes(join(eventTypes, ","));

    return transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(PROCESS_UPDATE_SUBSCRIBER)
        .paramsAsDto(dto)
        .sendAndReceiveResultAsDto(UpdateSubscriptionResponseDto.class);
  }

  @Override
  public JsonRpcPromise<List<GetProcessLogsResponseDto>> getProcessLogs(
      String endpointId, int pid, String from, String till, int limit, int skip) {
    Log.debug(
        getClass(),
        "Getting process logs"
            + ". PID: "
            + pid
            + ", from: "
            + from
            + ", till: "
            + till
            + ", limit: "
            + limit
            + ", skip: "
            + skip);

    GetProcessLogsRequestDto dto =
        dtoFactory
            .createDto(GetProcessLogsRequestDto.class)
            .withPid(pid)
            .withFrom(from)
            .withTill(till)
            .withLimit(limit)
            .withSkip(skip);

    return transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(PROCESS_GET_LOGS)
        .paramsAsDto(dto)
        .sendAndReceiveResultAsListOfDto(GetProcessLogsResponseDto.class);
  }

  @Override
  public JsonRpcPromise<GetProcessResponseDto> getProcess(String endpointId, int pid) {
    Log.debug(getClass(), "Getting process info. PID: " + pid);

    GetProcessRequestDto dto = dtoFactory.createDto(GetProcessRequestDto.class).withPid(pid);

    return transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(PROCESS_GET_PROCESS)
        .paramsAsDto(dto)
        .sendAndReceiveResultAsDto(GetProcessResponseDto.class);
  }

  @Override
  public JsonRpcPromise<List<GetProcessesResponseDto>> getProcesses(
      String endpointId, boolean all) {
    Log.debug(getClass(), "Getting processes info. All: " + all);

    GetProcessesRequestDto dto = dtoFactory.createDto(GetProcessesRequestDto.class).withAll(all);

    return transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(PROCESS_GET_PROCESSES)
        .paramsAsDto(dto)
        .sendAndReceiveResultAsListOfDto(GetProcessesResponseDto.class);
  }

  private <T extends DtoWithPid> void subscribe(
      String endpointId, ExecAgentConsumer<T> promise, T arg) {
    final int pid = arg.getPid();

    if (promise.hasProcessDiedEventConsumer()) {
      Consumer<ProcessDiedEventDto> consumer = promise.getProcessDiedEventDtoConsumer();
      eventManager.registerProcessDiedConsumer(endpointId, pid, consumer);
    }

    if (promise.hasProcessStartedEventConsumer()) {
      Consumer<ProcessStartedEventDto> consumer = promise.getProcessStartedEventDtoConsumer();
      eventManager.registerProcessStartedConsumer(endpointId, pid, consumer);
    }

    if (promise.hasProcessStdOutEventConsumer()) {
      Consumer<ProcessStdOutEventDto> consumer = promise.getProcessStdOutEventDtoConsumer();
      eventManager.registerProcessStdOutConsumer(endpointId, pid, consumer);
    }

    if (promise.hasProcessStdErrEventConsumer()) {
      Consumer<ProcessStdErrEventDto> consumer = promise.getProcessStdErrEventDtoConsumer();
      eventManager.registerProcessStdErrConsumer(endpointId, pid, consumer);
    }

    if (promise.hasOperation()) {
      promise.getConsumer().accept(arg);
    }
  }
}
