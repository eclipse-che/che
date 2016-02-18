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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Channels;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineState;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.ChannelsImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineStateImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Listeners(MockitoTestNGListener.class)
public class DockerInstanceProviderTest {
    private static final String PROJECT_FOLDER_PATH = "/projects";
    private static final String CONTAINER_ID        = "containerId";
    private static final String WORKSPACE_ID        = "wsId";
    private static final String DISPLAY_NAME        = "DisplayName";
    private static final String USER_TOKEN          = "userToken";

    @Mock
    private DockerConnector dockerConnector;

    @Mock
    private DockerConnectorConfiguration dockerConnectorConfiguration;

    @Mock
    private DockerMachineFactory dockerMachineFactory;

    @Mock
    private DockerInstanceStopDetector dockerInstanceStopDetector;

    @Mock
    private DockerNode dockerNode;

    @Mock
    private WorkspaceFolderPathProvider workspaceFolderPathProvider;

    private DockerInstanceProvider dockerInstanceProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        when(dockerConnectorConfiguration.getDockerHostIp()).thenReturn("123.123.123.123");

        dockerInstanceProvider = spy(new DockerInstanceProvider(dockerConnector,
                                                                dockerConnectorConfiguration,
                                                                dockerMachineFactory,
                                                                dockerInstanceStopDetector,
                                                                Collections.<ServerConf>emptySet(),
                                                                Collections.<ServerConf>emptySet(),
                                                                Collections.<String>emptySet(),
                                                                Collections.<String>emptySet(),
                                                                null,
                                                                workspaceFolderPathProvider,
                                                                PROJECT_FOLDER_PATH,
                                                                false,
                                                                Collections.<String>emptySet(),
                                                                Collections.<String>emptySet()));

        EnvironmentContext envCont = new EnvironmentContext();
        envCont.setUser(new UserImpl("user", "userId", USER_TOKEN, null, false));
        EnvironmentContext.setCurrent(envCont);

        when(dockerMachineFactory.createNode(anyString(), anyString())).thenReturn(dockerNode);
        when(dockerConnector.createContainer(any(ContainerConfig.class), anyString()))
                .thenReturn(new ContainerCreated(CONTAINER_ID, new String[0]));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test
    public void shouldReturnTypeDocker() throws Exception {
        assertEquals(dockerInstanceProvider.getType(), "docker");
    }

    @Test
    public void shouldReturnRecipeTypesDockerfile() throws Exception {
        assertEquals(dockerInstanceProvider.getRecipeTypes(), Collections.singleton("Dockerfile"));
    }

    // TODO add tests for instance snapshot removal

    @Test
    public void shouldBuildDockerfileOnInstanceCreationFromRecipe() throws Exception {
        String generatedContainerId = "genContainerId";
        doReturn(generatedContainerId).when(dockerInstanceProvider).generateContainerName(eq(WORKSPACE_ID), eq(DISPLAY_NAME));


        createInstanceFromRecipe();


        verify(dockerConnector).buildImage(eq("eclipse-che/" + generatedContainerId),
                                           any(ProgressMonitor.class),
                                           any(AuthConfigs.class),
                                           anyBoolean(),
                                           anyVararg());
    }

    @Test
    public void shouldPullDockerImageOnInstanceCreationFromSnapshot() throws Exception {
        String repo = "repo";
        String tag = "tag";
        String registry = "localhost:1234";


        createInstanceFromSnapshot(repo, tag, registry);


        verify(dockerConnector).pull(eq(repo), eq(tag), eq(registry), any(ProgressMonitor.class));
    }

    @Test
    public void shouldReTagBuiltImageWithPredictableOnInstanceCreationFromRecipe() throws Exception {
        String generatedContainerId = "genContainerId";
        doReturn(generatedContainerId).when(dockerInstanceProvider).generateContainerName(WORKSPACE_ID, DISPLAY_NAME);
        String repo = "repo1";
        String registry = "registry1";
        String tag = "tag1";


        createInstanceFromSnapshot(repo, tag, registry);


        verify(dockerConnector).tag(eq(registry + "/" + repo + ":" + tag), eq("eclipse-che/" + generatedContainerId), eq(null));
        verify(dockerConnector).removeImage(eq(registry + "/" + repo + ":" + tag), eq(false));
    }

    @Test
    public void shouldCreateContainerOnInstanceCreationFromRecipe() throws Exception {
        String generatedContainerId = "genContainerId";
        doReturn(generatedContainerId).when(dockerInstanceProvider).generateContainerName(WORKSPACE_ID, DISPLAY_NAME);


        createInstanceFromRecipe();


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(argumentCaptor.getValue().getImage(), "eclipse-che/" + generatedContainerId);
    }

    @Test
    public void shouldStartContainerOnCreateInstanceFromRecipe() throws Exception {
        createInstanceFromRecipe();

        verify(dockerConnector).startContainer(eq(CONTAINER_ID), any(HostConfig.class));
    }

    @Test
    public void shouldCreateContainerOnInstanceCreationFromSnapshot() throws Exception {
        String generatedContainerId = "genContainerId";
        doReturn(generatedContainerId).when(dockerInstanceProvider).generateContainerName(WORKSPACE_ID, DISPLAY_NAME);
        createInstanceFromSnapshot();


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(argumentCaptor.getValue().getImage(), "eclipse-che/" + generatedContainerId);
    }

