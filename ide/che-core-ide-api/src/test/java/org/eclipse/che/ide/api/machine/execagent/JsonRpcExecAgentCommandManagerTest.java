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
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.RequestTransmitter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcExecAgentCommandManager}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcExecAgentCommandManagerTest {
    public static final String ID                = "exec-agent";
    public static final String START             = "process.start";
    public static final String KILL              = "process.kill";
    public static final String SUBSCRIBE         = "process.subscribe";
    public static final String UN_SUBSCRIBE      = "process.unsubscribe";
    public static final String UPDATE_SUBSCRIBER = "process.updateSubscriber";
    public static final String GET_LOGS          = "process.getLogs";
    public static final String GET_PROCESS       = "process.getProcess";
    public static final String GET_PROCESSES     = "process.getProcesses";

    @Mock
    private DtoFactory                     dtoFactory;
    @Mock
    private RequestTransmitter             transmitter;
    @InjectMocks
    private JsonRpcExecAgentCommandManager commandManager;


    @Mock
    private ProcessStartRequestDto       processStartRequestDto;
    @Mock
    private ProcessKillRequestDto        processKillRequestDto;
    @Mock
    private ProcessSubscribeRequestDto   processSubscribeRequestDto;
    @Mock
    private ProcessUnSubscribeRequestDto processUnSubscribeRequestDto;
    @Mock
    private UpdateSubscriptionRequestDto updateSubscriptionRequestDto;
    @Mock
    private GetProcessLogsRequestDto     getProcessLogsRequestDto;
    @Mock
    private GetProcessRequestDto         getProcessRequestDto;
    @Mock
    private GetProcessesRequestDto       getProcessesRequestDto;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProperlyRunStartProcess() {
        when(transmitter.transmitRequest(ID, START, processStartRequestDto, ProcessStartResponseDto.class)).thenReturn(mock(Promise.class));
        when(dtoFactory.createDto(ProcessStartRequestDto.class)).thenReturn(processStartRequestDto);
        when(processStartRequestDto.withName(anyString())).thenReturn(processStartRequestDto);
        when(processStartRequestDto.withCommandLine(anyString())).thenReturn(processStartRequestDto);
        when(processStartRequestDto.withType(anyString())).thenReturn(processStartRequestDto);

        commandManager.startProcess("endpointId", new CommandImpl("name", "command", "type"));

        verify(dtoFactory).createDto(ProcessStartRequestDto.class);
        verify(processStartRequestDto).withName("name");
        verify(processStartRequestDto).withCommandLine("command");
        verify(processStartRequestDto).withType("type");
        verify(transmitter).transmitRequest(ID, START, processStartRequestDto, ProcessStartResponseDto.class);
    }

    @Test
    public void shouldProperlyRunKillProcess() {
        when(transmitter.transmitRequest(ID, KILL, processKillRequestDto, ProcessKillResponseDto.class)).thenReturn(mock(Promise.class));
        when(dtoFactory.createDto(ProcessKillRequestDto.class)).thenReturn(processKillRequestDto);
        when(processKillRequestDto.withPid(anyInt())).thenReturn(processKillRequestDto);

        commandManager.killProcess("endpointId", 0);

        verify(dtoFactory).createDto(ProcessKillRequestDto.class);
        verify(processKillRequestDto).withPid(0);
        verify(transmitter).transmitRequest(ID, KILL, processKillRequestDto, ProcessKillResponseDto.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProperlyRunSubscribe() {
        when(transmitter.transmitRequest(ID, SUBSCRIBE, processSubscribeRequestDto, ProcessSubscribeResponseDto.class))
                .thenReturn(mock(Promise.class));
        when(dtoFactory.createDto(ProcessSubscribeRequestDto.class)).thenReturn(processSubscribeRequestDto);
        when(processSubscribeRequestDto.withPid(anyInt())).thenReturn(processSubscribeRequestDto);
        when(processSubscribeRequestDto.withAfter(anyString())).thenReturn(processSubscribeRequestDto);
        when(processSubscribeRequestDto.withEventTypes(anyString())).thenReturn(processSubscribeRequestDto);

        commandManager.subscribe("endpointId", 0, singletonList("type"), "after");

        verify(dtoFactory).createDto(ProcessSubscribeRequestDto.class);
        verify(processSubscribeRequestDto).withPid(0);
        verify(processSubscribeRequestDto).withEventTypes("type");
        verify(processSubscribeRequestDto).withAfter("after");
        verify(transmitter).transmitRequest(ID, SUBSCRIBE, processSubscribeRequestDto, ProcessSubscribeResponseDto.class);
    }

    @Test
    public void shouldProperlyRunUnSubscribe() {
        when(dtoFactory.createDto(ProcessUnSubscribeRequestDto.class)).thenReturn(processUnSubscribeRequestDto);
        when(processUnSubscribeRequestDto.withPid(anyInt())).thenReturn(processUnSubscribeRequestDto);
        when(processUnSubscribeRequestDto.withAfter(anyString())).thenReturn(processUnSubscribeRequestDto);
        when(processUnSubscribeRequestDto.withEventTypes(anyString())).thenReturn(processUnSubscribeRequestDto);

        commandManager.unsubscribe("endpointId", 0, singletonList("type"), "after");

        verify(dtoFactory).createDto(ProcessUnSubscribeRequestDto.class);
        verify(processUnSubscribeRequestDto).withPid(0);
        verify(processUnSubscribeRequestDto).withEventTypes("type");
        verify(processUnSubscribeRequestDto).withAfter("after");
        verify(transmitter).transmitRequest(ID, UN_SUBSCRIBE, processUnSubscribeRequestDto, ProcessUnSubscribeResponseDto.class);
    }

    @Test
    public void shouldProperlyRunUpdateSubscription() {
        when(dtoFactory.createDto(UpdateSubscriptionRequestDto.class)).thenReturn(updateSubscriptionRequestDto);
        when(updateSubscriptionRequestDto.withPid(anyInt())).thenReturn(updateSubscriptionRequestDto);
        when(updateSubscriptionRequestDto.withEventTypes(anyString())).thenReturn(updateSubscriptionRequestDto);

        commandManager.updateSubscription("endpointId", 0, singletonList("type"));

        verify(dtoFactory).createDto(UpdateSubscriptionRequestDto.class);
        verify(updateSubscriptionRequestDto).withPid(0);
        verify(updateSubscriptionRequestDto).withEventTypes("type");
        verify(transmitter).transmitRequest(ID, UPDATE_SUBSCRIBER, updateSubscriptionRequestDto, UpdateSubscriptionResponseDto.class);
    }

    @Test
    public void shouldProperlyGetProcessLogs() {
        when(dtoFactory.createDto(GetProcessLogsRequestDto.class)).thenReturn(getProcessLogsRequestDto);
        when(getProcessLogsRequestDto.withPid(anyInt())).thenReturn(getProcessLogsRequestDto);
        when(getProcessLogsRequestDto.withLimit(anyInt())).thenReturn(getProcessLogsRequestDto);
        when(getProcessLogsRequestDto.withSkip(anyInt())).thenReturn(getProcessLogsRequestDto);
        when(getProcessLogsRequestDto.withFrom(anyString())).thenReturn(getProcessLogsRequestDto);
        when(getProcessLogsRequestDto.withTill(anyString())).thenReturn(getProcessLogsRequestDto);

        commandManager.getProcessLogs("endpointId", 0, "from", "till", 0, 0);

        verify(dtoFactory).createDto(GetProcessLogsRequestDto.class);
        verify(getProcessLogsRequestDto).withPid(0);
        verify(getProcessLogsRequestDto).withLimit(0);
        verify(getProcessLogsRequestDto).withSkip(0);
        verify(getProcessLogsRequestDto).withFrom("from");
        verify(getProcessLogsRequestDto).withTill("till");
        verify(transmitter).transmitRequestForList(ID, GET_LOGS, getProcessLogsRequestDto, GetProcessLogsResponseDto.class);
    }

    @Test
    public void shouldProperlyGetProcess() {
        when(dtoFactory.createDto(GetProcessRequestDto.class)).thenReturn(getProcessRequestDto);
        when(getProcessRequestDto.withPid(anyInt())).thenReturn(getProcessRequestDto);

        commandManager.getProcess("endpointId", 0);

        verify(dtoFactory).createDto(GetProcessRequestDto.class);
        verify(getProcessRequestDto).withPid(0);
        verify(transmitter).transmitRequest(ID, GET_PROCESS, getProcessRequestDto, GetProcessResponseDto.class);
    }

    @Test
    public void shouldProperlyGetProcesses() {
        when(dtoFactory.createDto(GetProcessesRequestDto.class)).thenReturn(getProcessesRequestDto);
        when(getProcessesRequestDto.withAll(anyBoolean())).thenReturn(getProcessesRequestDto);

        commandManager.getProcesses("endpointId", true);

        verify(dtoFactory).createDto(GetProcessesRequestDto.class);
        verify(getProcessesRequestDto).withAll(true);
        verify(transmitter).transmitRequestForList(ID, GET_PROCESSES, getProcessesRequestDto, GetProcessesResponseDto.class);
    }
}
