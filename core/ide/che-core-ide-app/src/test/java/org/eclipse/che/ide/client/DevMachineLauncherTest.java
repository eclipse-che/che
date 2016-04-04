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
package org.eclipse.che.ide.client;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.client.DevMachineLauncher.MachineStartedCallback;
import org.eclipse.che.ide.context.AppContextImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Artem Zatsarynny
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class DevMachineLauncherTest {

    private static final String DEV_MACHINE_ID = "id";
    private static final String TEXT           = "A horse, a horse! My kingdom for a horse! Richard III";
    private static final String WS_AGENT_URL   = "http://url";

    @Mock
    private MachineServiceClient machineServiceClient;
    @Mock
    private AppContextImpl       appContext;
    @Mock
    private MachineManager       machineManager;

    @Mock
    private MachineDto                machineDescriptor;
    @Mock
    private MachineConfigDto          machineConfigDescriptor;
    @Mock
    private EventBus                  eventBus;
    @Mock
    private Promise<List<MachineDto>> machinesPromise;
    @Mock
    private WorkspaceDto              usersWorkspaceDto;
    @Mock
    private MachineRuntimeInfoDto     runtimeInfoDto;
    @Mock
    private ServerDto                 serverDto;
    @Mock
    private MachineStartedCallback    startedCallback;

    private DevMachineLauncher devMachineLauncher;

    @Captor
    private ArgumentCaptor<Operation<List<MachineDto>>> machinesCaptor;

    @Before
    public void setUp() {
        Map<String, ServerDto> servers = Collections.singletonMap(Constants.WS_AGENT_PORT, serverDto);

        when(machineDescriptor.getConfig()).thenReturn(machineConfigDescriptor);
        when(machineDescriptor.getId()).thenReturn(DEV_MACHINE_ID);
        when(machineDescriptor.getRuntime()).thenReturn(runtimeInfoDto);
        when(runtimeInfoDto.getServers()).thenReturn(servers);
        when(serverDto.getUrl()).thenReturn(WS_AGENT_URL);

        when(appContext.getWorkspace()).thenReturn(usersWorkspaceDto);
        when(machineServiceClient.getMachines(anyString())).thenReturn(machinesPromise);
        when(machinesPromise.then(Matchers.<Operation<List<MachineDto>>>anyObject())).thenReturn(machinesPromise);
        when(machineConfigDescriptor.isDev()).thenReturn(true);
        when(usersWorkspaceDto.getId()).thenReturn(TEXT);

        devMachineLauncher = new DevMachineLauncher("/pathToWsAgent", appContext, machineManager, machineServiceClient);
    }

    @Test
    public void shouldUseRunningDevMachine() throws Exception {
        when(machineDescriptor.getStatus()).thenReturn(MachineStatus.RUNNING);

        devMachineLauncher.startDevMachine(startedCallback);

        verify(machineServiceClient).getMachines(anyString());
        verify(machinesPromise).then(machinesCaptor.capture());

        machinesCaptor.getValue().apply(Collections.singletonList(machineDescriptor));
        verify(machineConfigDescriptor).isDev();

        verify(machineDescriptor).getStatus();
        verify(appContext).setDevMachineId(DEV_MACHINE_ID);
        verify(appContext).setWsAgentURL(WS_AGENT_URL + "/pathToWsAgent");

        verify(machineManager).onMachineRunning(DEV_MACHINE_ID);
        verify(startedCallback).onStarted();
    }

    @Test
    public void shouldTransmitControlToMachineManager() throws Exception {
        when(machineDescriptor.getStatus()).thenReturn(MachineStatus.CREATING);

        devMachineLauncher.startDevMachine(startedCallback);

        verify(machineServiceClient).getMachines(anyString());
        verify(machinesPromise).then(machinesCaptor.capture());

        machinesCaptor.getValue().apply(Collections.singletonList(machineDescriptor));
        verify(machineConfigDescriptor).isDev();
        verify(machineDescriptor).getStatus();
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionShouldBeReturnedWhenWsAgentUrlIsNull() throws OperationException {
        when(serverDto.getUrl()).thenReturn(null);
        when(machineDescriptor.getStatus()).thenReturn(MachineStatus.RUNNING);

        devMachineLauncher.startDevMachine(startedCallback);

        verify(machineServiceClient).getMachines(anyString());
        verify(machinesPromise).then(machinesCaptor.capture());

        machinesCaptor.getValue().apply(Collections.singletonList(machineDescriptor));
    }
}