    @Test
    public void shouldStartContainerOnCreateInstanceFromSnapshot() throws Exception {
        createInstanceFromSnapshot();

        verify(dockerConnector).startContainer(eq(CONTAINER_ID), any(HostConfig.class));
    }

    @Test
    public void shouldCallCreationDockerInstanceWithFactoryOnCreateInstanceFromSnapshot() throws Exception {
        String generatedContainerId = "genContainerId";
        doReturn(generatedContainerId).when(dockerInstanceProvider).generateContainerName(eq(WORKSPACE_ID), eq(DISPLAY_NAME));

        final MachineSourceImpl machineSource = new MachineSourceImpl("type", "location");
        final MachineStateImpl machineState = new MachineStateImpl(false,
                                                                   DISPLAY_NAME,
                                                                   "machineType",
                                                                   machineSource,
                                                                   new LimitsImpl(64),
                                                                   "machineId",
                                                                   new ChannelsImpl("chan1", "chan2"),
                                                                   WORKSPACE_ID,
                                                                   "userId",
                                                                   "envName",
                                                                   MachineStatus.CREATING);


        createInstanceFromSnapshot(machineState);


        verify(dockerMachineFactory).createInstance(eq(machineState),
                                                    eq(CONTAINER_ID),
                                                    eq("eclipse-che/" + generatedContainerId),
                                                    eq(dockerNode),
                                                    any(LineConsumer.class));
    }

    @Test
    public void shouldCallCreationDockerInstanceWithFactoryOnCreateInstanceFromRecipe() throws Exception {
        String generatedContainerId = "genContainerId";
        doReturn(generatedContainerId).when(dockerInstanceProvider).generateContainerName(eq(WORKSPACE_ID), eq(DISPLAY_NAME));

        final MachineSourceImpl machineSource = new MachineSourceImpl("type", "location");
        final Recipe recipe = new RecipeImpl().withType("Dockerfile").withScript("FROM busybox");
        final MachineStateImpl machineState = new MachineStateImpl(false,
                                                                   DISPLAY_NAME,
                                                                   "machineType",
                                                                   machineSource,
                                                                   new LimitsImpl(64),
                                                                   "machineId",
                                                                   new ChannelsImpl("chan1", "chan2"),
                                                                   WORKSPACE_ID,
                                                                   "userId",
                                                                   "envName",
                                                                   MachineStatus.CREATING);

        createInstanceFromRecipe(recipe, machineState);


        verify(dockerMachineFactory).createInstance(eq(machineState),
                                                    eq(CONTAINER_ID),
                                                    eq("eclipse-che/" + generatedContainerId),
                                                    eq(dockerNode),
                                                    any(LineConsumer.class));
    }

    @Test
    public void shouldBindWorkspaceOnDevInstanceCreationFromRecipe() throws Exception {
        final boolean isDev = true;
        final String hostProjectsFolder = "/tmp/projects";

        when(dockerNode.getProjectsFolder()).thenReturn(hostProjectsFolder);

        createInstanceFromRecipe(isDev, WORKSPACE_ID);

        verify(dockerNode).bindWorkspace();
    }

    @Test
    public void shouldBindWorkspaceOnDevInstanceCreationFromSnapshot() throws Exception {
        final boolean isDev = true;
        final String hostProjectsFolder = "/tmp/projects";

        when(dockerNode.getProjectsFolder()).thenReturn(hostProjectsFolder);

        createInstanceFromSnapshot(isDev, WORKSPACE_ID);

        verify(dockerNode).bindWorkspace();
    }

    @Test
    public void shouldNotBindWorkspaceOnNonDevInstanceCreationFromRecipe() throws Exception {
        final boolean isDev = false;

        when(dockerNode.getProjectsFolder()).thenReturn("/tmp/projects");

        createInstanceFromRecipe(isDev, WORKSPACE_ID);

        verify(dockerNode, never()).bindWorkspace();
    }

    @Test
    public void shouldNotBindWorkspaceOnNonDevInstanceCreationFromSnapshot() throws Exception {
        final boolean isDev = false;

        when(dockerNode.getProjectsFolder()).thenReturn("/tmp/projects");

        createInstanceFromSnapshot(isDev, WORKSPACE_ID);

        verify(dockerNode, never()).bindWorkspace();
    }

