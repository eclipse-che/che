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

import org.eclipse.che.api.agent.server.AgentException;
import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.shared.model.AgentConfig;
import org.everrest.assured.EverrestJetty;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class AgentRegistryImplTest {

    @SuppressWarnings("unused")
    private TestService service;

    private AgentRegistry agentRegistry;

    @BeforeMethod
    public void setUp(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        agentRegistry = new AgentRegistryImpl("http://localhost:" + port + "/rest/registry/agent/${fqn}/${version}",
                                              "http://localhost:" + port + "/rest/registry/agent/${fqn}");
        service = new TestService();
    }

    @Test
    public void testGetSpecificVersionAgent() throws Exception {
        AgentConfig config = agentRegistry.getConfig("ws-agent", "1.0");

        assertEquals(config.getFqn(), "ws-agent");
        assertEquals(config.getVersion(), "1.0");
    }

    @Test
    public void testGetLatestVersionAgent() throws Exception {
        AgentConfig config = agentRegistry.getConfig("ws-agent");

        assertEquals(config.getFqn(), "ws-agent");
        assertEquals(config.getVersion(), "2.0");
    }


    @Test(expectedExceptions = AgentException.class)
    public void testGetConfigShouldThrowExceptionIfAgentNotFound() throws Exception {
        agentRegistry.getConfig("terminal", "1.0");
    }

    @Path("registry")
    public class TestService {

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

        private Response doGetAgent(String artifact, String version) throws IOException {
            if (!artifact.endsWith("ws-agent")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String content = format("{ \"fqn\" : \"%s\", \"version\" : \"%s\"}", artifact, version);
            java.nio.file.Path file = Paths.get(System.getProperty("java.io.tmpdir"), "config.tmp");
            copy(new ByteArrayInputStream(content.getBytes()), file, StandardCopyOption.REPLACE_EXISTING);

            return Response.ok(file.toFile(), MediaType.APPLICATION_OCTET_STREAM)
                           .header("Content-Length", String.valueOf(Files.size(file)))
                           .header("Content-Disposition", "attachment; filename=" + file.getFileName().toString())
                           .build();
        }
    }
}
