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
package org.eclipse.che.plugin.docker.client;

import com.google.common.io.CharStreams;

import org.eclipse.che.plugin.docker.client.connection.CloseConnectionInputStream;
import org.eclipse.che.plugin.docker.client.connection.DockerConnection;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.connection.DockerResponse;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Anton Korneta
 * @author Mykola Morhun
 */
@Listeners(MockitoTestNGListener.class)
public class DockerConnectorTest {

    private DockerConnector dockerConnector;

    @Mock
    private DockerConnectorConfiguration dockerConnectorConfiguration;
    @Mock
    private DockerConnectionFactory      dockerConnectionFactory;
    @Mock
    private DockerConnection             dockerConnection;
    @Mock
    private DockerResponse               dockerResponse;
    @Mock
    private ProgressMonitor              progressMonitor;
    @Mock
    private InitialAuthConfig            initialAuthConfig;

    @BeforeMethod
    public void setup() throws IOException {
        when(dockerConnectionFactory.openConnection(any(URI.class))).thenReturn(dockerConnection);
        when(dockerConnection.method(any())).thenReturn(dockerConnection);
        when(dockerConnection.entity(any(InputStream.class))).thenReturn(dockerConnection);
        when(dockerConnection.headers(any())).thenReturn(dockerConnection);
        when(dockerConnection.query(any(), anyVararg())).thenReturn(dockerConnection);
        when(dockerConnection.path(anyString())).thenReturn(dockerConnection);
        when(dockerConnection.request()).thenReturn(dockerResponse);
        when(dockerConnectorConfiguration.getAuthConfigs()).thenReturn(initialAuthConfig);

        dockerConnector = new DockerConnector(dockerConnectorConfiguration, dockerConnectionFactory);
    }

    @Test
    public void shouldGetResourcesFromContainer() throws IOException {
        String resource = "stream data";
        when(dockerResponse.getStatus()).thenReturn(200);
        when(dockerResponse.getInputStream())
                .thenReturn(new CloseConnectionInputStream(new ByteArrayInputStream(resource.getBytes()), dockerConnection));

        String response = CharStreams.toString(new InputStreamReader(dockerConnector.getResource("id", "path")));

        assertEquals(response, resource);
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Error response from docker API, status: 500, message: Error")
    public void shouldProduceAnErrorWhenGetsResourcesFromContainer() throws IOException {
        String msg = "Error";
        when(dockerResponse.getStatus()).thenReturn(500);
        when(dockerResponse.getInputStream())
                .thenReturn(new CloseConnectionInputStream(new ByteArrayInputStream(msg.getBytes()), dockerConnection));

        dockerConnector.getResource("id", "path");
    }

    @Test
    public void shouldPutResourcesIntoContainer() throws IOException {
        String file = "stream data";
        when(dockerResponse.getStatus()).thenReturn(200);
        InputStream source = new CloseConnectionInputStream(new ByteArrayInputStream(file.getBytes()), dockerConnection);

        dockerConnector.putResource("id", "path", source, false);
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Error response from docker API, status: 500, message: Error")
    public void shouldProduceAnErrorWhenPutsResourcesIntoContainer() throws IOException {
        String msg = "Error";
        when(dockerResponse.getStatus()).thenReturn(500);
        when(dockerResponse.getInputStream())
                .thenReturn(new ByteArrayInputStream(msg.getBytes()));
        InputStream source = new CloseConnectionInputStream(new ByteArrayInputStream(msg.getBytes()), dockerConnection);

        dockerConnector.putResource("id", "path", source, false);
    }

    @Test
    public void shouldParseDigestFromDockerPushOutput() throws IOException, InterruptedException {
        String digest = "hash:1234567890";
        String repository = "repository";
        String tag = "tag";
        String registry = "registry";

        String dockerPushOutput = "{\"progress\":\"[=====>              ] 25%\"}\n" +
                                  "{\"status\":\"Image already exists\"}\n" +
                                  "{\"progress\":\"[===============>    ] 75%\"}\n" +
                                  "{\"status\":\"" + tag + ": digest: " + digest + " size: 12345\"}";

        when(initialAuthConfig.getAuthConfigHeader()).thenReturn("authHeader");
        when(dockerResponse.getStatus()).thenReturn(200);
        when(dockerResponse.getInputStream()).thenReturn(new ByteArrayInputStream(dockerPushOutput.getBytes()));

        assertEquals(digest, dockerConnector.push(repository, tag, registry, progressMonitor));
    }

}
