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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.util.RecipeRetriever;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerState;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectContainerParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.DOCKER_FILE_TYPE;
import static org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.MACHINE_SNAPSHOT_PREFIX;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Listeners(MockitoTestNGListener.class)
public class MachineProviderImplTest {
    private static final String  CONTAINER_ID           = "containerId";
    private static final String  WORKSPACE_ID           = "wsId";
    private static final String  MACHINE_NAME           = "machineName";
    private static final String  USER_TOKEN             = "userToken";
    private static final String  USER_NAME              = "user";
    private static final boolean SNAPSHOT_USE_REGISTRY  = true;
    private static final int     MEMORY_SWAP_MULTIPLIER = 0;
    private static final String  ENV_NAME               = "env";
    private static final String  NETWORK_NAME           = "networkName";

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
    private UserSpecificDockerRegistryCredentialsProvider credentialsReader;

    @Mock
    private ContainerInfo containerInfo;

    @Mock
    private ContainerState containerState;

    @Mock
    private RecipeRetriever recipeRetriever;

    @Mock
    private WindowsPathEscaper pathEscaper;

    private MachineProviderImpl provider;

    @BeforeMethod
    public void setUp() throws Exception {
        when(dockerConnectorConfiguration.getDockerHostIp()).thenReturn("123.123.123.123");
        when(dockerNode.getProjectsFolder()).thenReturn("/tmp/projects");

        provider = spy(new MachineProviderBuilder().build());

        EnvironmentContext envCont = new EnvironmentContext();
        envCont.setSubject(new SubjectImpl(USER_NAME, "userId", USER_TOKEN, false));
        EnvironmentContext.setCurrent(envCont);


        when(recipeRetriever.getRecipe(any(MachineConfig.class)))
                .thenReturn(new RecipeImpl().withType(DOCKER_FILE_TYPE).withScript("FROM codenvy"));

        when(dockerMachineFactory.createNode(anyString(), anyString())).thenReturn(dockerNode);
        when(dockerConnector.createContainer(any(CreateContainerParams.class)))
                .thenReturn(new ContainerCreated(CONTAINER_ID, new String[0]));
        when(dockerConnector.inspectContainer(any(InspectContainerParams.class))).thenReturn(containerInfo);
        when(containerInfo.getState()).thenReturn(containerState);
        when(containerState.isRunning()).thenReturn(false);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test
    public void shouldPullDockerImageOnInstanceCreationFromSnapshotFromRegistry() throws Exception {
        String repo = MACHINE_SNAPSHOT_PREFIX + "repo";
        String tag = "latest";
        String registry = "localhost:1234";

        createInstanceFromSnapshot(repo, tag, registry);

        PullParams pullParams = PullParams.create(repo).withRegistry(registry).withTag(tag);

        verify(dockerConnector).pull(eq(pullParams), any(ProgressMonitor.class));
    }

    @Test
    public void shouldNotPullDockerImageOnInstanceCreationFromLocalSnapshot() throws Exception {
        String repo = MACHINE_SNAPSHOT_PREFIX + "repo";
        String tag = "latest";
        String registry = "localhost:1234";
        provider = spy(new MachineProviderBuilder().setSnapshotUseRegistry(false)
                                                   .build());

        createInstanceFromSnapshot(repo, tag, registry);

        verify(dockerConnector, never()).pull(eq(PullParams.create(repo).withTag(tag)), any(ProgressMonitor.class));
    }

    @Test
    public void shouldUseLocalImageOnInstanceCreationFromSnapshot() throws Exception {
        final String repo = MACHINE_SNAPSHOT_PREFIX + "repo";
        final String tag = "latest";
        provider = spy(new MachineProviderBuilder().setSnapshotUseRegistry(false)
                                                   .build());

        CheServiceImpl machine = createService();
        machine.setImage(repo + ":" + tag);
        machine.setBuild(null);

        provider.startService(USER_NAME,
                              WORKSPACE_ID,
                              ENV_NAME,
                              MACHINE_NAME,
                              false,
                              NETWORK_NAME,
                              machine,
                              LineConsumer.DEV_NULL);

        verify(dockerConnector, never()).pull(any(PullParams.class), any(ProgressMonitor.class));
    }

    @Test
    public void shouldNotRemoveImageAfterRestoreFromLocalSnapshot() throws Exception {
        String repo = MACHINE_SNAPSHOT_PREFIX + "repo";
        String tag = "latest";
        provider = spy(new MachineProviderBuilder().setSnapshotUseRegistry(false)
                                                   .build());

        createInstanceFromSnapshot(repo, tag, null);

        verify(dockerConnector, never()).removeImage(any(RemoveImageParams.class));
    }

    @Test
    public void shouldNotRemoveImageWhenCreatingInstanceFromLocalImage() throws Exception {
        String repo = "repo1";
        String tag = "latest";
        MachineProviderImpl provider = spy(new MachineProviderBuilder().setSnapshotUseRegistry(false)
                                                                       .build());

        CheServiceImpl machine = createService();
        machine.setBuild(null);
        machine.setImage(repo + ":" + tag + "@digest");

        provider.startService(USER_NAME,
                              WORKSPACE_ID,
                              ENV_NAME,
                              MACHINE_NAME,
                              false,
                              NETWORK_NAME,
                              machine,
                              LineConsumer.DEV_NULL);

        verify(dockerConnector, never()).removeImage(any(RemoveImageParams.class));
    }

    @Test
    public void shouldReTagBuiltImageWithPredictableOnInstanceCreationFromRecipe() throws Exception {
        // given
        String repo = MACHINE_SNAPSHOT_PREFIX + "repo1";
        String tag = "tag1";
        String registry = "registry1";

        // when
        CheServiceImpl machine = createInstanceFromSnapshot(repo, tag, registry);

        // then
        TagParams tagParams = TagParams.create(registry + "/" + repo + ":" + tag,
                                               "eclipse-che/" + machine.getContainerName());
        verify(dockerConnector).tag(eq(tagParams));
        ArgumentCaptor<RemoveImageParams> argumentCaptor = ArgumentCaptor.forClass(RemoveImageParams.class);
        verify(dockerConnector).removeImage(argumentCaptor.capture());
        RemoveImageParams imageParams = argumentCaptor.getValue();
        assertEquals(imageParams.getImage(), registry + "/" + repo + ":" + tag);
        assertFalse(imageParams.isForce());
    }

    @Test
    public void shouldCreateContainerOnInstanceCreationFromRecipe() throws Exception {
        // when
        CheServiceImpl machine = createInstanceFromRecipe();

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getContainerConfig().getImage(),
                     "eclipse-che/" + machine.getContainerName());
    }

