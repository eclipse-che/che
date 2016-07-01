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
package org.eclipse.che.plugin.docker.machine.integration;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.InitialAuthConfig;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.helper.DefaultNetworkFinder;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.machine.DockerProcess;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Alexander Garagatyi
 */
public class DockerProcessTest {
    private DockerConnectorConfiguration dockerConnectorConfiguration;
    private DockerConnector              docker;
    private String                       container;

    private AtomicInteger pidGenerator = new AtomicInteger(1);

    @BeforeMethod
    public void setUp() throws Exception {

        dockerConnectorConfiguration = new DockerConnectorConfiguration(new InitialAuthConfig(),
                                                                        new DefaultNetworkFinder());
        docker = new DockerConnector(dockerConnectorConfiguration,
                                     new DockerConnectionFactory(dockerConnectorConfiguration),
                                     new DockerRegistryAuthResolver(null));

        final ContainerCreated containerCreated = docker.createContainer(new ContainerConfig().withImage("ubuntu")
                                                                                              .withCmd("tail", "-f", "/dev/null"),
                                                                         null);
        container = containerCreated.getId();
        docker.startContainer(containerCreated.getId(), null);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (container != null) {
            docker.stopContainer(container, 2, TimeUnit.SECONDS);
            docker.removeContainer(container, true, true);
        }
    }

    /**
     * This test requires TCP access to docker API to get timeout exception.<br>
     * If default access to docker is UNIX socket try to reconfigure docker connector for this test.<br>
     * This test may fail if system doesn't allow such access.
     */
    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "Command output read timeout is reached. Process is still running and has id \\d+ inside machine")
    public void shouldThrowErrorWithRealPIDIfSocketTimeoutExceptionHappens() throws Exception {
        DockerConnectorConfiguration dockerConnectorConfiguration = this.dockerConnectorConfiguration;
        DockerConnector docker = this.docker;
        if ("unix".equals(dockerConnectorConfiguration.getDockerDaemonUri().getScheme())) {
            // access through unix socket - reconfigure to use tcp
            dockerConnectorConfiguration = new DockerConnectorConfiguration(new URI("http://localhost:2375"),
                                                                            null,
                                                                            new InitialAuthConfig(),
                                                                            new DefaultNetworkFinder());
            docker = new DockerConnector(dockerConnectorConfiguration,
                                         new DockerConnectionFactory(dockerConnectorConfiguration),
                                         new DockerRegistryAuthResolver(null));
        }
        Command command = new CommandImpl("tailf", "tail -f /dev/null", "mvn");
        final DockerProcess dockerProcess = new DockerProcess(docker,
                                                              command,
                                                              container,
                                                              "outputChannel",
                                                              "/tmp/chetests",
                                                              pidGenerator.incrementAndGet());

        dockerProcess.start(new SOUTLineConsumer());
    }

    static class SOUTLineConsumer implements LineConsumer {
        @Override
        public void writeLine(String line) throws IOException {
            System.out.println(line);
        }

        @Override
        public void close() throws IOException {
        }
    }
}