    @Test
    public void shouldSetMemorySizeInContainersOnInstanceCreationFromRecipe() throws Exception {
        int memorySizeMB = 234;


        createInstanceFromRecipe(memorySizeMB);


        ArgumentCaptor<ContainerConfig> createContainerCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(createContainerCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));
        // docker accepts memory size in bytes
        assertEquals(createContainerCaptor.getValue().getHostConfig().getMemory(), memorySizeMB * 1024 * 1024);
    }

    @Test
    public void shouldSetMemorySizeInContainersOnInstanceCreationFromSnapshot() throws Exception {
        int memorySizeMB = 234;


        createInstanceFromSnapshot(memorySizeMB);


        ArgumentCaptor<ContainerConfig> createContainerCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(createContainerCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));
        // docker accepts memory size in bytes
        assertEquals(createContainerCaptor.getValue().getHostConfig().getMemory(), memorySizeMB * 1024 * 1024);
    }

    @Test
    public void shouldDisableSwapMemorySizeInContainersOnInstanceCreationFromRecipe() throws Exception {
        createInstanceFromRecipe();

        ArgumentCaptor<ContainerConfig> createContainerCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(createContainerCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));
        assertEquals(createContainerCaptor.getValue().getHostConfig().getMemorySwap(), -1);
    }

    @Test
    public void shouldDisableSwapMemorySizeInContainersOnInstanceCreationFromSnapshot() throws Exception {
        createInstanceFromSnapshot();

        ArgumentCaptor<ContainerConfig> createContainerCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(createContainerCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));
        assertEquals(createContainerCaptor.getValue().getHostConfig().getMemorySwap(), -1);
    }

    @Test
    public void shouldAddCommonAndDevLabelsToContainerOnDevInstanceCreationFromRecipe() throws Exception {
        final Map<String, String> expectedLabels = new HashMap<>();
        final Set<ServerConf> commonServers = new HashSet<>(asList(new ServerConf("reference1", "8080", "http"),
                                                                   new ServerConf("reference2", "8081", "ftp")));
        for (ServerConf server : commonServers) {
            expectedLabels.put("che:server:" + server.getPort() + ":ref", server.getRef());
            expectedLabels.put("che:server:" + server.getPort() + ":protocol", server.getProtocol());
        }

        final Set<ServerConf> devServers = new HashSet<>(asList(new ServerConf("reference3", "8082", "https"),
                                                                new ServerConf("reference4", "8083", "sftp")));
        for (ServerConf server : devServers) {
            expectedLabels.put("che:server:" + server.getPort() + ":ref", server.getRef());
            expectedLabels.put("che:server:" + server.getPort() + ":protocol", server.getProtocol());
        }

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            devServers,
                                                            commonServers,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        final boolean isDev = true;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(argumentCaptor.getValue().getLabels(), expectedLabels);
    }

    @Test
    public void shouldAddOnlyCommonLabelsToContainerOnNonDevInstanceCreationFromRecipe() throws Exception {
        final Map<String, String> expectedLabels = new HashMap<>();
        final Set<ServerConf> commonServers = new HashSet<>(asList(new ServerConf("reference1", "8080", "http"),
                                                                   new ServerConf("reference2", "8081", "ftp")));
        for (ServerConf server : commonServers) {
            expectedLabels.put("che:server:" + server.getPort() + ":ref", server.getRef());
            expectedLabels.put("che:server:" + server.getPort() + ":protocol", server.getProtocol());
        }

        final Set<ServerConf> devServers = new HashSet<>(asList(new ServerConf("reference3", "8082", "https"),
                                                                new ServerConf("reference4", "8083", "sftp")));

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            devServers,
                                                            commonServers,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        final boolean isDev = false;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(argumentCaptor.getValue().getLabels(), expectedLabels);
    }

    @Test
    public void shouldAddCommonAndDevLabelsToContainerOnDevInstanceCreationFromSnapshot() throws Exception {
        final Map<String, String> expectedLabels = new HashMap<>();
        final Set<ServerConf> commonServers = new HashSet<>(asList(new ServerConf("reference1", "8080", "http"),
                                                                   new ServerConf("reference2", "8081", "ftp")));
        for (ServerConf server : commonServers) {
            expectedLabels.put("che:server:" + server.getPort() + ":ref", server.getRef());
            expectedLabels.put("che:server:" + server.getPort() + ":protocol", server.getProtocol());
        }

        final Set<ServerConf> devServers = new HashSet<>(asList(new ServerConf("reference3", "8082", "https"),
                                                                new ServerConf("reference4", "8083", "sftp")));
        for (ServerConf server : devServers) {
            expectedLabels.put("che:server:" + server.getPort() + ":ref", server.getRef());
            expectedLabels.put("che:server:" + server.getPort() + ":protocol", server.getProtocol());
        }

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            devServers,
                                                            commonServers,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        final boolean isDev = true;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(argumentCaptor.getValue().getLabels(), expectedLabels);
    }

    @Test
    public void shouldAddOnlyCommonLabelsToContainerOnNonDevInstanceCreationFromSnapshot() throws Exception {
        final Map<String, String> expectedLabels = new HashMap<>();
        final Set<ServerConf> commonServers = new HashSet<>(asList(new ServerConf("reference1", "8080", "http"),
                                                                   new ServerConf("reference2", "8081", "ftp")));
        for (ServerConf server : commonServers) {
            expectedLabels.put("che:server:" + server.getPort() + ":ref", server.getRef());
            expectedLabels.put("che:server:" + server.getPort() + ":protocol", server.getProtocol());
        }

        final Set<ServerConf> devServers = new HashSet<>(asList(new ServerConf("reference3", "8082", "https"),
                                                                new ServerConf("reference4", "8083", "sftp")));

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            devServers,
                                                            commonServers,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        final boolean isDev = false;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(argumentCaptor.getValue().getLabels(), expectedLabels);
    }

    @Test
    public void shouldExposeCommonAndDevPortsToContainerOnDevInstanceCreationFromRecipe() throws Exception {
        Map<String, Map<String, String>> expectedExposedPorts = new HashMap<>();
        final Set<ServerConf> commonServers = new HashSet<>(asList(new ServerConf("reference1", "8080", "http"),
                                                                   new ServerConf("reference2", "8081", "ftp")));
        for (ServerConf server : commonServers) {
            expectedExposedPorts.put(server.getPort(), Collections.<String, String>emptyMap());
        }

        final Set<ServerConf> devServers = new HashSet<>(asList(new ServerConf("reference3", "8082", "https"),
                                                                new ServerConf("reference4", "8083", "sftp")));
        for (ServerConf server : devServers) {
            expectedExposedPorts.put(server.getPort(), Collections.<String, String>emptyMap());
        }

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            devServers,
                                                            commonServers,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        final boolean isDev = true;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());

        assertEquals(argumentCaptor.getValue().getExposedPorts(), expectedExposedPorts);
    }

    @Test
    public void shouldExposeOnlyCommonPortsToContainerOnNonDevInstanceCreationFromRecipe() throws Exception {
        Map<String, Map<String, String>> expectedExposedPorts = new HashMap<>();
        final Set<ServerConf> commonServers = new HashSet<>(asList(new ServerConf("reference1", "8080", "http"),
                                                                   new ServerConf("reference2", "8081", "ftp")));
        for (ServerConf server : commonServers) {
            expectedExposedPorts.put(server.getPort(), Collections.<String, String>emptyMap());
        }

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            commonServers,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        final boolean isDev = false;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());

        assertEquals(argumentCaptor.getValue().getExposedPorts(), expectedExposedPorts);
    }

    @Test
    public void shouldExposeCommonAndDevPortsToContainerOnDevInstanceCreationFromSnapshot() throws Exception {
        Map<String, Map<String, String>> expectedExposedPorts = new HashMap<>();
        final Set<ServerConf> commonServers = new HashSet<>(asList(new ServerConf("reference1", "8080", "http"),
                                                                   new ServerConf("reference2", "8081", "ftp")));
        for (ServerConf server : commonServers) {
            expectedExposedPorts.put(server.getPort(), Collections.<String, String>emptyMap());
        }

        final Set<ServerConf> devServers = new HashSet<>(asList(new ServerConf("reference3", "8082", "https"),
                                                                new ServerConf("reference4", "8083", "sftp")));
        for (ServerConf server : devServers) {
            expectedExposedPorts.put(server.getPort(), Collections.<String, String>emptyMap());
        }

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            devServers,
                                                            commonServers,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        final boolean isDev = true;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());

        assertEquals(argumentCaptor.getValue().getExposedPorts(), expectedExposedPorts);
    }

    @Test
    public void shouldExposeOnlyCommonPortsToContainerOnNonDevInstanceCreationFromSnapshot() throws Exception {
        Map<String, Map<String, String>> expectedExposedPorts = new HashMap<>();
        final Set<ServerConf> commonServers = new HashSet<>(asList(new ServerConf("reference1", "8080", "http"),
                                                                   new ServerConf("reference2", "8081", "ftp")));
        for (ServerConf server : commonServers) {
            expectedExposedPorts.put(server.getPort(), Collections.<String, String>emptyMap());
        }

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            commonServers,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        final boolean isDev = false;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());

        assertEquals(argumentCaptor.getValue().getExposedPorts(), expectedExposedPorts);
    }

    @Test
    public void shouldBindProjectsFSVolumeToContainerOnDevInstanceCreationFromRecipe() throws Exception {
        final String expectedHostPathOfProjects = "/tmp/projects";
        String[] expectedVolumes = new String[]{expectedHostPathOfProjects + ":/projects"};

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(expectedHostPathOfProjects);

        final boolean isDev = true;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        assertEquals(argumentCaptor.getValue().getHostConfig().getBinds(), expectedVolumes);
    }

    @Test
    public void shouldBindProjectsFSVolumeToContainerOnDevInstanceCreationFromSnapshot() throws Exception {
        final String expectedHostPathOfProjects = "/tmp/projects";
        final String[] expectedVolumes = new String[]{expectedHostPathOfProjects + ":/projects"};

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(expectedHostPathOfProjects);

        final boolean isDev = true;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        assertEquals(argumentCaptor.getValue().getHostConfig().getBinds(), expectedVolumes);
    }

    @Test
    public void shouldNotBindProjectsFSVolumeToContainerOnNonDevInstanceCreationFromRecipe() throws Exception {
        String[] expectedVolumes = new String[0];

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(dockerNode.getProjectsFolder()).thenReturn("/tmp/projects");

        final boolean isDev = false;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        assertEquals(argumentCaptor.getValue().getHostConfig().getBinds(), expectedVolumes);
    }

    @Test
    public void shouldNotBindProjectsFSVolumeToContainerOnNonDevInstanceCreationFromSnapshot() throws Exception {
        String[] expectedVolumes = new String[0];

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(dockerNode.getProjectsFolder()).thenReturn("/tmp/projects");

        final boolean isDev = false;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        assertEquals(argumentCaptor.getValue().getHostConfig().getBinds(), expectedVolumes);
    }

    @Test
    public void shouldBindCommonAndDevVolumesToContainerOnDevInstanceCreationFromRecipe() throws Exception {
        final String expectedHostPathOfProjects = "/tmp/projects";
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes = new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        final ArrayList<String> expectedVolumes = new ArrayList<>();
        expectedVolumes.addAll(devVolumes);
        expectedVolumes.addAll(commonVolumes);
        expectedVolumes.add(expectedHostPathOfProjects + ":/projects");

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            devVolumes,
                                                            commonVolumes,
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(expectedHostPathOfProjects);
        final boolean isDev = true;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        final String[] actualBinds = argumentCaptor.getValue().getHostConfig().getBinds();
        assertEquals(actualBinds.length, expectedVolumes.size());
        assertEquals(new HashSet<>(asList(actualBinds)), new HashSet<>(expectedVolumes));
    }

    @Test
    public void shouldBindCommonAndDevVolumesToContainerOnDevInstanceCreationFromSnapshot() throws Exception {
        final String expectedHostPathOfProjects = "/tmp/projects";
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes = new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        final ArrayList<String> expectedVolumes = new ArrayList<>();
        expectedVolumes.addAll(devVolumes);
        expectedVolumes.addAll(commonVolumes);
        expectedVolumes.add(expectedHostPathOfProjects + ":/projects");

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            devVolumes,
                                                            commonVolumes,
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(expectedHostPathOfProjects);

        final boolean isDev = true;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        final String[] actualBinds = argumentCaptor.getValue().getHostConfig().getBinds();
        assertEquals(actualBinds.length, expectedVolumes.size());
        assertEquals(new HashSet<>(asList(actualBinds)), new HashSet<>(expectedVolumes));
    }

    @Test
    public void shouldBindCommonVolumesOnlyToContainerOnNonDevInstanceCreationFromRecipe() throws Exception {
        final String expectedHostPathOfProjects = "/tmp/projects";
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes = new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        final ArrayList<String> expectedVolumes = new ArrayList<>();
        expectedVolumes.addAll(commonVolumes);

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            devVolumes,
                                                            commonVolumes,
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(dockerNode.getProjectsFolder()).thenReturn(expectedHostPathOfProjects);

        final boolean isDev = false;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        final String[] actualBinds = argumentCaptor.getValue().getHostConfig().getBinds();
        assertEquals(actualBinds.length, expectedVolumes.size());
        assertEquals(new HashSet<>(asList(actualBinds)), new HashSet<>(expectedVolumes));
    }

    @Test
    public void shouldAddExtraHostOnDevInstanceCreationFromRecipe() throws Exception {
        //given
        final String expectedHostPathOfProjects = "/tmp/projects";
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes = new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            devVolumes,
                                                            commonVolumes,
                                                            "dev.box.com:192.168.0.1",
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(dockerNode.getProjectsFolder()).thenReturn(expectedHostPathOfProjects);

        final boolean isDev = true;

        //when
        createInstanceFromRecipe(isDev);

        //then
        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        final String[] extraHosts = argumentCaptor.getValue().getHostConfig().getExtraHosts();
        assertEquals(extraHosts.length, 2);
        assertEquals(extraHosts[0], "dev.box.com:192.168.0.1");
    }

    @Test
    public void shouldAddExtraHostOnDevInstanceCreationFromSnapshot() throws Exception {
        //given
        final String expectedHostPathOfProjects = "/tmp/projects";
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes = new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            devVolumes,
                                                            commonVolumes,
                                                            "dev.box.com:192.168.0.1,codenvy.com.com:185",
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(dockerNode.getProjectsFolder()).thenReturn(expectedHostPathOfProjects);
        final boolean isDev = true;

        //when
        createInstanceFromSnapshot(isDev);
        //then

        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        final String[] extraHosts = argumentCaptor.getValue().getHostConfig().getExtraHosts();
        assertEquals(extraHosts.length, 3);
        assertEquals(extraHosts[0], "dev.box.com:192.168.0.1");
        assertEquals(extraHosts[1], "codenvy.com.com:185");
    }

    @Test
    public void shouldAddExtraHostOnNonDevInstanceCreationFromRecipe() throws Exception {
        //given
        final String expectedHostPathOfProjects = "/tmp/projects";
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes = new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            devVolumes,
                                                            commonVolumes,
                                                            "dev.box.com:192.168.0.1",
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(dockerNode.getProjectsFolder()).thenReturn(expectedHostPathOfProjects);

        final boolean isDev = false;

        //when
        createInstanceFromRecipe(isDev);

        //then
        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        final String[] extraHosts = argumentCaptor.getValue().getHostConfig().getExtraHosts();
        assertEquals(extraHosts.length, 2);
        assertEquals(extraHosts[0], "dev.box.com:192.168.0.1");
    }

    @Test
    public void shouldAddExtraHostOnNonDevInstanceCreationFromSnapshot() throws Exception {
        //given
        final String expectedHostPathOfProjects = "/tmp/projects";
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes = new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            devVolumes,
                                                            commonVolumes,
                                                            "dev.box.com:192.168.0.1,codenvy.com.com:185",
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(dockerNode.getProjectsFolder()).thenReturn(expectedHostPathOfProjects);
        final boolean isDev = false;

        //when
        createInstanceFromSnapshot(isDev);
        //then

        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        final String[] extraHosts = argumentCaptor.getValue().getHostConfig().getExtraHosts();
        assertEquals(extraHosts.length, 3);
        assertEquals(extraHosts[0], "dev.box.com:192.168.0.1");
        assertEquals(extraHosts[1], "codenvy.com.com:185");
    }

    @Test
    public void shouldBindCommonVolumesOnlyToContainerOnNonDevInstanceCreationFromSnapshot() throws Exception {
        final String expectedHostPathOfProjects = "/tmp/projects";
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes = new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        final ArrayList<String> expectedVolumes = new ArrayList<>();
        expectedVolumes.addAll(commonVolumes);

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.emptySet(),
                                                            Collections.emptySet(),
                                                            devVolumes,
                                                            commonVolumes,
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet());

        when(dockerNode.getProjectsFolder()).thenReturn(expectedHostPathOfProjects);

        final boolean isDev = false;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        verify(dockerConnector).startContainer(anyString(), eq(null));

        final String[] actualBinds = argumentCaptor.getValue().getHostConfig().getBinds();
        assertEquals(actualBinds.length, expectedVolumes.size());
        assertEquals(new HashSet<>(asList(actualBinds)), new HashSet<>(expectedVolumes));
    }

    @Test
    public void shouldGenerateValidNameForContainerFromPrefixWithValidCharacters() throws Exception {
        final String userName = "user";
        final String displayName = "displayName";
        final String expectedPrefix = String.format("%s_%s_%s_", userName, WORKSPACE_ID, displayName);

        final String containerName = dockerInstanceProvider.generateContainerName(WORKSPACE_ID, displayName);

        assertTrue(containerName.startsWith(expectedPrefix),
                   "Unexpected container name " + containerName + " while expected " + expectedPrefix + "*");
    }

    @Test
    public void shouldGenerateValidNameForContainerFromPrefixWithInvalidCharacters() throws Exception {
        final String userName = "{use}r+";
        final String displayName = "displ{[ayName@";
        EnvironmentContext.getCurrent().setUser(new UserImpl(userName));
        final String expectedPrefix = String.format("%s_%s_%s_", "user", "WORKSPACE_ID", "displayName");

        final String containerName = dockerInstanceProvider.generateContainerName("WORKSPACE_ID", displayName);

        assertTrue(containerName.startsWith(expectedPrefix),
                   "Unexpected container name " + containerName + " while expected " + expectedPrefix + "*");
    }

    @Test
    public void shouldAddWorkspaceIdEnvVariableOnDevInstanceCreationFromRecipe() throws Exception {
        String wsId = "myWs";
        createInstanceFromRecipe(true, wsId);
        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertTrue(Arrays.asList(argumentCaptor.getValue().getEnv())
                         .contains(DockerInstanceMetadata.CHE_WORKSPACE_ID + "=" + wsId),
                   "Workspace Id variable is missing. Required " +  DockerInstanceMetadata.CHE_WORKSPACE_ID + "=" + wsId +
                   ". Found " + Arrays.toString(argumentCaptor.getValue().getEnv()));
    }

    @Test
    public void shouldAddWorkspaceIdEnvVariableOnDevInstanceCreationFromSnapshot() throws Exception {
        String wsId = "myWs";
        createInstanceFromSnapshot(true, wsId);
        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertTrue(Arrays.asList(argumentCaptor.getValue().getEnv())
                         .contains(DockerInstanceMetadata.CHE_WORKSPACE_ID + "=" + wsId),
                   "Workspace Id variable is missing. Required " + DockerInstanceMetadata.CHE_WORKSPACE_ID + "=" + wsId +
                   ". Found " + Arrays.toString(argumentCaptor.getValue().getEnv()));
    }

    @Test
    public void shouldNotAddWorkspaceIdEnvVariableOnNonDevInstanceCreationFromRecipe() throws Exception {
        String wsId = "myWs";
        createInstanceFromRecipe(false, wsId);
        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertFalse(Arrays.asList(argumentCaptor.getValue().getEnv())
                          .contains(DockerInstanceMetadata.CHE_WORKSPACE_ID + "=" + wsId),
                    "Non dev machine should not contains " + DockerInstanceMetadata.CHE_WORKSPACE_ID);
    }

    @Test
    public void shouldNotAddWorkspaceIdEnvVariableOnNonDevInstanceCreationFromSnapshot() throws Exception {
        String wsId = "myWs";
        createInstanceFromSnapshot(false, wsId);
        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertFalse(Arrays.asList(argumentCaptor.getValue().getEnv())
                          .contains(DockerInstanceMetadata.CHE_WORKSPACE_ID + "=" + wsId),
                    "Non dev machine should not contains " + DockerInstanceMetadata.CHE_WORKSPACE_ID);
    }

    /**
     * E.g from https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
     *
     *   Users should be /Users
     *   /Users should be /Users
     *   c/Users should be /c/Users
     *   /c/Users should be /c/Users
     *   c:/Users should be /c/Users
     */
    @Test
    public void shouldEscapePathForWindowsHost() {
        assertEquals(dockerInstanceProvider.escapePath("Users"), "/Users");
        assertEquals(dockerInstanceProvider.escapePath("/Users"), "/Users");
        assertEquals(dockerInstanceProvider.escapePath("c/Users"), "/c/Users");
        assertEquals(dockerInstanceProvider.escapePath("/c/Users"), "/c/Users");
        assertEquals(dockerInstanceProvider.escapePath("c:/Users"), "/c/Users");
        assertEquals(dockerInstanceProvider.escapePath("C:/Users"), "/c/Users");

        assertEquals(dockerInstanceProvider.escapePath("C:/Users/path/dir/from/host:/name/of/dir/in/container"),
                     "/c/Users/path/dir/from/host:/name/of/dir/in/container");
    }

    @Test
    public void shouldAddCommonAndDevEnvVariablesToContainerOnDevInstanceCreationFromRecipe() throws Exception {
        Set<String> commonEnv = new HashSet<>(asList("ENV_VAR1=123", "ENV_VAR2=234"));
        Set<String> devEnv = new HashSet<>(asList("DEV_ENV_VAR1=345", "DEV_ENV_VAR2=456", "DEV_ENV_VAR3=567"));
        Set<String> expectedEnv = new HashSet<>();
        expectedEnv.addAll(commonEnv);
        expectedEnv.addAll(devEnv);
        expectedEnv.add(DockerInstanceMetadata.USER_TOKEN + "=" + USER_TOKEN);
        expectedEnv.add(DockerInstanceMetadata.CHE_WORKSPACE_ID + "=" + WORKSPACE_ID);

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.<ServerConf>emptySet(),
                                                            Collections.<ServerConf>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            devEnv,
                                                            commonEnv);

        final boolean isDev = true;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(new HashSet<>(Arrays.asList(argumentCaptor.getValue().getEnv())), expectedEnv);
    }

    @Test
    public void shouldNotAddDevEnvToCommonEnvVariablesToContainerOnNonDevInstanceCreationFromRecipe() throws Exception {
        Set<String> commonEnv = new HashSet<>(asList("ENV_VAR1=123", "ENV_VAR2=234"));
        Set<String> devEnv = new HashSet<>(asList("DEV_ENV_VAR1=345", "DEV_ENV_VAR2=456", "DEV_ENV_VAR3=567"));

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.<ServerConf>emptySet(),
                                                            Collections.<ServerConf>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            devEnv,
                                                            commonEnv);

        final boolean isDev = false;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(new HashSet<>(Arrays.asList(argumentCaptor.getValue().getEnv())), commonEnv);
    }

    @Test
    public void shouldAddCommonAndDevEnvVariablesToContainerOnDevInstanceCreationFromSnapshot() throws Exception {
        Set<String> commonEnv = new HashSet<>(asList("ENV_VAR1=123", "ENV_VAR2=234"));
        Set<String> devEnv = new HashSet<>(asList("DEV_ENV_VAR1=345", "DEV_ENV_VAR2=456", "DEV_ENV_VAR3=567"));
        Set<String> expectedEnv = new HashSet<>();
        expectedEnv.addAll(commonEnv);
        expectedEnv.addAll(devEnv);
        expectedEnv.add(DockerInstanceMetadata.USER_TOKEN + "=" + USER_TOKEN);
        expectedEnv.add(DockerInstanceMetadata.CHE_WORKSPACE_ID + "=" + WORKSPACE_ID);

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.<ServerConf>emptySet(),
                                                            Collections.<ServerConf>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            devEnv,
                                                            commonEnv);

        final boolean isDev = true;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(new HashSet<>(Arrays.asList(argumentCaptor.getValue().getEnv())), expectedEnv);
    }

    @Test
    public void shouldNotAddDevEnvToCommonEnvVariablesToContainerOnNonDevInstanceCreationFromSnapshot() throws Exception {
        Set<String> commonEnv = new HashSet<>(asList("ENV_VAR1=123", "ENV_VAR2=234"));
        Set<String> devEnv = new HashSet<>(asList("DEV_ENV_VAR1=345", "DEV_ENV_VAR2=456", "DEV_ENV_VAR3=567"));

        dockerInstanceProvider = new DockerInstanceProvider(dockerConnector,
                                                            dockerConnectorConfiguration,
                                                            dockerMachineFactory,
                                                            dockerInstanceStopDetector,
                                                            Collections.<ServerConf>emptySet(),
                                                            Collections.<ServerConf>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            Collections.<String>emptySet(),
                                                            null,
                                                            workspaceFolderPathProvider,
                                                            PROJECT_FOLDER_PATH,
                                                            false,
                                                            devEnv,
                                                            commonEnv);

        final boolean isDev = false;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<ContainerConfig> argumentCaptor = ArgumentCaptor.forClass(ContainerConfig.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture(), anyString());
        assertEquals(new HashSet<>(Arrays.asList(argumentCaptor.getValue().getEnv())), commonEnv);
    }

    private void createInstanceFromRecipe() throws Exception {
        createInstanceFromRecipe(false,
                                 64,
                                 "machineId",
                                 "userId",
                                 WORKSPACE_ID,
                                 DISPLAY_NAME,
                                 new RecipeImpl().withType("Dockerfile")
                                                 .withScript("FROM busybox"));
    }

    private void createInstanceFromRecipe(boolean isDev) throws Exception {
        createInstanceFromRecipe(isDev, null, null, null, null, null, null);
    }

    private void createInstanceFromRecipe(boolean isDev, String workspaceId) throws Exception {
        createInstanceFromRecipe(isDev, null, null, null, workspaceId, null, null);
    }

    private void createInstanceFromRecipe(int memorySizeInMB) throws Exception {
        createInstanceFromRecipe(null, memorySizeInMB, null, null, null, null, null);
    }

    private void createInstanceFromRecipe(Boolean isDev,
                                          Integer memorySizeInMB,
                                          String machineId,
                                          String userId,
                                          String workspaceId,
                                          String displayName,
                                          Recipe recipe) throws Exception {

        createInstanceFromRecipe(isDev == null ? false : isDev,
                                 memorySizeInMB == null ? 64 : memorySizeInMB,
                                 machineId == null ? "machineId" : machineId,
                                 userId == null ? "userId" : userId,
                                 workspaceId == null ? WORKSPACE_ID : workspaceId,
                                 displayName == null ? DISPLAY_NAME : displayName,
                                 recipe == null ? new RecipeImpl().withType("Dockerfile")
                                                                  .withScript("FROM busybox") : recipe,
                                 "machineType",
                                 new MachineSourceImpl("source type", "source location"),
                                 new ChannelsImpl("channel1", "channel2"),
                                 MachineStatus.CREATING);
    }

    private void createInstanceFromRecipe(boolean isDev,
                                          int memorySizeInMB,
                                          String machineId,
                                          String userId,
                                          String workspaceId,
                                          String displayName,
                                          Recipe recipe,
                                          String machineType,
                                          MachineSource machineSource,
                                          Channels channels,
                                          MachineStatus machineStatus)
            throws Exception {

        dockerInstanceProvider.createInstance(recipe,
                                              new MachineStateImpl(isDev,
                                                                   displayName,
                                                                   machineType,
                                                                   machineSource,
                                                                   new LimitsImpl(memorySizeInMB),
                                                                   machineId,
                                                                   channels,
                                                                   workspaceId,
                                                                   userId,
                                                                   "envName",
                                                                   machineStatus),
                                              LineConsumer.DEV_NULL);
    }

    private void createInstanceFromRecipe(Recipe recipe, MachineState machineState) throws Exception {

        dockerInstanceProvider.createInstance(recipe,
                                              machineState,
                                              LineConsumer.DEV_NULL);
    }

    private void createInstanceFromSnapshot() throws NotFoundException, MachineException {
        createInstanceFromSnapshot(null, null, null, null, null, null, null, null, null);
    }

    private void createInstanceFromSnapshot(int memorySizeInMB) throws NotFoundException, MachineException {
        createInstanceFromSnapshot(null, null, null, null, memorySizeInMB, null, null, null, null);
    }

    private void createInstanceFromSnapshot(boolean isDev) throws NotFoundException, MachineException {
        createInstanceFromSnapshot(null, null, null, isDev, null, null, null, null, null);
    }

    private void createInstanceFromSnapshot(boolean isDev, String workspaceId) throws NotFoundException, MachineException {
        createInstanceFromSnapshot(null, null, null, isDev, null, null, null, workspaceId, null);
    }

    private void createInstanceFromSnapshot(String repo, String tag, String registry) throws NotFoundException, MachineException {
        createInstanceFromSnapshot(repo, tag, registry, null, null, null, null, null, null);
    }

    private void createInstanceFromSnapshot(String repo,
                                            String tag,
                                            String registry,
                                            Boolean isDev,
                                            Integer memorySizeInMB,
                                            String machineId,
                                            String userId,
                                            String workspaceId,
                                            String displayName)
            throws NotFoundException, MachineException {

        createInstanceFromSnapshot(repo == null ? "repo" : repo,
                                   tag == null ? "tag" : tag,
                                   registry == null ? "localhost:1234" : registry,
                                   isDev == null ? false : isDev,
                                   memorySizeInMB == null ? 64 : memorySizeInMB,
                                   machineId == null ? "machineId" : machineId,
                                   userId == null ? "userId" : userId,
                                   workspaceId == null ? WORKSPACE_ID : workspaceId,
                                   displayName == null ? DISPLAY_NAME : displayName,
                                   "machineType",
                                   new MachineSourceImpl("source type", "source location"),
                                   new ChannelsImpl("channel1", "channel2"),
                                   MachineStatus.CREATING,
                                   "envName");
    }

    private void createInstanceFromSnapshot(String repo,
                                            String tag,
                                            String registry,
                                            boolean isDev,
                                            int memorySizeInMB,
                                            String machineId,
                                            String userId,
                                            String workspaceId,
                                            String displayName,
                                            String machineType,
                                            MachineSource machineSource,
                                            Channels channels,
                                            MachineStatus machineStatus,
                                            String envName)
            throws NotFoundException, MachineException {

        dockerInstanceProvider.createInstance(new DockerInstanceKey(repo, tag, "imageId", registry),
                                              new MachineStateImpl(isDev,
                                                                   displayName,
                                                                   machineType,
                                                                   machineSource,
                                                                   new LimitsImpl(memorySizeInMB),
                                                                   machineId,
                                                                   channels,
                                                                   workspaceId,
                                                                   userId,
                                                                   envName,
                                                                   machineStatus),
                                              LineConsumer.DEV_NULL);
    }

    private void createInstanceFromSnapshot(MachineState machineState)
            throws NotFoundException, MachineException {

        dockerInstanceProvider.createInstance(new DockerInstanceKey("repo" ,
                                                                    "tag",
                                                                    "imageId",
                                                                    "localhost:1234"),
                                              machineState,
                                              LineConsumer.DEV_NULL);
    }
}