    @Test
    public void shouldStartContainerOnCreateInstanceFromRecipe() throws Exception {
        createInstanceFromRecipe();

        ArgumentCaptor<StartContainerParams> argumentCaptor = ArgumentCaptor.forClass(StartContainerParams.class);
        verify(dockerConnector).startContainer(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getContainer(), CONTAINER_ID);
    }

    @Test
    public void shouldCreateContainerOnInstanceCreationFromSnapshot() throws Exception {
        // when
        CheServiceImpl machine = createInstanceFromSnapshot();

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getContainerConfig().getImage(),
                     "eclipse-che/" + machine.getContainerName());
    }

    @Test
    public void shouldCreateContainerWithPrivilegeMode() throws Exception {
        provider = spy(new MachineProviderBuilder().setPrivilegedMode(true)
                                                   .build());

        createInstanceFromRecipe();

        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue().getContainerConfig().getHostConfig().isPrivileged());
    }

    @Test
    public void shouldCreateContainerWithPidsLimit() throws Exception {
        provider = spy(new MachineProviderBuilder().setPidsLimit(512)
                                                   .build());

        createInstanceFromRecipe();

        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getContainerConfig().getHostConfig().getPidsLimit(), 512);
    }


    @Test(expectedExceptions = ServerException.class)
    public void shouldRemoveContainerInCaseFailedStartContainer() throws Exception {
        doThrow(IOException.class).when(dockerConnector).startContainer(StartContainerParams.create(CONTAINER_ID));

        createInstanceFromRecipe(false, WORKSPACE_ID);

        verify(dockerConnector)
                .removeContainer(RemoveContainerParams.create(CONTAINER_ID).withRemoveVolumes(true).withForce(true));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldRemoveContainerInCaseFailedGetCreateNode() throws Exception {
        doThrow(IOException.class).when(dockerMachineFactory).createNode(any(), any());

        createInstanceFromRecipe(false, WORKSPACE_ID);

        verify(dockerConnector)
                .removeContainer(RemoveContainerParams.create(CONTAINER_ID).withRemoveVolumes(true).withForce(true));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldRemoveContainerInCaseFailedCreateInstanceOnTheDockerMachineFactory() throws Exception {
        doThrow(IOException.class).when(dockerMachineFactory).createInstance(any(), any(), any(), any(), any());

        createInstanceFromRecipe(false, WORKSPACE_ID);

        verify(dockerConnector)
                .removeContainer(RemoveContainerParams.create(CONTAINER_ID).withRemoveVolumes(true).withForce(true));
    }

    @Test
    public void shouldStartContainerOnCreateInstanceFromSnapshot() throws Exception {
        createInstanceFromSnapshot();

        ArgumentCaptor<StartContainerParams> argumentCaptor = ArgumentCaptor.forClass(StartContainerParams.class);
        verify(dockerConnector).startContainer(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getContainer(), CONTAINER_ID);
    }

    @Test
    public void shouldCallCreationDockerInstanceWithFactoryOnCreateInstanceFromRecipe() throws Exception {
        CheServiceImpl service = createService();
        createInstanceFromRecipe(service);


        verify(dockerMachineFactory).createInstance(any(Machine.class),
                                                    eq(CONTAINER_ID),
                                                    eq("eclipse-che/" + service.getContainerName()),
                                                    eq(dockerNode),
                                                    any(LineConsumer.class));
    }

    @Test
    public void shouldSetMemorySizeInContainersOnInstanceCreationFromRecipe() throws Exception {
        int memorySizeMB = 234;


        createInstanceFromRecipe(memorySizeMB);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        verify(dockerConnector).startContainer(any(StartContainerParams.class));
        // docker accepts memory size in bytes
        assertEquals(argumentCaptor.getValue().getContainerConfig().getHostConfig().getMemory(),
                     memorySizeMB * 1024 * 1024);
    }

    @Test
    public void shouldSetMemorySizeInContainersOnInstanceCreationFromSnapshot() throws Exception {
        int memorySizeMB = 234;


        createInstanceFromSnapshot(memorySizeMB);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        verify(dockerConnector).startContainer(any(StartContainerParams.class));
        // docker accepts memory size in bytes
        assertEquals(argumentCaptor.getValue().getContainerConfig().getHostConfig().getMemory(),
                     memorySizeMB * 1024 * 1024);
    }

    @Test(dataProvider = "swapTestProvider")
    public void shouldBeAbleToSetCorrectSwapSize(double swapMultiplier, int memoryMB, long expectedSwapSize)
            throws Exception {
        // given
        provider = spy(new MachineProviderBuilder().setMemorySwapMultiplier(swapMultiplier)
                                                   .build());

        // when
        createInstanceFromRecipe(memoryMB);

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getContainerConfig().getHostConfig().getMemorySwap(), expectedSwapSize);
    }

    @DataProvider(name = "swapTestProvider")
    public static Object[][] swapTestProvider() {
        return new Object[][] {
                {-1, 1000, -1},
                {0, 1000, 1000L * 1024 * 1024},
                {0.7, 1000, (long)(1.7 * 1000 * 1024 * 1024)},
                {1, 1000, 2L * 1000 * 1024 * 1024},
                {2, 1000, 3L * 1000 * 1024 * 1024},
                {2.5, 1000, (long)(3.5 * 1000 * 1024 * 1024)}
        };
    }

    @Test
    public void shouldExposeCommonAndDevPortsToContainerOnDevInstanceCreationFromRecipe() throws Exception {
        List<String> expectedExposedPorts = new ArrayList<>();
        final Set<ServerConf> commonServers =
                new HashSet<>(asList(new ServerConfImpl("reference1", "8080", "http", null),
                                     new ServerConfImpl("reference2", "8081", "ftp", null)));
        expectedExposedPorts.addAll(commonServers.stream()
                                                 .map(ServerConf::getPort)
                                                 .collect(Collectors.toList()));

        final Set<ServerConf> devServers = new HashSet<>(asList(new ServerConfImpl("reference3", "8082", "https", null),
                                                                new ServerConfImpl("reference4", "8083", "sftp",
                                                                                   null)));
        expectedExposedPorts.addAll(devServers.stream()
                                              .map(ServerConf::getPort)
                                              .collect(Collectors.toList()));

        provider = new MachineProviderBuilder().setDevMachineServers(devServers)
                                               .setAllMachineServers(commonServers)
                                               .build();

        final boolean isDev = true;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());

        assertTrue(new ArrayList<>(argumentCaptor.getValue()
                                                 .getContainerConfig()
                                                 .getExposedPorts()
                                                 .keySet())
                           .containsAll(expectedExposedPorts));
    }

    @Test
    public void shouldExposeOnlyCommonPortsToContainerOnNonDevInstanceCreationFromRecipe() throws Exception {
        List<String> expectedExposedPorts = new ArrayList<>();
        final Set<ServerConf> commonServers =
                new HashSet<>(asList(new ServerConfImpl("reference1", "8080", "http", null),
                                     new ServerConfImpl("reference2", "8081", "ftp", null)));
        expectedExposedPorts.addAll(commonServers.stream()
                                                 .map(ServerConf::getPort)
                                                 .collect(Collectors.toList()));

        provider = new MachineProviderBuilder().setAllMachineServers(commonServers)
                                               .build();

        final boolean isDev = false;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());

        assertTrue(new ArrayList<>(argumentCaptor.getValue()
                                                 .getContainerConfig()
                                                 .getExposedPorts()
                                                 .keySet())
                           .containsAll(expectedExposedPorts));
    }

    @Test
    public void shouldExposeCommonAndDevPortsToContainerOnDevInstanceCreationFromSnapshot() throws Exception {
        List<String> expectedExposedPorts = new ArrayList<>();
        final Set<ServerConf> commonServers =
                new HashSet<>(asList(new ServerConfImpl("reference1", "8080", "http", null),
                                     new ServerConfImpl("reference2", "8081", "ftp", null)));
        expectedExposedPorts.addAll(commonServers.stream()
                                                 .map(ServerConf::getPort)
                                                 .collect(Collectors.toList()));

        final Set<ServerConf> devServers = new HashSet<>(asList(new ServerConfImpl("reference3", "8082", "https", null),
                                                                new ServerConfImpl("reference4", "8083", "sftp",
                                                                                   null)));
        expectedExposedPorts.addAll(devServers.stream()
                                              .map(ServerConf::getPort)
                                              .collect(Collectors.toList()));

        provider = new MachineProviderBuilder().setDevMachineServers(devServers)
                                               .setAllMachineServers(commonServers)
                                               .build();

        final boolean isDev = true;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());

        assertTrue(new ArrayList<>(argumentCaptor.getValue()
                                                 .getContainerConfig()
                                                 .getExposedPorts()
                                                 .keySet())
                           .containsAll(expectedExposedPorts));
    }

    @Test
    public void shouldExposeOnlyCommonPortsToContainerOnNonDevInstanceCreationFromSnapshot() throws Exception {
        List<String> expectedExposedPorts = new ArrayList<>();
        final Set<ServerConf> commonServers =
                new HashSet<>(asList(new ServerConfImpl("reference1", "8080", "http", null),
                                     new ServerConfImpl("reference2", "8081", "ftp", null)));
        expectedExposedPorts.addAll(commonServers.stream()
                                                 .map(ServerConf::getPort)
                                                 .collect(Collectors.toList()));

        provider = new MachineProviderBuilder().setAllMachineServers(commonServers)
                                               .build();

        final boolean isDev = false;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());

        assertTrue(new ArrayList<>(argumentCaptor.getValue()
                                                 .getContainerConfig()
                                                 .getExposedPorts()
                                                 .keySet())
                           .containsAll(expectedExposedPorts));
    }

    @Test
    public void shouldAddServersConfsPortsFromMachineConfigToExposedPortsOnNonDevInstanceCreationFromRecipe()
            throws Exception {
        // given
        final boolean isDev = false;
        CheServiceImpl machine = createService();
        machine.setExpose(asList("9090", "8080"));
        List<String> expectedExposedPorts = asList("9090", "8080");

        // when
        createInstanceFromRecipe(machine, isDev);

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());

        assertTrue(new ArrayList<>(argumentCaptor.getValue()
                                                 .getContainerConfig()
                                                 .getExposedPorts()
                                                 .keySet())
                           .containsAll(expectedExposedPorts));
    }

    @Test
    public void shouldAddServersConfigsPortsFromMachineConfigToExposedPortsOnDevInstanceCreationFromRecipe()
            throws Exception {
        // given
        final boolean isDev = true;
        CheServiceImpl machine = createService();
        machine.setExpose(asList("9090", "8080"));

        // when
        createInstanceFromRecipe(machine, isDev);

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());

        assertTrue(new ArrayList<>(argumentCaptor.getValue()
                                                 .getContainerConfig()
                                                 .getExposedPorts()
                                                 .keySet())
                           .containsAll(asList("9090", "8080")));
    }

    @Test
    public void shouldBindCommonAndDevVolumesToContainerOnDevInstanceCreationFromRecipe() throws Exception {
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes =
                new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        final ArrayList<String> expectedVolumes = new ArrayList<>();
        expectedVolumes.addAll(devVolumes);
        expectedVolumes.addAll(commonVolumes);

        provider = new MachineProviderBuilder().setDevMachineVolumes(devVolumes)
                                               .setAllMachineVolumes(commonVolumes)
                                               .build();

        final boolean isDev = true;

        CheServiceImpl service = createService();
        service.setVolumes(null);
        createInstanceFromRecipe(isDev, service);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        verify(dockerConnector).startContainer(any(StartContainerParams.class));

        final String[] actualBinds = argumentCaptor.getValue().getContainerConfig().getHostConfig().getBinds();
        assertEquals(actualBinds.length, expectedVolumes.size());
        assertEquals(new HashSet<>(asList(actualBinds)), new HashSet<>(expectedVolumes));
    }

    @Test
    public void shouldBindCommonVolumesOnlyToContainerOnNonDevInstanceCreationFromRecipe() throws Exception {
        Set<String> devVolumes = new HashSet<>(asList("/etc:/tmp/etc:ro", "/some/thing:/home/some/thing"));
        Set<String> commonVolumes =
                new HashSet<>(asList("/some/thing/else:/home/some/thing/else", "/other/path:/home/other/path"));

        final ArrayList<String> expectedVolumes = new ArrayList<>();
        expectedVolumes.addAll(commonVolumes);

        provider = new MachineProviderBuilder().setDevMachineVolumes(devVolumes)
                                               .setAllMachineVolumes(commonVolumes)
                                               .build();

        final boolean isDev = false;


        CheServiceImpl service = createService();
        service.setVolumes(null);
        createInstanceFromRecipe(isDev, service);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        verify(dockerConnector).startContainer(any(StartContainerParams.class));

        final String[] actualBinds = argumentCaptor.getValue().getContainerConfig().getHostConfig().getBinds();
        assertEquals(actualBinds.length, expectedVolumes.size());
        assertEquals(new HashSet<>(asList(actualBinds)), new HashSet<>(expectedVolumes));
    }

    @Test
    public void shouldAddExtraHostOnDevInstanceCreationFromRecipe() throws Exception {
        //given
        provider = new MachineProviderBuilder().setExtraHosts("dev.box.com:192.168.0.1")
                                               .build();

        final boolean isDev = true;

        //when
        createInstanceFromRecipe(isDev);

        //then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        verify(dockerConnector).startContainer(any(StartContainerParams.class));

        final String[] extraHosts = argumentCaptor.getValue().getContainerConfig().getHostConfig().getExtraHosts();
        assertEquals(extraHosts.length, 2);
        assertEquals(extraHosts[0], "dev.box.com:192.168.0.1");
    }

    @Test
    public void shouldAddExtraHostOnDevInstanceCreationFromSnapshot() throws Exception {
        //given
        provider = new MachineProviderBuilder().setExtraHosts("dev.box.com:192.168.0.1,codenvy.com.com:185")
                                               .build();

        final boolean isDev = true;

        //when
        createInstanceFromSnapshot(isDev);
        //then

        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        verify(dockerConnector).startContainer(any(StartContainerParams.class));

        final String[] extraHosts = argumentCaptor.getValue().getContainerConfig().getHostConfig().getExtraHosts();
        assertEquals(extraHosts.length, 3);
        assertEquals(extraHosts[0], "dev.box.com:192.168.0.1");
        assertEquals(extraHosts[1], "codenvy.com.com:185");
    }

    @Test
    public void shouldAddExtraHostOnNonDevInstanceCreationFromRecipe() throws Exception {
        //given
        provider = new MachineProviderBuilder().setExtraHosts("dev.box.com:192.168.0.1")
                                               .build();

        final boolean isDev = false;

        //when
        createInstanceFromRecipe(isDev);

        //then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        verify(dockerConnector).startContainer(any(StartContainerParams.class));

        final String[] extraHosts = argumentCaptor.getValue().getContainerConfig().getHostConfig().getExtraHosts();
        assertEquals(extraHosts.length, 2);
        assertEquals(extraHosts[0], "dev.box.com:192.168.0.1");
    }

    @Test
    public void shouldAddExtraHostOnNonDevInstanceCreationFromSnapshot() throws Exception {
        //given
        provider = new MachineProviderBuilder().setExtraHosts("dev.box.com:192.168.0.1,codenvy.com.com:185")
                                               .build();

        final boolean isDev = false;

        //when
        createInstanceFromSnapshot(isDev);
        //then

        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        verify(dockerConnector).startContainer(any(StartContainerParams.class));

        final String[] extraHosts = argumentCaptor.getValue().getContainerConfig().getHostConfig().getExtraHosts();
        assertEquals(extraHosts.length, 3);
        assertEquals(extraHosts[0], "dev.box.com:192.168.0.1");
        assertEquals(extraHosts[1], "codenvy.com.com:185");
    }

    @Test
    public void shouldAddWorkspaceIdEnvVariableOnDevInstanceCreationFromRecipe() throws Exception {
        String wsId = "myWs";
        createInstanceFromRecipe(true, wsId);
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(asList(argumentCaptor.getValue().getContainerConfig().getEnv())
                           .contains(DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID + "=" + wsId),
                   "Workspace Id variable is missing. Required " + DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID + "=" +
                   wsId +
                   ". Found " + Arrays.toString(argumentCaptor.getValue().getContainerConfig().getEnv()));
    }

    @Test
    public void shouldAddWorkspaceIdEnvVariableOnDevInstanceCreationFromSnapshot() throws Exception {
        String wsId = "myWs";
        createInstanceFromSnapshot(true, wsId);
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(asList(argumentCaptor.getValue().getContainerConfig().getEnv())
                           .contains(DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID + "=" + wsId),
                   "Workspace Id variable is missing. Required " + DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID + "=" +
                   wsId +
                   ". Found " + Arrays.toString(argumentCaptor.getValue().getContainerConfig().getEnv()));
    }

    @Test
    public void shouldNotAddWorkspaceIdEnvVariableOnNonDevInstanceCreationFromRecipe() throws Exception {
        String wsId = "myWs";
        createInstanceFromRecipe(false, wsId);
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertFalse(asList(argumentCaptor.getValue().getContainerConfig().getEnv())
                            .contains(DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID + "=" + wsId),
                    "Non dev machine should not contains " + DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID);
    }

    @Test
    public void shouldAddCommonAndDevEnvVariablesToContainerOnDevInstanceCreationFromRecipe() throws Exception {
        Set<String> commonEnv = new HashSet<>(asList("ENV_VAR1=123", "ENV_VAR2=234"));
        Set<String> devEnv = new HashSet<>(asList("DEV_ENV_VAR1=345", "DEV_ENV_VAR2=456", "DEV_ENV_VAR3=567"));
        Set<String> expectedEnv = new HashSet<>();
        expectedEnv.addAll(commonEnv);
        expectedEnv.addAll(devEnv);
        expectedEnv.add(DockerInstanceRuntimeInfo.USER_TOKEN + "=" + USER_TOKEN);
        expectedEnv.add(DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID + "=" + WORKSPACE_ID);

        provider = new MachineProviderBuilder().setDevMachineEnvVars(devEnv)
                                               .setAllMachineEnvVars(commonEnv)
                                               .build();

        final boolean isDev = true;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(new HashSet<>(asList(argumentCaptor.getValue().getContainerConfig().getEnv()))
                           .containsAll(expectedEnv));
    }

    @Test
    public void shouldNotAddDevEnvToCommonEnvVariablesToContainerOnNonDevInstanceCreationFromRecipe() throws Exception {
        Set<String> commonEnv = new HashSet<>(asList("ENV_VAR1=123", "ENV_VAR2=234"));
        Set<String> devEnv = new HashSet<>(asList("DEV_ENV_VAR1=345", "DEV_ENV_VAR2=456", "DEV_ENV_VAR3=567"));

        provider = new MachineProviderBuilder().setDevMachineEnvVars(devEnv)
                                               .setAllMachineEnvVars(commonEnv)
                                               .build();

        final boolean isDev = false;


        createInstanceFromRecipe(isDev);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(
                new HashSet<>(asList(argumentCaptor.getValue().getContainerConfig().getEnv())).containsAll(commonEnv));
    }

    @Test
    public void shouldAddCommonAndDevEnvVariablesToContainerOnDevInstanceCreationFromSnapshot() throws Exception {
        Set<String> commonEnv = new HashSet<>(asList("ENV_VAR1=123", "ENV_VAR2=234"));
        Set<String> devEnv = new HashSet<>(asList("DEV_ENV_VAR1=345", "DEV_ENV_VAR2=456", "DEV_ENV_VAR3=567"));
        Set<String> expectedEnv = new HashSet<>();
        expectedEnv.addAll(commonEnv);
        expectedEnv.addAll(devEnv);
        expectedEnv.add(DockerInstanceRuntimeInfo.USER_TOKEN + "=" + USER_TOKEN);
        expectedEnv.add(DockerInstanceRuntimeInfo.CHE_WORKSPACE_ID + "=" + WORKSPACE_ID);

        provider = new MachineProviderBuilder().setDevMachineEnvVars(devEnv)
                                               .setAllMachineEnvVars(commonEnv)
                                               .build();

        final boolean isDev = true;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(new HashSet<>(asList(argumentCaptor.getValue().getContainerConfig().getEnv()))
                           .containsAll(expectedEnv));
    }

    @Test
    public void shouldNotAddDevEnvToCommonEnvVariablesToContainerOnNonDevInstanceCreationFromSnapshot()
            throws Exception {
        Set<String> commonEnv = new HashSet<>(asList("ENV_VAR1=123", "ENV_VAR2=234"));
        Set<String> devEnv = new HashSet<>(asList("DEV_ENV_VAR1=345", "DEV_ENV_VAR2=456", "DEV_ENV_VAR3=567"));

        provider = new MachineProviderBuilder().setDevMachineEnvVars(devEnv)
                                               .setAllMachineEnvVars(commonEnv)
                                               .build();

        final boolean isDev = false;


        createInstanceFromSnapshot(isDev);


        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(
                new HashSet<>(asList(argumentCaptor.getValue().getContainerConfig().getEnv())).containsAll(commonEnv));
    }

    @Test
    public void shouldAddEnvVarsFromMachineConfigToContainerOnNonDevInstanceCreationFromSnapshot() throws Exception {
        // given
        Map<String, String> envVarsFromConfig = new HashMap<>();
        envVarsFromConfig.put("ENV_VAR1", "123");
        envVarsFromConfig.put("ENV_VAR2", "234");

        final boolean isDev = false;
        CheServiceImpl machine = createService();
        machine.setEnvironment(envVarsFromConfig);

        // when
        createInstanceFromSnapshot(machine, isDev);

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(asList(argumentCaptor.getValue()
                                        .getContainerConfig()
                                        .getEnv())
                           .containsAll(envVarsFromConfig.entrySet()
                                                         .stream()
                                                         .map(entry -> entry.getKey() +
                                                                       "=" +
                                                                       entry.getValue())
                                                         .collect(Collectors.toList())));
    }

    @Test
    public void shouldAddEnvVarsFromMachineConfigToContainerOnDevInstanceCreationFromSnapshot() throws Exception {
        // given
        Map<String, String> envVarsFromConfig = new HashMap<>();
        envVarsFromConfig.put("ENV_VAR1", "123");
        envVarsFromConfig.put("ENV_VAR2", "234");

        final boolean isDev = true;
        CheServiceImpl machine = createService();
        machine.setEnvironment(envVarsFromConfig);

        // when
        createInstanceFromSnapshot(machine, isDev);

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(asList(argumentCaptor.getValue()
                                        .getContainerConfig()
                                        .getEnv())
                           .containsAll(envVarsFromConfig.entrySet()
                                                         .stream()
                                                         .map(entry -> entry.getKey() +
                                                                       "=" +
                                                                       entry.getValue())
                                                         .collect(Collectors.toList())));
    }

    @Test
    public void shouldAddEnvVarsFromMachineConfigToContainerOnNonDevInstanceCreationFromRecipe() throws Exception {
        // given
        Map<String, String> envVarsFromConfig = new HashMap<>();
        envVarsFromConfig.put("ENV_VAR1", "123");
        envVarsFromConfig.put("ENV_VAR2", "234");

        final boolean isDev = false;
        CheServiceImpl service = createService();
        service.setEnvironment(envVarsFromConfig);

        // when
        createInstanceFromRecipe(service, isDev);

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(asList(argumentCaptor.getValue()
                                        .getContainerConfig()
                                        .getEnv())
                           .containsAll(envVarsFromConfig.entrySet()
                                                         .stream()
                                                         .map(entry -> entry.getKey() +
                                                                       "=" +
                                                                       entry.getValue())
                                                         .collect(Collectors.toList())));
    }

    @Test
    public void shouldAddEnvVarsFromMachineConfigToContainerOnDevInstanceCreationFromRecipe() throws Exception {
        // given
        Map<String, String> envVarsFromConfig = new HashMap<>();
        envVarsFromConfig.put("ENV_VAR1", "123");
        envVarsFromConfig.put("ENV_VAR2", "234");

        final boolean isDev = true;
        CheServiceImpl machine = createService();
        machine.setEnvironment(envVarsFromConfig);

        // when
        createInstanceFromRecipe(machine, isDev);

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        assertTrue(asList(argumentCaptor.getValue()
                                        .getContainerConfig()
                                        .getEnv())
                           .containsAll(envVarsFromConfig.entrySet()
                                                         .stream()
                                                         .map(entry -> entry.getKey() +
                                                                       "=" +
                                                                       entry.getValue())
                                                         .collect(Collectors.toList())));
    }

    @Test
    public void shouldAddLinksToContainerOnCreation() throws Exception {
        // given
        String links[] = new String[] {"container1", "container2:alias"};
        String networkName = "network";

        CheServiceImpl service = createService();
        service.setLinks(asList(links));

        // when
        createInstanceFromRecipe(service, true);

        // then
        ArgumentCaptor<CreateContainerParams> argumentCaptor = ArgumentCaptor.forClass(CreateContainerParams.class);
        verify(dockerConnector).createContainer(argumentCaptor.capture());
        ContainerConfig containerConfig = argumentCaptor.getValue().getContainerConfig();
        assertEquals(containerConfig.getHostConfig().getLinks(), links);
        assertEquals(containerConfig.getNetworkingConfig().getEndpointsConfig().get(NETWORK_NAME).getLinks(), links);
    }

    private CheServiceImpl createInstanceFromRecipe() throws Exception {
        CheServiceImpl service = createService();
        createInstanceFromRecipe(service);

        return service;
    }

    private void createInstanceFromRecipe(boolean isDev, CheServiceImpl service) throws Exception {
        createInstanceFromRecipe(service, isDev, WORKSPACE_ID);
    }

    private void createInstanceFromRecipe(boolean isDev) throws Exception {
        createInstanceFromRecipe(createService(), isDev, WORKSPACE_ID);
    }

    private void createInstanceFromRecipe(boolean isDev, String workspaceId) throws Exception {
        createInstanceFromRecipe(createService(), isDev, workspaceId);
    }

    private void createInstanceFromRecipe(int memorySizeInMB) throws Exception {
        CheServiceImpl machine = createService();
        machine.setMemLimit(memorySizeInMB * 1024L * 1024L);
        createInstanceFromRecipe(machine);
    }

    private CheServiceImpl createInstanceFromSnapshot(String repo, String tag, String registry) throws ServerException {
        CheServiceImpl machine = createService();
        machine.setImage(registry + "/" + repo + ":" + tag);
        machine.setBuild(null);
        createInstanceFromSnapshot(machine);

        return machine;
    }

    private void createInstanceFromRecipe(CheServiceImpl service, boolean isDev) throws Exception {
        createInstanceFromRecipe(service, isDev, WORKSPACE_ID);
    }

    private void createInstanceFromRecipe(CheServiceImpl service) throws Exception {
        createInstanceFromRecipe(service, false, WORKSPACE_ID);
    }

    private void createInstanceFromRecipe(CheServiceImpl service,
                                          boolean isDev,
                                          String workspaceId) throws Exception {
        provider.startService(USER_NAME,
                              workspaceId,
                              ENV_NAME,
                              MACHINE_NAME,
                              isDev,
                              NETWORK_NAME,
                              service,
                              LineConsumer.DEV_NULL);
    }

    private CheServiceImpl createInstanceFromSnapshot() throws ServerException {
        CheServiceImpl service = createService();
        createInstanceFromSnapshot(service, false, WORKSPACE_ID);

        return service;
    }

    private void createInstanceFromSnapshot(CheServiceImpl service) throws ServerException {
        createInstanceFromSnapshot(service, false, WORKSPACE_ID);
    }

    private void createInstanceFromSnapshot(int memorySizeInMB) throws ServerException {
        CheServiceImpl machine = createService();
        machine.setMemLimit(memorySizeInMB * 1024L * 1024L);
        createInstanceFromSnapshot(machine, false, WORKSPACE_ID);
    }

    private void createInstanceFromSnapshot(boolean isDev) throws ServerException {
        createInstanceFromSnapshot(createService(), isDev, WORKSPACE_ID);
    }

    private void createInstanceFromSnapshot(boolean isDev, String workspaceId) throws ServerException {
        createInstanceFromSnapshot(createService(), isDev, workspaceId);
    }

    private void createInstanceFromSnapshot(CheServiceImpl service, boolean isDev) throws ServerException {
        createInstanceFromSnapshot(service, isDev, WORKSPACE_ID);
    }

    private void createInstanceFromSnapshot(CheServiceImpl service, boolean isDev, String workspaceId)
            throws ServerException {
        provider.startService(USER_NAME,
                              workspaceId,
                              ENV_NAME,
                              MACHINE_NAME,
                              isDev,
                              NETWORK_NAME,
                              service,
                              LineConsumer.DEV_NULL);
    }

    public CheServiceImpl createService() {
        CheServiceImpl service = new CheServiceImpl();
        service.setId("testId");
        service.setImage("image");
        service.setCommand(asList("some", "command"));
        service.setContainerName("cont_name");
        service.setDependsOn(asList("dep1", "dep2"));
        service.setEntrypoint(asList("entry", "point"));
        service.setExpose(asList("1010", "1111"));
        service.setEnvironment(singletonMap("some", "var"));
        service.setLabels(singletonMap("some", "label"));
        service.setLinks(asList("link1", "link2:alias"));
        service.setMemLimit(1000000000L);
        service.setPorts(asList("port1", "port2"));
        service.setVolumes(asList("vol1", "vol2"));
        service.setVolumesFrom(asList("from1", "from2"));
        return service;
    }

    private class MachineProviderBuilder {
        private Set<ServerConf>  devMachineServers;
        private Set<ServerConf>  allMachineServers;
        private Set<String>      devMachineVolumes;
        private Set<String>      allMachineVolumes;
        private String           extraHosts;
        private boolean          doForcePullOnBuild;
        private boolean          privilegedMode;
        private int              pidsLimit;
        private Set<String>      devMachineEnvVars;
        private Set<String>      allMachineEnvVars;
        private boolean          snapshotUseRegistry;
        private Set<Set<String>> additionalNetworks;
        private double           memorySwapMultiplier;

        public MachineProviderBuilder() {
            devMachineEnvVars = emptySet();
            allMachineEnvVars = emptySet();
            snapshotUseRegistry = SNAPSHOT_USE_REGISTRY;
            privilegedMode = false;
            doForcePullOnBuild = false;
            additionalNetworks = emptySet();
            devMachineServers = emptySet();
            allMachineServers = emptySet();
            devMachineVolumes = emptySet();
            allMachineVolumes = emptySet();
            extraHosts = null;
            memorySwapMultiplier = MEMORY_SWAP_MULTIPLIER;
            pidsLimit = -1;
        }

        public MachineProviderBuilder setDevMachineEnvVars(Set<String> devMachineEnvVars) {
            this.devMachineEnvVars = devMachineEnvVars;
            return this;
        }

        public MachineProviderBuilder setAllMachineEnvVars(Set<String> allMachineEnvVars) {
            this.allMachineEnvVars = allMachineEnvVars;
            return this;
        }

        public MachineProviderBuilder setSnapshotUseRegistry(boolean snapshotUseRegistry) {
            this.snapshotUseRegistry = snapshotUseRegistry;
            return this;
        }

        public MachineProviderBuilder setPrivilegedMode(boolean privilegedMode) {
            this.privilegedMode = privilegedMode;
            return this;
        }

        public MachineProviderBuilder setPidsLimit(int pidsLimit) {
            this.pidsLimit = pidsLimit;
            return this;
        }

        public MachineProviderBuilder setMemorySwapMultiplier(double memorySwapMultiplier) {
            this.memorySwapMultiplier = memorySwapMultiplier;
            return this;
        }

        public MachineProviderBuilder setDevMachineServers(Set<ServerConf> devMachineServers) {
            this.devMachineServers = devMachineServers;
            return this;
        }

        public MachineProviderBuilder setAllMachineServers(Set<ServerConf> allMachineServers) {
            this.allMachineServers = allMachineServers;
            return this;
        }

        public MachineProviderBuilder setAllMachineVolumes(Set<String> allMachineVolumes) {
            this.allMachineVolumes = allMachineVolumes;
            return this;
        }

        public MachineProviderBuilder setDevMachineVolumes(Set<String> devMachineVolumes) {
            this.devMachineVolumes = devMachineVolumes;
            return this;
        }

        public MachineProviderBuilder setExtraHosts(String extraHosts) {
            this.extraHosts = extraHosts;
            return this;
        }

        MachineProviderImpl build() throws IOException {
            return new MachineProviderImpl(dockerConnector,
                                           dockerConnectorConfiguration,
                                           credentialsReader,
                                           dockerMachineFactory,
                                           dockerInstanceStopDetector,
                                           devMachineServers,
                                           allMachineServers,
                                           devMachineVolumes,
                                           allMachineVolumes,
                                           extraHosts,
                                           doForcePullOnBuild,
                                           privilegedMode,
                                           pidsLimit,
                                           devMachineEnvVars,
                                           allMachineEnvVars,
                                           snapshotUseRegistry,
                                           memorySwapMultiplier,
                                           additionalNetworks,
                                           null,
                                           pathEscaper);
        }
    }
}
