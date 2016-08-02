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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.agent.server.Agent;
import org.eclipse.che.api.agent.server.AgentKey;
import org.eclipse.che.api.agent.server.AgentProvider;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class DockerAgentsApplierTest {

    @Mock
    private MachineConfig       machineConfig;
    @Mock
    private Instance            machine;
    @Mock
    private AgentProvider       agentProvider;
    @Mock
    private Agent               agent1;
    @Mock
    private Agent               agent2;
    @Mock
    private Agent               agent3;
    @Mock
    private InstanceProcess     instanceProcess;
    @InjectMocks
    private DockerAgentsApplier dockerAgentsApplier;

    @BeforeMethod
    public void setUp() throws Exception {
        when(machine.getConfig()).thenReturn(machineConfig);
        when(machine.createProcess(any(), any())).thenReturn(instanceProcess);

        when(machineConfig.getAgents()).thenReturn(asList("fqn1:1.0.0", "fqn2"));

        when(agentProvider.createAgent(eq(AgentKey.of("fqn1:1.0.0")))).thenReturn(agent1);
        when(agentProvider.createAgent(eq(AgentKey.of("fqn2")))).thenReturn(agent2);
        when(agentProvider.createAgent(eq(AgentKey.of("fqn3")))).thenReturn(agent3);

        when(agent1.getScript()).thenReturn("script1");
        when(agent1.getDependencies()).thenReturn(singletonList("fqn3"));

        when(agent2.getScript()).thenReturn("script2");
        when(agent2.getDependencies()).thenReturn(singletonList("fqn3"));

        when(agent3.getScript()).thenReturn("script3");

    }

    @Test
    public void testApplyAllRespectingDependencies() throws Exception {
        ArgumentCaptor<Command> command = ArgumentCaptor.forClass(Command.class);

        dockerAgentsApplier.apply(machine);

        verify(machine, times(3)).createProcess(command.capture(), any());

        List<Command> commands = command.getAllValues();
        assertEquals(commands.size(), 3);
        assertEquals(commands.get(0).getCommandLine(), "script3");
        assertEquals(commands.get(1).getCommandLine(), "script1");
        assertEquals(commands.get(2).getCommandLine(), "script2");
    }

    @Test(expectedExceptions = MachineException.class)
    public void shouldFailIfCircularDependenciesFound() throws Exception {
        when(agent1.getDependencies()).thenReturn(singletonList("fqn2"));
        when(agent2.getDependencies()).thenReturn(singletonList("fqn1"));

        dockerAgentsApplier.apply(machine);
    }

    @Test(expectedExceptions = MachineException.class, expectedExceptionsMessageRegExp = ".*Agent error.*")
    public void shouldFailIfAgentFail() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                LineConsumer lineConsumer = (LineConsumer)invocation.getArguments()[0];
                lineConsumer.writeLine("[STDERR] Agent error");
                return null;
            }
        }).when(instanceProcess).start(any(LineConsumer.class));

        dockerAgentsApplier.apply(machine);
    }
}
