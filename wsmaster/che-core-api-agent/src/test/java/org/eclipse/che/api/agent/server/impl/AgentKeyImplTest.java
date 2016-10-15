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

import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Anatolii Bazko
 */
public class AgentKeyImplTest {

    @Test
    public void testAgentKeyWithNameAndVersion() {
        AgentKeyImpl agentKey = AgentKeyImpl.parse("name:1");

        assertEquals(agentKey.getName(), "name");
        assertEquals(agentKey.getVersion(), "1");
    }

    @Test
    public void testParseAgentKeyWithName() {
        AgentKeyImpl agentKey = AgentKeyImpl.parse("name");

        assertEquals(agentKey.getName(), "name");
        assertNull(agentKey.getVersion());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseAgentKeyFails() {
        AgentKeyImpl.parse("name:1:2");
    }
}
