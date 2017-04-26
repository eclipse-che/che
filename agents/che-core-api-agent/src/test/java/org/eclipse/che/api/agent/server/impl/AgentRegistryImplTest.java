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

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.impl.AgentKeyImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class AgentRegistryImplTest {

    private AgentRegistry registry;

    @BeforeMethod
    public void setUp() throws Exception {
        registry = new LocalAgentRegistry(new HashSet<Agent>() {{
            add(DtoFactory.newDto(AgentDto.class).withId("id1").withVersion("v1").withName("id1:v1"));
            add(DtoFactory.newDto(AgentDto.class).withId("id1").withVersion("v2").withName("id1:v2"));
            add(DtoFactory.newDto(AgentDto.class).withId("id2").withName("id2:latest"));
            add(DtoFactory.newDto(AgentDto.class).withId("id3").withVersion("v1").withName("id3:v1"));
            add(DtoFactory.newDto(AgentDto.class).withId("id3").withName("id3:latest"));
        }});
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldNotRegisterAgentWithSameIdAndVersion() throws Exception {
        new LocalAgentRegistry(new HashSet<Agent>() {{
            add(DtoFactory.newDto(AgentDto.class).withId("id1").withVersion("v1").withScript("s1"));
            add(DtoFactory.newDto(AgentDto.class).withId("id1").withVersion("v1").withScript("s2"));
        }});
    }

    @Test(dataProvider = "versions")
    public void shouldReturnVersionsById(String id, Set<String> expectedVersions) throws Exception {
        List<String> versions = registry.getVersions(id);

        assertEquals(versions.size(), expectedVersions.size());
        for (String v : expectedVersions) {
            assertTrue(versions.contains(v));
        }
    }

    @DataProvider(name = "versions")
    public static Object[][] versions() {
        return new Object[][] {{"id1", ImmutableSet.of("v1", "v2")},
                               {"id2", ImmutableSet.of("latest")},
                               {"id3", ImmutableSet.of("v1", "latest")}};
    }

    @Test
    public void shouldReturnAllAgents() throws Exception {
        Collection<Agent> agents = registry.getAgents();

        assertEquals(agents.size(), 5);
    }

    @Test(dataProvider = "AgentKeys")
    public void shouldReturnAgentByIdAndVersion(String id, String version) throws Exception {
        Agent agent = registry.getAgent(new AgentKeyImpl(id, version));

        assertNotNull(agent);
        assertEquals(agent.getName(), String.format("%s:%s", id, (version == null ? "latest" : version)));
    }


    @DataProvider(name = "AgentKeys")
    public static Object[][] AgentKeys() {
        return new String[][] {{"id1", "v1"},
                               {"id1", "v2"},
                               {"id2", null},
                               {"id2", "latest"},
                               {"id3", "v1"},
                               {"id3", null},
                               {"id3", "latest"}};
    }
}
