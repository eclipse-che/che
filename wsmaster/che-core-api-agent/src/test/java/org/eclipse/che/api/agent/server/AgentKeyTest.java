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
package org.eclipse.che.api.agent.server;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Anatolii Bazko
 */
public class AgentKeyTest {

    @Test
    public void testAgentKeyWithFqnAndVersion() {
        AgentKey agentKey = AgentKey.of("fqn:1");

        assertEquals(agentKey.getFqn(), "fqn");
        assertEquals(agentKey.getVersion(), "1");
    }

    @Test
    public void testParseAgentKeyWithFqn() {
        AgentKey agentKey = AgentKey.of("fqn");

        assertEquals(agentKey.getFqn(), "fqn");
        assertNull(agentKey.getVersion());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseAgentKeyFails() {
        AgentKey.of("fqn:1:2");
    }
}
