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

import org.eclipse.che.api.agent.server.AgentRegistryUrlProvider;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.dto.server.JsonArrayImpl;
import org.everrest.assured.EverrestJetty;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class AgentRegistryImplTest {

    @SuppressWarnings("unused")
    private RegistryService          service;
    @Mock
    private AgentRegistryUrlProvider urlProvider;
    private AgentRegistryImpl        agentRegistry;

    @BeforeMethod
    public void setUp(ITestContext context) throws Exception {
        agentRegistry = new AgentRegistryImpl(urlProvider, new DefaultHttpJsonRequestFactory());

        final Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        when(urlProvider.getAgentUrl(any(AgentKey.class))).thenAnswer(new Answer<URL>() {
            @Override
            public URL answer(InvocationOnMock invocation) throws Throwable {
                AgentKey agentKey = (AgentKey)invocation.getArguments()[0];
                if (agentKey.getVersion() == null) {
                    return new URL("http://localhost:" + port + "/rest/registry/agent/" + agentKey.getName());
                } else {
                    return new URL("http://localhost:" + port + "/rest/registry/agent/" + agentKey.getName() + "/" + agentKey.getVersion());
                }
            }
        });
        when(urlProvider.getAgentVersionsUrl(anyString())).thenAnswer(new Answer<URL>() {
            @Override
            public URL answer(InvocationOnMock invocation) throws Throwable {
                String name = (String)invocation.getArguments()[0];
                return new URL("http://localhost:" + port + "/rest/registry/updates/" + name);
            }
        });

        service = new RegistryService();
    }

    @Test
    public void testCreateSpecificVersionAgent() throws Exception {
        Agent agent = agentRegistry.getAgent(new AgentKeyImpl("org.eclipse.che.ws-agent", "1.0"));

        assertEquals(agent.getName(), "org.eclipse.che.ws-agent");
        assertEquals(agent.getVersion(), "1.0");
    }

    @Test
    public void testCreateLatestVersionAgent() throws Exception {
        Agent agent = agentRegistry.getAgent(new AgentKeyImpl("org.eclipse.che.ws-agent"));

        assertEquals(agent.getName(), "org.eclipse.che.ws-agent");
        assertEquals(agent.getVersion(), "2.0");
    }


    @Test(expectedExceptions = AgentException.class)
    public void testGetConfigShouldThrowExceptionIfAgentNotFound() throws Exception {
        agentRegistry.getAgent(new AgentKeyImpl("terminal", "1.0"));
    }

    @Test
    public void testGetAgentsVersion() throws Exception {
        Collection<String> versions = agentRegistry.getVersions("org.eclipse.che.ws-agent");

        assertEquals(versions.size(), 1);
        assertTrue(versions.contains("1.0.0"));
    }

    @Test(expectedExceptions = AgentNotFoundException.class)
    public void testGetAgentsVersionShouldFailsIfAgentUnknown() throws Exception {
        agentRegistry.getVersions("terminal");
    }

    @Path("registry")
    public class RegistryService {

        @GET
        @Path("agent/{artifact}/{version}")
        @Produces(MediaType.APPLICATION_OCTET_STREAM)
        public Response getAgent(@PathParam("artifact") String artifact, @PathParam("version") String version) throws IOException {
            return doGetAgent(artifact, version);
        }


        @GET
        @Path("agent/{artifact}")
        @Produces(MediaType.APPLICATION_OCTET_STREAM)
        public Response getLatestAgent(@PathParam("artifact") String artifact) throws IOException {
            return doGetAgent(artifact, "2.0");
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("/updates/{artifact}")
        public Response getUpdates(@PathParam("artifact") final String artifact) {
            if (!artifact.endsWith("org.eclipse.che.ws-agent")) {
                return Response.status(Response.Status.NOT_FOUND).entity("{ \"message\" : \"not found\" }").build();
            }
            return Response.status(Response.Status.OK).entity(new JsonArrayImpl<>(singletonList("1.0.0"))).build();
        }

        private Response doGetAgent(String artifact, String version) throws IOException {
            if (!artifact.endsWith("org.eclipse.che.ws-agent")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String content = format("{ \"name\" : \"%s\", \"version\" : \"%s\"}", artifact, version);
            java.nio.file.Path file = Paths.get(System.getProperty("java.io.tmpdir"), "config.tmp");
            file.toFile().deleteOnExit();

            copy(new ByteArrayInputStream(content.getBytes()), file, StandardCopyOption.REPLACE_EXISTING);

            return Response.ok(file.toFile(), MediaType.APPLICATION_OCTET_STREAM)
                           .header("Content-Length", String.valueOf(Files.size(file)))
                           .header("Content-Disposition", "attachment; filename=" + file.getFileName().toString())
                           .build();
        }
    }
}
