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
package org.eclipse.che.api.agent.server.impl;

import org.eclipse.che.api.agent.server.AgentFactory;
import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatolii Bazko
 */
@Listeners(MockitoTestNGListener.class)
public class AgentProviderImplTest {

    @Mock
    private AgentRegistry agentRegistry;
    @Mock
    private AgentFactory  agentFactory;

    private AgentProviderImpl agentProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        when(agentFactory.getFqn()).thenReturn("fqn");
        when(agentRegistry.getVersions(eq("fqn"))).thenReturn(singletonList("1.0.0"));

        agentProvider = new AgentProviderImpl(singleton(agentFactory), agentRegistry);
    }

    @Test
    public void testGetAgents() throws Exception {
        List<AgentKey> agents = agentProvider.getAgents();

        assertEquals(agents.size(), 1);
        assertEquals(agents.get(0), AgentKeyImpl.of("fqn:1.0.0"));
    }
}
