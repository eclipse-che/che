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

import org.eclipse.che.api.agent.server.launcher.AgentLauncherFactory;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class DockerAgentConfigApplierTest {

    @Mock
    private AgentSorter          sorter;
    @Mock
    private MachineConfig        machineConfig;
    @Mock
    private Instance             machine;
    @Mock
    private Agent                agent1;
    @Mock
    private Agent                agent2;
    @Mock
    private Agent                agent3;
    @Mock
    private AgentLauncherFactory agentLauncher;

    private DockerAgentConfigApplier dockerAgentConfigApplier;

    @BeforeMethod
    public void setUp() throws Exception {
        when(sorter.sort(any())).thenReturn(Arrays.asList(agent1, agent2, agent3));

        dockerAgentConfigApplier = new DockerAgentConfigApplier(sorter);

        when(machine.getConfig()).thenReturn(machineConfig);
        when(machineConfig.getAgents()).thenReturn(asList("fqn1:1.0.0", "fqn2"));

        when(agent1.getScript()).thenReturn("script1");
        when(agent1.getDependencies()).thenReturn(singletonList("fqn3"));

        when(agent2.getScript()).thenReturn("script2");
        when(agent2.getDependencies()).thenReturn(singletonList("fqn3"));

        when(agent3.getScript()).thenReturn("script3");

    }

    @Test
    public void shouldAddExposedPorts() throws Exception {
        when(agent1.getProperties()).thenReturn(singletonMap("ports", "terminal:1111/udp,terminal:2222/tcp"));
        when(agent2.getProperties()).thenReturn(singletonMap("ports", "3333/udp"));
        ContainerConfig containerConfig = new ContainerConfig();

        dockerAgentConfigApplier.applyOn(containerConfig, machineConfig.getAgents());

        Map<String, Map<String, String>> exposedPorts = containerConfig.getExposedPorts();
        assertTrue(exposedPorts.containsKey("1111/udp"));
        assertTrue(exposedPorts.containsKey("2222/tcp"));
        assertTrue(exposedPorts.containsKey("3333/udp"));
    }

    @Test
    public void shouldAddEnvVariables() throws Exception {
        when(agent1.getProperties()).thenReturn(singletonMap("environment", "p1=v1,p2=v2"));
        when(agent2.getProperties()).thenReturn(singletonMap("environment", "p3=v3"));
        ContainerConfig containerConfig = new ContainerConfig();

        dockerAgentConfigApplier.applyOn(containerConfig, machineConfig.getAgents());

        String[] env = containerConfig.getEnv();
        assertEquals(env.length, 3);
        assertEquals(env[0], "p1=v1");
        assertEquals(env[1], "p2=v2");
        assertEquals(env[2], "p3=v3");
    }


    @Test
    public void shouldIgnoreEnvironmentIfIllegalFormat() throws Exception {
        when(agent1.getProperties()).thenReturn(singletonMap("environment", "p1"));

        ContainerConfig containerConfig = new ContainerConfig();

        dockerAgentConfigApplier.applyOn(containerConfig, machineConfig.getAgents());

        String[] env = containerConfig.getEnv();
        assertEquals(env.length, 0);
    }
}
