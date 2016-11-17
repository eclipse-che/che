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
package org.eclipse.che.api.agent.server.launcher;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class DefaultAgentLauncherTest {

    @Mock
    private Instance                        machine;
    @Mock
    private Agent                           agent;
    @Mock
    private LineConsumer                    lineConsumer;
    @Mock
    private InstanceProcess                 instanceProcess;

    private AgentLauncher agentLauncher;

    @BeforeMethod
    public void setUp() throws Exception {
        agentLauncher = new DefaultAgentLauncher();

        when(machine.createProcess(any(), any())).thenReturn(instanceProcess);
        when(machine.getLogger()).thenReturn(lineConsumer);
        when(agent.getScript()).thenReturn("script1");
    }

    @Test
    public void shouldLaunchAgent() throws Exception {
        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);

        agentLauncher.launch(machine, agent);

        verify(machine).createProcess(commandCaptor.capture(), (String)isNull());

        Command command = commandCaptor.getValue();
        assertEquals(command.getCommandLine(), "script1");

        verify(instanceProcess).start(any());
    }

    @Test
    public void shouldDoNothingIfAgentScriptIsNull() throws Exception {
        when(agent.getScript()).thenReturn(null);

        agentLauncher.launch(machine, agent);

        verifyZeroInteractions(machine, lineConsumer, instanceProcess);
    }

    @Test
    public void shouldDoNothingIfAgentScriptIsEmpty() throws Exception {
        when(agent.getScript()).thenReturn("");

        agentLauncher.launch(machine, agent);

        verifyZeroInteractions(machine, lineConsumer, instanceProcess);
    }
}
