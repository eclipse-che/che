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
package org.eclipse.che.api.agent.server.impl;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class AgentSorterTest {

    @Mock
    private AgentRegistry agentRegistry;
    @Mock
    private Agent         agent1;
    @Mock
    private Agent         agent2;
    @Mock
    private Agent         agent3;

    @InjectMocks
    private AgentSorter agentSorter;

    @BeforeMethod
    public void setUp() throws Exception {
        when(agentRegistry.getAgent(eq(AgentKeyImpl.parse("fqn1")))).thenReturn(agent1);
        when(agentRegistry.getAgent(eq(AgentKeyImpl.parse("fqn2")))).thenReturn(agent2);
        when(agentRegistry.getAgent(eq(AgentKeyImpl.parse("fqn3")))).thenReturn(agent3);
        when(agentRegistry.getAgent(eq(AgentKeyImpl.parse("fqn4")))).thenThrow(new AgentNotFoundException("Agent not found"));

        when(agent1.getDependencies()).thenReturn(singletonList("fqn3"));
        when(agent1.getId()).thenReturn("fqn1");

        when(agent2.getDependencies()).thenReturn(singletonList("fqn3"));
        when(agent2.getId()).thenReturn("fqn2");

        when(agent3.getId()).thenReturn("fqn3");
    }

    @Test
    public void sortAgentsRespectingDependencies() throws Exception {
        List<AgentKey> sorted = agentSorter.sort(Arrays.asList("fqn1", "fqn2", "fqn3"));

        assertEquals(sorted.size(), 3);
        assertEquals(sorted.get(0).getId(), "fqn3");
        assertEquals(sorted.get(1).getId(), "fqn1");
        assertEquals(sorted.get(2).getId(), "fqn2");
    }

    @Test(expectedExceptions = AgentException.class, expectedExceptionsMessageRegExp = ".*fqn1.*fqn2.*")
    public void sortingShouldFailIfCircularDependenciesFound() throws Exception {
        when(agent1.getDependencies()).thenReturn(singletonList("fqn2"));
        when(agent2.getDependencies()).thenReturn(singletonList("fqn1"));

        agentSorter.sort(Arrays.asList("fqn1", "fqn2"));
    }

    @Test(expectedExceptions = AgentNotFoundException.class)
    public void sortingShouldFailIfAgentNotFound() throws Exception {
        agentSorter.sort(singletonList("fqn4"));
    }
}
