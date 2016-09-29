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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.server.launcher.AgentLauncherFactory;
import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
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
public class AgentConfigApplierTest {

    @Mock
    private AgentSorter          sorter;
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
    @Mock
    private AgentRegistry        agentRegistry;

    private AgentConfigApplier agentConfigApplier;

    @BeforeMethod
    public void setUp() throws Exception {
        agentConfigApplier = new AgentConfigApplier(sorter, agentRegistry);
        when(agentRegistry.getAgent(AgentKeyImpl.parse("agent1"))).thenReturn(agent1);
        when(agentRegistry.getAgent(AgentKeyImpl.parse("agent2"))).thenReturn(agent2);
        when(agentRegistry.getAgent(AgentKeyImpl.parse("agent3"))).thenReturn(agent3);

        when(agent1.getScript()).thenReturn("script1");
        when(agent1.getDependencies()).thenReturn(singletonList("fqn3"));

        when(agent2.getScript()).thenReturn("script2");
        when(agent2.getDependencies()).thenReturn(singletonList("fqn3"));

        when(agent3.getScript()).thenReturn("script3");

    }

    @Test
    public void shouldAddExposedPorts() throws Exception {
        when(sorter.sort(any())).thenReturn(asList(AgentKeyImpl.parse("agent1"),
                                                   AgentKeyImpl.parse("agent2"),
                                                   AgentKeyImpl.parse("agent3")));
        when(agent1.getProperties()).thenReturn(singletonMap("ports", "terminal:1111/udp,terminal:2222/tcp"));
        when(agent2.getProperties()).thenReturn(singletonMap("ports", "3333/udp"));
        CheServiceImpl service = new CheServiceImpl();

        agentConfigApplier.modify(service, asList("agent1", "agent2", "agent3"));

        List<String> exposedPorts = service.getExpose();
        assertTrue(exposedPorts.contains("1111/udp"));
        assertTrue(exposedPorts.contains("2222/tcp"));
        assertTrue(exposedPorts.contains("3333/udp"));
    }

    @Test
    public void shouldAddEnvVariables() throws Exception {
        when(sorter.sort(any())).thenReturn(asList(AgentKeyImpl.parse("agent1"), AgentKeyImpl.parse("agent2")));
        when(agent1.getProperties()).thenReturn(singletonMap("environment", "p1=v1,p2=v2"));
        when(agent2.getProperties()).thenReturn(singletonMap("environment", "p3=v3"));
        CheServiceImpl service = new CheServiceImpl();

        agentConfigApplier.modify(service, asList("agent1", "agent2"));

        Map<String, String> env = service.getEnvironment();
        assertEquals(env.size(), 3);
        assertEquals(env.get("p1"), "v1");
        assertEquals(env.get("p2"), "v2");
        assertEquals(env.get("p3"), "v3");
    }

    @Test
    public void shouldIgnoreEnvironmentIfIllegalFormat() throws Exception {
        when(sorter.sort(any())).thenReturn(singletonList(AgentKeyImpl.parse("agent1")));
        when(agent1.getProperties()).thenReturn(singletonMap("environment", "p1"));
        CheServiceImpl service = new CheServiceImpl();

        agentConfigApplier.modify(service, singletonList("agent1"));

        Map<String, String> env = service.getEnvironment();
        assertEquals(env.size(), 0);
    }
}
