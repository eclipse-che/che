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
package org.eclipse.che.ide.extension.machine.client.command;

import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProviderRegistry;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class CommandManagerTest {

    @Mock
    private DtoFactory                           dtoFactory;
    @Mock
    private MachineServiceClient                 machineServiceClient;
    @Mock
    private ProcessesPanelPresenter              processesPanelPresenter;
    @Mock
    private CommandConsoleFactory                commandConsoleFactory;
    @Mock
    private NotificationManager                  notificationManager;
    @Mock
    private MachineLocalizationConstant          localizationConstant;
    @Mock
    private WorkspaceAgent                       workspaceAgent;
    @Mock
    private AppContext                           appContext;
    @Mock
    private DevMachine                           devMachine;
    @Mock
    private CommandPropertyValueProviderRegistry commandPropertyValueProviderRegistry;

    @Mock
    private Promise<MachineProcessDto>                   processPromise;
    @Captor
    private ArgumentCaptor<Operation<MachineProcessDto>> processCaptor;

    @InjectMocks
    private CommandManager commandManager;

    @Test
    public void shouldShowWarningWhenNoDevMachineIsRunning() throws Exception {
        when(appContext.getDevMachine()).thenReturn(devMachine);
        when(devMachine.getId()).thenReturn(null);
        commandManager.execute(mock(CommandConfiguration.class));

        verify(localizationConstant).noDevMachine();
        notificationManager.notify(anyString());
    }

}
