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
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.agent.ExecAgent;
import org.eclipse.che.api.agent.TerminalAgent;
import org.eclipse.che.api.agent.WsAgent;
import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.impl.AgentRegistryImpl;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.InitialAuthConfig;
import org.eclipse.che.plugin.docker.client.NoOpDockerRegistryDynamicAuthResolverImpl;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.helper.DefaultNetworkFinder;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentValidator;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerServicesStartStrategy;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.old.AgentConfigApplier;
import org.eclipse.che.workspace.infrastructure.docker.old.config.provider.DockerExtConfBindingProvider;
import org.eclipse.che.workspace.infrastructure.docker.old.config.provider.ExecAgentVolumeProvider;
import org.eclipse.che.workspace.infrastructure.docker.old.config.provider.TerminalVolumeProvider;
import org.eclipse.che.workspace.infrastructure.docker.old.config.provider.WsAgentVolumeProvider;
import org.eclipse.che.workspace.infrastructure.docker.old.extra.DockerInstanceStopDetector;
import org.eclipse.che.workspace.infrastructure.docker.old.local.LocalCheInfrastructureProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.old.local.node.WorkspaceFolderPathProvider;
import org.eclipse.che.workspace.infrastructure.docker.old.strategy.LocalDockerServerEvaluationStrategy;
import org.eclipse.che.workspace.infrastructure.docker.old.strategy.ServerEvaluationStrategy;
import org.eclipse.che.workspace.infrastructure.docker.old.strategy.ServerEvaluationStrategyProvider;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class IntegrationTest {
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);

    // docker
    private DockerConnectorConfiguration       connectorConfiguration;
    private DockerConnectionFactory            connectionFactory;
    private DockerRegistryAuthResolver         resolver;
    private DockerApiVersionPathPrefixProvider dockerApiVersion;
    private DockerConnector                    docker;

    // agent
    private AgentRegistry      agentRegistry;
    private AgentSorter        agentSorter;
    private AgentConfigApplier agentConfigApplier;

    // other
    private URLRewriter      urlRewriter;
    private RecipeDownloader recipeDownloader;

    // docker runtime
    private DockerEnvironmentParser     parser;
    private DockerEnvironmentValidator  validator;
    private DockerServicesStartStrategy strategy;
    private DockerEnvironmentNormalizer normalizer;
    private DockerServiceStarter        starter;
    private DockerNetworkLifecycle      networkLifecycle;
    private InfrastructureProvisioner   provisioner;

    // desired class
    private DockerRuntimeInfrastructure infra;

    private RuntimeContext context;

    @BeforeMethod
    public void setUp() throws Exception {
        // docker
        InitialAuthConfig initialAuthConfig = new InitialAuthConfig();
        UserSpecificDockerRegistryCredentialsProvider credentialsProvider = mock(UserSpecificDockerRegistryCredentialsProvider.class);
        connectorConfiguration = new DockerConnectorConfiguration(URI.create("unix:///var/run/docker.sock"),
                                                                  null,
                                                                  initialAuthConfig,
                                                                  new DefaultNetworkFinder());
        connectionFactory = new DockerConnectionFactory(connectorConfiguration);
        resolver = new DockerRegistryAuthResolver(initialAuthConfig, new NoOpDockerRegistryDynamicAuthResolverImpl());
        dockerApiVersion = new DockerApiVersionPathPrefixProvider("1.20");
        docker = new DockerConnector(connectorConfiguration, connectionFactory, resolver, dockerApiVersion);

        // agent stuff
        agentRegistry = new AgentRegistryImpl(ImmutableSet.of(new WsAgent(), new ExecAgent(), new TerminalAgent()));
        agentSorter = new AgentSorter(agentRegistry);
        agentConfigApplier = new AgentConfigApplier(agentSorter, agentRegistry);

        // other
        urlRewriter = new TestURLRewriter();
        recipeDownloader = mock(RecipeDownloader.class);
        Pattern recipePattern = Pattern.compile(".*");

        // docker runtime
        DockerContainerNameGenerator dockerContainerNameGenerator = new DockerContainerNameGenerator();
        DockerInstanceStopDetector stopDetector = mock(DockerInstanceStopDetector.class);
        parser = new DockerEnvironmentParser(singletonMap("dockerimage", new DockerImageEnvironmentParser()));
        validator = new DockerEnvironmentValidator();
        strategy = new DockerServicesStartStrategy();
        ServerEvaluationStrategy serverEvaluationStrategy = new LocalDockerServerEvaluationStrategy("localhost", "localhost");
        ServerEvaluationStrategyProvider strategyProvider = new ServerEvaluationStrategyProvider(singletonMap("strategy1", serverEvaluationStrategy), "strategy1");
        normalizer = new DockerEnvironmentNormalizer(recipeDownloader, recipePattern, dockerContainerNameGenerator, 2_000_000_000);
        starter = new DockerServiceStarter(docker,
                                           credentialsProvider,
                                           stopDetector,
                                           emptySet(),
                                           emptySet(),
                                           emptySet(),
                                           emptySet(),
                                           false,
                                           false,
                                           0,
                                           emptySet(),
                                           emptySet(),
                                           false,
                                           -1,
                                           emptySet(),
                                           null,
                                           null,
                                           0,
                                           0,
                                           new WindowsPathEscaper(),
                                           emptySet(),
                                           null,
                                           strategyProvider);
        networkLifecycle = new DockerNetworkLifecycle(docker, null);
        WorkspaceFolderPathProvider workspaceFolderPathProvider = mock(WorkspaceFolderPathProvider.class);
        WsAgentVolumeProvider wsAgentVolumeProvider = mock(WsAgentVolumeProvider.class);
        TerminalVolumeProvider terminalVolumeProvider = mock(TerminalVolumeProvider.class);
        ExecAgentVolumeProvider execAgentVolumeProvider = mock(ExecAgentVolumeProvider.class);
        provisioner = new LocalCheInfrastructureProvisioner(agentConfigApplier,
                                                            workspaceFolderPathProvider,
                                                            new WindowsPathEscaper(),
                                                            "/projects",
                                                            null,
                                                            wsAgentVolumeProvider,
                                                            new DockerExtConfBindingProvider(),
                                                            terminalVolumeProvider,
                                                            execAgentVolumeProvider);

        // desired class
        infra = new DockerRuntimeInfrastructure(parser,
                                                validator,
                                                strategy,
                                                provisioner,
                                                normalizer,
                                                starter,
                                                networkLifecycle,
                                                urlRewriter,
                                                docker,
                                                agentRegistry, agentSorter);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (context != null) {
            context.stop(emptyMap());
        }
    }

    @Test
    public void testWSStart() throws Exception {
        RuntimeIdentity runtimeIdentity = new RuntimeIdentity("testWsId1", "my-test-env", "gaal");
        Environment environment = newDto(EnvironmentDto.class)
                .withRecipe(newDto(RecipeDto.class).withType("dockerimage").withLocation("eclipse/ubuntu_jdk8"))
                .withMachines(singletonMap("ddevM", newDto(MachineConfigDto.class)
                        .withAgents(singletonList("org.eclipse.che.ws-agent"))));

        context = infra.prepare(runtimeIdentity, environment);
        InternalRuntime runtime = context.start(emptyMap());
        assertNotNull(runtime.getMachines());
        LOG.error("{}", runtime.getMachines());
    }

    private class TestURLRewriter implements URLRewriter {
        @Override
        public URL rewriteURL(RuntimeIdentity identity, String name, URL url) throws MalformedURLException {
            return url;
        }
    }
}
