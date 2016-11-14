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

import org.eclipse.che.api.agent.shared.model.Agent;
import org.everrest.assured.EverrestJetty;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;

import static org.testng.AssertJUnit.assertFalse;

/**
 * @author Anatoliy Bazko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class LocalAgentRegistryImplTest {

    private LocalAgentRegistryImpl agentRegistry;

    @BeforeMethod
    public void setUp() throws Exception {
        agentRegistry = new LocalAgentRegistryImpl(Collections.emptySet());
    }

    @Test
    public void testInitializeAgents() throws Exception {
        Collection<Agent> agents = agentRegistry.getAgents();
        assertFalse(agents.isEmpty());
    }
}
