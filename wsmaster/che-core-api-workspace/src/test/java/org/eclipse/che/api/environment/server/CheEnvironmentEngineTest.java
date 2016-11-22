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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineLogMessage;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.ServerConf2;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.MessageConsumer;
import org.eclipse.che.api.environment.server.exception.EnvironmentNotRunningException;
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class CheEnvironmentEngineTest {
    private static final int    DEFAULT_MACHINE_MEM_LIMIT_MB = 256;
    private static final String API_ENDPOINT                 = "http://eclipse.che:8080/api";

    @Mock
    private MessageConsumer<MachineLogMessage> messageConsumer;
    @Mock
    private InstanceProvider                   instanceProvider;
    @Mock
    private MachineInstanceProvider            machineProvider;
    @Mock
    private MachineInstanceProviders machineInstanceProviders;
    @Mock
    private EventService             eventService;
    @Mock
    private SnapshotDao              snapshotDao;
    @Mock
    private RecipeDownloader         recipeDownloader;
    @Mock
    InfrastructureProvisioner          infrastructureProvisioner;
    @Mock
    private ContainerNameGenerator   containerNameGenerator;
    @Mock
    private AgentRegistry            agentRegistry;
    @Mock
    private Agent                    agent;
    @Mock
    private EnvironmentParser        environmentParser;

    private CheEnvironmentEngine engine;

    @BeforeMethod
    public void setUp() throws Exception {
        engine = spy(new CheEnvironmentEngine(snapshotDao,
                                              machineInstanceProviders,
                                              "/tmp",
                                              DEFAULT_MACHINE_MEM_LIMIT_MB,
                                              eventService,
                                              environmentParser,
                                              new DefaultServicesStartStrategy(),
                                              machineProvider,
                                              infrastructureProvisioner,
                                              API_ENDPOINT,
                                              recipeDownloader,
                                              containerNameGenerator,
                                              agentRegistry));

        when(machineInstanceProviders.getProvider("docker")).thenReturn(instanceProvider);
        when(instanceProvider.getRecipeTypes()).thenReturn(Collections.singleton("dockerfile"));
        when(agentRegistry.getAgent(any(AgentKey.class))).thenReturn(agent);

        EnvironmentContext.getCurrent().setSubject(new SubjectImpl("name", "id", "token", false));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test
    public void shouldBeAbleToGetMachinesOfEnv() throws Exception {
        // given
        List<Instance> instances = startEnv();
        String workspaceId = instances.get(0).getWorkspaceId();

        // when
        List<Instance> actualMachines = engine.getMachines(workspaceId);

        // then
        assertEquals(actualMachines, instances);
    }

    @Test(expectedExceptions = EnvironmentNotRunningException.class,
          expectedExceptionsMessageRegExp = "Environment with ID '.*' is not found")
    public void shouldThrowExceptionOnGetMachinesIfEnvironmentIsNotFound() throws Exception {
        engine.getMachines("wsIdOfNotRunningEnv");
    }

    @Test
    public void shouldBeAbleToGetMachineOfEnv() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);
        String workspaceId = instance.getWorkspaceId();

        // when
        Instance actualInstance = engine.getMachine(workspaceId, instance.getId());

        // then
        assertEquals(actualInstance, instance);
    }

    @Test(expectedExceptions = EnvironmentNotRunningException.class,
          expectedExceptionsMessageRegExp = "Environment with ID '.*' is not found")
    public void shouldThrowExceptionOnGetMachineIfEnvironmentIsNotFound() throws Exception {
        // when
        engine.getMachine("wsIdOfNotRunningEnv", "nonExistingInstanceId");
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Machine with ID .* is not found in the environment of workspace .*")
    public void shouldThrowExceptionOnGetMachineIfMachineIsNotFound() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);
        String workspaceId = instance.getWorkspaceId();

        // when
        engine.getMachine(workspaceId, "nonExistingInstanceId");
    }

    @Test
    public void shouldBeAbleToStartEnvironment() throws Exception {
        // given
        EnvironmentImpl env = createEnv();
        String envName = "env-1";
        String workspaceId = "wsId";
        List<Instance> expectedMachines = new ArrayList<>();
        when(machineProvider.startService(anyString(),
                                          eq(workspaceId),
                                          eq(envName),
                                          anyString(),
                                          anyBoolean(),
                                          anyString(),
                                          any(CheServiceImpl.class),
                                          any(LineConsumer.class)))
                .thenAnswer(invocationOnMock -> {
                    Object[] arguments = invocationOnMock.getArguments();
                    String machineName = (String)arguments[3];
                    boolean isDev = (boolean)arguments[4];
                    CheServiceImpl service = (CheServiceImpl)arguments[6];
                    Machine machine = createMachine(workspaceId,
                                                    envName,
                                                    service,
                                                    machineName,
                                                    isDev);
                    NoOpMachineInstance instance = spy(new NoOpMachineInstance(machine));
                    expectedMachines.add(instance);
                    return instance;
                });
        when(environmentParser.parse(env)).thenReturn(createCheServicesEnv());

        // when
        List<Instance> machines = engine.start(workspaceId,
                                               envName,
                                               env,
                                               false,
                                               messageConsumer);

        // then
        assertEquals(machines, expectedMachines);
    }

    @Test
    public void shouldSetDefaultRamToMachinesWithoutRamOnEnvironmentStart() throws Exception {
        // given
        EnvironmentImpl env = createEnv();
        String machineName = "machineWithoutRam";
        //prepare CheServicesEnvironmentImpl which should return compose parser
        CheServicesEnvironmentImpl cheServicesEnvironment = createCheServicesEnvByName(machineName);

        // when
        startEnv(env, cheServicesEnvironment);

        // then
        ArgumentCaptor<CheServiceImpl> captor = ArgumentCaptor.forClass(CheServiceImpl.class);
        verify(machineProvider).startService(anyString(),
                                             anyString(),
                                             anyString(),
                                             eq(machineName),
                                             eq(false),
                                             anyString(),
                                             captor.capture(),
                                             any(LineConsumer.class));
        CheServiceImpl actualService = captor.getValue();
        assertEquals((long)actualService.getMemLimit(), DEFAULT_MACHINE_MEM_LIMIT_MB * 1024L * 1024L);
    }

    @Test
    public void shouldUseConfiguredInServiceRamInsteadOfSetDefaultOnEnvironmentStart() throws Exception {
        // given
        EnvironmentImpl env = createEnv();
        String machineName = "machineWithoutRam";
        //prepare CheServicesEnvironmentImpl which should return compose parser
        CheServicesEnvironmentImpl cheServicesEnvironment = createCheServicesEnvByName(machineName);
        cheServicesEnvironment.getServices().get(machineName).withMemLimit(42943433L);

        // when
        startEnv(env, cheServicesEnvironment);

        // then
        ArgumentCaptor<CheServiceImpl> captor = ArgumentCaptor.forClass(CheServiceImpl.class);
        verify(machineProvider).startService(anyString(),
                                             anyString(),
                                             anyString(),
                                             eq(machineName),
                                             eq(false),
                                             anyString(),
                                             captor.capture(),
                                             any(LineConsumer.class));
        CheServiceImpl actualService = captor.getValue();
        assertEquals((long)actualService.getMemLimit(), 42943433L);
    }

    @Test
    public void shouldSetDockerfileContentInsteadOfUrlIfUrlPointsToCheApiOnEnvironmentStart() throws Exception {
        // given
        EnvironmentImpl env = createEnv();
        String machineName = "machineWithDockerfileFromApi";
        String dockerfileContent = "this is dockerfile content";
        when(recipeDownloader.getRecipe(anyString())).thenReturn(dockerfileContent);

        //prepare CheServicesEnvironmentImpl which should return compose parser
        CheServicesEnvironmentImpl cheServicesEnvironment = createCheServicesEnvByName(machineName);
        cheServicesEnvironment.getServices()
                              .get(machineName)
                              .withBuild(new CheServiceBuildContextImpl().withContext(API_ENDPOINT + "/recipe/12345"));

        // when
        startEnv(env, cheServicesEnvironment);

        // then
        ArgumentCaptor<CheServiceImpl> captor = ArgumentCaptor.forClass(CheServiceImpl.class);
        verify(machineProvider).startService(anyString(),
                                             anyString(),
                                             anyString(),
                                             eq(machineName),
                                             eq(false),
                                             anyString(),
                                             captor.capture(),
                                             any(LineConsumer.class));
        CheServiceImpl actualService = captor.getValue();
        assertNull(actualService.getBuild().getContext());
        assertNull(actualService.getBuild().getDockerfilePath());
        assertEquals(actualService.getBuild().getDockerfileContent(), dockerfileContent);
    }

    @Test
    public void shouldNotSetDockerfileContentInsteadOfUrlIfUrlDoesNotPointToCheApiOnEnvironmentStart() throws Exception {
        // given
        EnvironmentImpl env = createEnv();
        String machineName = "machineWithDockerfileNotFromApi";
        String contextUrl = "http://another-server.com/recipe/12345";

        //prepare CheServicesEnvironmentImpl which should return compose parser
        CheServicesEnvironmentImpl cheServicesEnvironment = createCheServicesEnvByName(machineName);
        cheServicesEnvironment.getServices()
                              .get(machineName)
                              .withBuild(new CheServiceBuildContextImpl().withContext(contextUrl));

        // when
        startEnv(env, cheServicesEnvironment);

        // then
        ArgumentCaptor<CheServiceImpl> captor = ArgumentCaptor.forClass(CheServiceImpl.class);
        verify(machineProvider).startService(anyString(),
                                             anyString(),
                                             anyString(),
                                             eq(machineName),
                                             eq(false),
                                             anyString(),
                                             captor.capture(),
                                             any(LineConsumer.class));
        CheServiceImpl actualService = captor.getValue();
        assertNull(actualService.getBuild().getDockerfilePath());
        assertNull(actualService.getBuild().getDockerfileContent());
        assertEquals(actualService.getBuild().getContext(), contextUrl);
    }

    @Test
    public void shouldApplyAgentsOnEnvironmentStart() throws Exception {
        EnvironmentImpl env = createEnv();
        String machineName = "extraMachine";

        //prepare CheServicesEnvironmentImpl which should return compose parser
        CheServicesEnvironmentImpl cheServicesEnvironment = createCheServicesEnvByName(machineName);
        cheServicesEnvironment.getServices()
                              .get(machineName)
                              .withImage("codenvy/ubuntu_jdk8");

        // when
        startEnv(env, cheServicesEnvironment);

        // then
        verify(machineProvider).startService(anyString(),
                                             anyString(),
                                             anyString(),
                                             eq(machineName),
                                             eq(false),
                                             anyString(),
                                             any(CheServiceImpl.class),
                                             any(LineConsumer.class));
        verify(infrastructureProvisioner).provision(eq(env), any(CheServicesEnvironmentImpl.class));
        verifyNoMoreInteractions(infrastructureProvisioner);
    }

    @Test
    public void shouldSetDefaultRamToMachineWithoutRamOnMachineStart() throws Exception {
        // given
        List<Instance> instances = startEnv();
        String workspaceId = instances.get(0).getWorkspaceId();

        when(engine.generateMachineId()).thenReturn("newMachineId");
        Instance newMachine = mock(Instance.class);
        when(newMachine.getId()).thenReturn("newMachineId");
        when(newMachine.getWorkspaceId()).thenReturn(workspaceId);
        when(machineProvider.startService(anyString(),
                                          anyString(),
                                          anyString(),
                                          anyString(),
                                          anyBoolean(),
                                          anyString(),
                                          any(CheServiceImpl.class),
                                          any(LineConsumer.class)))
                .thenReturn(newMachine);

        MachineConfigImpl config = createConfig(false);
        String machineName = "extraMachine";
        config.setName(machineName);
        config.setLimits(null);

        // when
        engine.startMachine(workspaceId, config, emptyList());

        // then
        ArgumentCaptor<CheServiceImpl> captor = ArgumentCaptor.forClass(CheServiceImpl.class);
        verify(machineProvider).startService(anyString(),
                                             anyString(),
                                             anyString(),
                                             eq(machineName),
                                             eq(false),
                                             anyString(),
                                             captor.capture(),
                                             any(LineConsumer.class));
        CheServiceImpl actualService = captor.getValue();
        assertEquals((long)actualService.getMemLimit(), DEFAULT_MACHINE_MEM_LIMIT_MB * 1024L * 1024L);
    }

    @Test
    public void shouldUseConfiguredInMachineRamInsteadOfSetDefaultOnMachineStart() throws Exception {
        // given
        List<Instance> instances = startEnv();
        String workspaceId = instances.get(0).getWorkspaceId();

        when(engine.generateMachineId()).thenReturn("newMachineId");
        Instance newMachine = mock(Instance.class);
        when(newMachine.getId()).thenReturn("newMachineId");
        when(newMachine.getWorkspaceId()).thenReturn(workspaceId);
        when(machineProvider.startService(anyString(),
                                          anyString(),
                                          anyString(),
                                          anyString(),
                                          anyBoolean(),
                                          anyString(),
                                          any(CheServiceImpl.class),
                                          any(LineConsumer.class)))
                .thenReturn(newMachine);

        MachineConfigImpl config = createConfig(false);
        String machineName = "extraMachine";
        config.setName(machineName);
        config.setLimits(new MachineLimitsImpl(4096));

        // when
        engine.startMachine(workspaceId, config, emptyList());

        // then
        ArgumentCaptor<CheServiceImpl> captor = ArgumentCaptor.forClass(CheServiceImpl.class);
        verify(machineProvider).startService(anyString(),
                                             anyString(),
                                             anyString(),
                                             eq(machineName),
                                             eq(false),
                                             anyString(),
                                             captor.capture(),
                                             any(LineConsumer.class));
        CheServiceImpl actualService = captor.getValue();
        assertEquals((long)actualService.getMemLimit(), 4096L * 1024L * 1024L);
    }

    @Test
    public void shouldSetDockerfileContentInsteadOfUrlIfUrlPointsToCheApiOnMachineStart() throws Exception {
        // given
        List<Instance> instances = startEnv();
        String workspaceId = instances.get(0).getWorkspaceId();

        when(engine.generateMachineId()).thenReturn("newMachineId");
        Instance newMachine = mock(Instance.class);
        when(newMachine.getId()).thenReturn("newMachineId");
        when(newMachine.getWorkspaceId()).thenReturn(workspaceId);
        when(machineProvider.startService(anyString(),
                                          anyString(),
                                          anyString(),
                                          anyString(),
                                          anyBoolean(),
                                          anyString(),
                                          any(CheServiceImpl.class),
                                          any(LineConsumer.class)))
                .thenReturn(newMachine);

        MachineConfigImpl config = createConfig(false);
        String machineName = "extraMachine";
        config.setName(machineName);
        config.setSource(new MachineSourceImpl("docker").setLocation(API_ENDPOINT + "/recipe/12345"));
        String dockerfileContent = "this is dockerfile content";
        when(recipeDownloader.getRecipe(anyString())).thenReturn("this is dockerfile content");

        // when
        engine.startMachine(workspaceId, config, emptyList());

        // then
        ArgumentCaptor<CheServiceImpl> captor = ArgumentCaptor.forClass(CheServiceImpl.class);
        verify(machineProvider).startService(anyString(),
                                             anyString(),
                                             anyString(),
                                             eq(machineName),
                                             eq(false),
                                             anyString(),
                                             captor.capture(),
                                             any(LineConsumer.class));
        CheServiceImpl actualService = captor.getValue();
        assertNull(actualService.getBuild().getContext());
        assertNull(actualService.getBuild().getDockerfilePath());
        assertEquals(actualService.getBuild().getDockerfileContent(), dockerfileContent);
    }

    @Test
    public void shouldNotSetDockerfileContentInsteadOfUrlIfUrlDoesNotPointToCheApiOnMachineStart() throws Exception {
        // given
        List<Instance> instances = startEnv();
        String workspaceId = instances.get(0).getWorkspaceId();

        when(engine.generateMachineId()).thenReturn("newMachineId");
        Instance newMachine = mock(Instance.class);
        when(newMachine.getId()).thenReturn("newMachineId");
        when(newMachine.getWorkspaceId()).thenReturn(workspaceId);
        when(machineProvider.startService(anyString(),
                                          anyString(),
                                          anyString(),
                                          anyString(),
                                          anyBoolean(),
                                          anyString(),
                                          any(CheServiceImpl.class),
                                          any(LineConsumer.class)))
                .thenReturn(newMachine);

        MachineConfigImpl config = createConfig(false);
        String machineName = "extraMachine";
        config.setName(machineName);
        String contextUrl = "http://another-server.com/recipe/12345";
        config.setSource(new MachineSourceImpl("docker").setLocation(contextUrl));

        // when
        engine.startMachine(workspaceId, config, emptyList());

        // then
        ArgumentCaptor<CheServiceImpl> captor = ArgumentCaptor.forClass(CheServiceImpl.class);
        verify(machineProvider).startService(anyString(),
                                             anyString(),
                                             anyString(),
                                             eq(machineName),
                                             eq(false),
                                             anyString(),
                                             captor.capture(),
                                             any(LineConsumer.class));
        CheServiceImpl actualService = captor.getValue();
        assertNull(actualService.getBuild().getDockerfilePath());
        assertNull(actualService.getBuild().getDockerfileContent());
        assertEquals(actualService.getBuild().getContext(), contextUrl);
    }

    @Test
    public void shouldApplyAgentsOnDockerMachineStart() throws Exception {
        // given
        List<Instance> instances = startEnv();
        String workspaceId = instances.get(0).getWorkspaceId();

        when(engine.generateMachineId()).thenReturn("newMachineId");
        Instance newMachine = mock(Instance.class);
        when(newMachine.getId()).thenReturn("newMachineId");
        when(newMachine.getWorkspaceId()).thenReturn(workspaceId);
        when(machineProvider.startService(anyString(),
                                          anyString(),
                                          anyString(),
                                          anyString(),
                                          anyBoolean(),
                                          anyString(),
                                          any(CheServiceImpl.class),
                                          any(LineConsumer.class)))
                .thenReturn(newMachine);

        MachineConfigImpl config = createConfig(false);
        List<String> agents = asList("agent1", "agent2");

        // when
        engine.startMachine(workspaceId, config, agents);

        // then
        verify(infrastructureProvisioner).provision(any(ExtendedMachine.class), any(CheServiceImpl.class));
    }

    @Test
    public void envStartShouldFireEvents() throws Exception {
        // when
        List<Instance> instances = startEnv();
        assertTrue(instances.size() > 1, "This test requires at least 2 instances in environment");

        // then
        for (Instance instance : instances) {
            verify(eventService).publish(newDto(MachineStatusEvent.class)
                                                 .withEventType(MachineStatusEvent.EventType.CREATING)
                                                 .withDev(instance.getConfig().isDev())
                                                 .withMachineName(instance.getConfig().getName())
                                                 .withMachineId(instance.getId())
                                                 .withWorkspaceId(instance.getWorkspaceId()));
            verify(eventService).publish(newDto(MachineStatusEvent.class)
                                                 .withEventType(MachineStatusEvent.EventType.RUNNING)
                                                 .withDev(instance.getConfig().isDev())
                                                 .withMachineName(instance.getConfig().getName())
                                                 .withMachineId(instance.getId())
                                                 .withWorkspaceId(instance.getWorkspaceId()));
        }
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Environment of workspace '.*' already exists")
    public void envStartShouldThrowsExceptionIfSameEnvironmentExists() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);
        EnvironmentImpl env = createEnv();
        String envName = "env-1";

        // when
        engine.start(instance.getWorkspaceId(),
                     envName,
                     env,
                     false,
                     messageConsumer);
    }

    @Test
    public void shouldDestroyMachinesOnEnvStop() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);

        // when
        engine.stop(instance.getWorkspaceId());

        // then
        for (Instance instance1 : instances) {
            verify(instance1).destroy();
        }
    }

    @Test(expectedExceptions = EnvironmentNotRunningException.class,
          expectedExceptionsMessageRegExp = "Stop of not running environment of workspace with ID '.*' is not allowed.")
    public void shouldThrowExceptionOnEnvStopIfItIsNotRunning() throws Exception {
        engine.stop("wsIdOFNonExistingEnv");
    }

    @Test
    public void destroyOfMachineOnEnvStopShouldNotPreventStopOfOthers() throws Exception {
        // given
        List<Instance> instances = startEnv();
        assertTrue(instances.size() > 1, "This test requires at least 2 instances in environment");
        Instance instance = instances.get(0);
        doThrow(new MachineException("test exception")).when(instance).destroy();

        // when
        engine.stop(instance.getWorkspaceId());

        // then
        InOrder inOrder = inOrder(instances.toArray());
        for (Instance instance1 : instances) {
            inOrder.verify(instance1).destroy();
        }
    }

    @Test
    public void shouldBeAbleToStartMachine() throws Exception {
        // given
        List<Instance> instances = startEnv();
        verify(machineProvider, times(2)).startService(anyString(),
                                                       anyString(),
                                                       anyString(),
                                                       anyString(),
                                                       anyBoolean(),
                                                       anyString(),
                                                       any(CheServiceImpl.class),
                                                       any(LineConsumer.class));
        String workspaceId = instances.get(0).getWorkspaceId();

        when(engine.generateMachineId()).thenReturn("newMachineId");
        Instance newMachine = mock(Instance.class);
        when(newMachine.getId()).thenReturn("newMachineId");
        when(newMachine.getWorkspaceId()).thenReturn(workspaceId);
        when(machineProvider.startService(anyString(),
                                          anyString(),
                                          anyString(),
                                          anyString(),
                                          anyBoolean(),
                                          anyString(),
                                          any(CheServiceImpl.class),
                                          any(LineConsumer.class)))
                .thenReturn(newMachine);

        MachineConfigImpl config = createConfig(false);

        // when
        Instance actualInstance = engine.startMachine(workspaceId, config, emptyList());

        // then
        assertEquals(actualInstance, newMachine);
        verify(instanceProvider, never()).createInstance(any(Machine.class), any(LineConsumer.class));
        verify(machineProvider, times(3)).startService(anyString(),
                                                       anyString(),
                                                       anyString(),
                                                       anyString(),
                                                       anyBoolean(),
                                                       anyString(),
                                                       any(CheServiceImpl.class),
                                                       any(LineConsumer.class));
    }

    @Test
    public void shouldBeAbleToStartNonDockerMachine() throws Exception {
        // given
        ServerConf2 serverConf2 = mock(ServerConf2.class);
        when(serverConf2.getPort()).thenReturn("1111/tcp");
        when(serverConf2.getProtocol()).thenReturn("http");
        when(serverConf2.getProperties()).thenReturn(singletonMap("path", "some path"));
        when(agent.getServers()).thenAnswer(invocation -> singletonMap("ssh", serverConf2));

        List<Instance> instances = startEnv();
        String workspaceId = instances.get(0).getWorkspaceId();

        when(engine.generateMachineId()).thenReturn("newMachineId");
        Instance newMachine = mock(Instance.class);
        when(newMachine.getId()).thenReturn("newMachineId");
        when(newMachine.getWorkspaceId()).thenReturn(workspaceId);
        when(machineInstanceProviders.getProvider("anotherType")).thenReturn(instanceProvider);
        doReturn(newMachine).when(instanceProvider).createInstance(any(Machine.class), any(LineConsumer.class));

        MachineConfigImpl config = MachineConfigImpl.builder()
                                                    .fromConfig(createConfig(false))
                                                    .setType("anotherType")
                                                    .build();

        // when
        Instance actualInstance = engine.startMachine(workspaceId, config, singletonList("agent"));

        // then
        assertEquals(actualInstance, newMachine);
        ArgumentCaptor<Machine> argumentCaptor = ArgumentCaptor.forClass(Machine.class);
        verify(instanceProvider).createInstance(argumentCaptor.capture(), any(LineConsumer.class));

        MachineConfigImpl newConfig = new MachineConfigImpl(config);
        newConfig.setServers(singletonList(new ServerConfImpl("ssh", "1111/tcp", "http", "some path")));
        assertEquals(argumentCaptor.getValue().getConfig(), newConfig);
    }

    @Test(expectedExceptions = EnvironmentNotRunningException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' is not running")
    public void shouldThrowExceptionOnMachineStartIfEnvironmentIsNotRunning() throws Exception {
        MachineConfigImpl config = createConfig(false);

        // when
        engine.startMachine("wsIdOfNotRunningEnv", config, emptyList());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Machine with name '.*' already exists in environment of workspace '.*'")
    public void machineStartShouldThrowExceptionIfMachineWithTheSameNameAlreadyExistsInEnvironment() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);

        MachineConfigImpl config = createConfig(false);
        config.setName(instance.getConfig().getName());

        // when
        engine.startMachine(instance.getWorkspaceId(), config, emptyList());
    }

    @Test
    public void machineStartShouldPublishEvents() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);

        MachineConfigImpl config = createConfig(false);
        when(engine.generateMachineId()).thenReturn("newMachineId");

        // when
        engine.startMachine(instance.getWorkspaceId(), config, emptyList());

        // then
        verify(eventService).publish(newDto(MachineStatusEvent.class)
                                             .withEventType(MachineStatusEvent.EventType.CREATING)
                                             .withDev(config.isDev())
                                             .withMachineName(config.getName())
                                             .withMachineId("newMachineId")
                                             .withWorkspaceId(instance.getWorkspaceId()));
        verify(eventService).publish(newDto(MachineStatusEvent.class)
                                             .withEventType(MachineStatusEvent.EventType.RUNNING)
                                             .withDev(config.isDev())
                                             .withMachineName(config.getName())
                                             .withMachineId("newMachineId")
                                             .withWorkspaceId(instance.getWorkspaceId()));
    }

    @Test
    public void shouldBeAbleToStopMachine() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Optional<Instance> instanceOpt = instances.stream()
                                                  .filter(machine -> !machine.getConfig().isDev())
                                                  .findAny();
        assertTrue(instanceOpt.isPresent(), "Required for test non-dev machine is not found");
        Instance instance = instanceOpt.get();

        // when
        engine.stopMachine(instance.getWorkspaceId(), instance.getId());

        // then
        verify(instance).destroy();
    }

    @Test(expectedExceptions = EnvironmentNotRunningException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' is not running")
    public void machineStopShouldThrowExceptionIfEnvDoesNotExist() throws Exception {
        engine.stopMachine("wsIdOfNotRunningEnv", "testMachineID");
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Stop of dev machine is not allowed. Please, stop whole environment")
    public void devMachineStopShouldThrowException() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Optional<Instance> instanceOpt = instances.stream()
                                                  .filter(machine -> machine.getConfig().isDev())
                                                  .findAny();
        assertTrue(instanceOpt.isPresent(), "Required for test dev machine is not found");
        Instance instance = instanceOpt.get();

        // when
        engine.stopMachine(instance.getWorkspaceId(), instance.getId());
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Machine with ID '.*' is not found in environment of workspace '.*'")
    public void machineStopOfNonExistingMachineShouldThrowsException() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);

        // when
        engine.stopMachine(instance.getWorkspaceId(), "idOfNonExistingMachine");
    }

    @Test
    public void machineStopShouldFireEvents() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Optional<Instance> instanceOpt = instances.stream()
                                                  .filter(machine -> !machine.getConfig().isDev())
                                                  .findAny();
        assertTrue(instanceOpt.isPresent(), "Required for test non-dev machine is not found");
        Instance instance = instanceOpt.get();

        // when
        engine.stopMachine(instance.getWorkspaceId(), instance.getId());

        // then
        verify(eventService).publish(newDto(MachineStatusEvent.class)
                                             .withEventType(MachineStatusEvent.EventType.CREATING)
                                             .withDev(instance.getConfig().isDev())
                                             .withMachineName(instance.getConfig().getName())
                                             .withMachineId(instance.getId())
                                             .withWorkspaceId(instance.getWorkspaceId()));
        verify(eventService).publish(newDto(MachineStatusEvent.class)
                                             .withEventType(MachineStatusEvent.EventType.RUNNING)
                                             .withDev(instance.getConfig().isDev())
                                             .withMachineName(instance.getConfig().getName())
                                             .withMachineId(instance.getId())
                                             .withWorkspaceId(instance.getWorkspaceId()));
    }

    @Test
    public void shouldBeAbleToSaveMachineSnapshot() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);
        doReturn(new MachineSourceImpl("someType").setContent("some content")).when(instance).saveToSnapshot();

        // when
        engine.saveSnapshot("someNamespace", instance.getWorkspaceId(), instance.getId());

        // then
        verify(instance).saveToSnapshot();
    }

    @Test(expectedExceptions = EnvironmentNotRunningException.class,
          expectedExceptionsMessageRegExp = "Environment .*' is not running")
    public void shouldThrowExceptionOnSaveSnapshotIfEnvIsNotRunning() throws Exception {
        engine.saveSnapshot("someNamespace", "wsIdOfNotRunningEnv", "someId");
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Machine with id '.*' is not found in environment of workspace '.*'")
    public void shouldThrowExceptionOnSaveSnapshotIfMachineIsNotFound() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);

        // when
        engine.saveSnapshot("someNamespace", instance.getWorkspaceId(), "idOfNonExistingMachine");
    }

    @Test
    public void shouldBeAbleToRemoveSnapshot() throws Exception {
        // given
        SnapshotImpl snapshot = mock(SnapshotImpl.class);
        MachineSourceImpl machineSource = mock(MachineSourceImpl.class);
        when(snapshot.getType()).thenReturn("docker");
        when(snapshot.getMachineSource()).thenReturn(machineSource);

        // when
        engine.removeSnapshot(snapshot);

        // then
        verify(instanceProvider).removeInstanceSnapshot(machineSource);
    }

    @Test
    public void shouldReplaceServiceNameWithContainerNameInLinks() {
        //given
        final String serviceNameToLink = "service";
        final String containerNameToLink = "container";

        List<String> links = new ArrayList<>();
        links.add(serviceNameToLink);

        CheServiceImpl serviceToNormalizeLinks = new CheServiceImpl().withLinks(links);

        CheServiceImpl service1 = mock(CheServiceImpl.class);
        CheServiceImpl service2 = mock(CheServiceImpl.class);

        Map<String, CheServiceImpl> allServices = new HashMap<>();
        allServices.put("this", serviceToNormalizeLinks);
        allServices.put(serviceNameToLink, service1);
        allServices.put("anotherService", service2);

        when(service1.getContainerName()).thenReturn(containerNameToLink);

        // when
        engine.normalizeLinks(serviceToNormalizeLinks, allServices);

        // then
        assertEquals(serviceToNormalizeLinks.getLinks().size(), 1);
        assertEquals(serviceToNormalizeLinks.getLinks().get(0), containerNameToLink);
    }

    @Test
    public void shouldReplaceServiceNameWithContainerNameAndUseAliasInLinks() {
        //given
        final String serviceNameToLink = "service";
        final String containerNameToLink = "container";
        final String AliasToServiceToLink = "alias";

        List<String> links = new ArrayList<>();
        links.add(serviceNameToLink + ':' + AliasToServiceToLink);

        CheServiceImpl serviceToNormalizeLinks = new CheServiceImpl().withLinks(links);

        CheServiceImpl service1 = mock(CheServiceImpl.class);
        CheServiceImpl service2 = mock(CheServiceImpl.class);

        Map<String, CheServiceImpl> allServices = new HashMap<>();
        allServices.put("this", serviceToNormalizeLinks);
        allServices.put(serviceNameToLink, service1);
        allServices.put("anotherService", service2);

        when(service1.getContainerName()).thenReturn(containerNameToLink);

        // when
        engine.normalizeLinks(serviceToNormalizeLinks, allServices);

        // then
        assertEquals(serviceToNormalizeLinks.getLinks().size(), 1);
        assertEquals(serviceToNormalizeLinks.getLinks().get(0), containerNameToLink + ':' + AliasToServiceToLink);
    }

    private List<Instance> startEnv() throws Exception {
        EnvironmentImpl env = createEnv();
        CheServicesEnvironmentImpl cheServicesEnv = createCheServicesEnv();
        return startEnv(env, cheServicesEnv);
    }

    private List<Instance> startEnv(EnvironmentImpl env, CheServicesEnvironmentImpl cheServicesEnv) throws Exception {
        String envName = "env-1";
        String workspaceId = "wsId";
        when(machineProvider.startService(anyString(),
                                          eq(workspaceId),
                                          eq(envName),
                                          anyString(),
                                          anyBoolean(),
                                          anyString(),
                                          any(CheServiceImpl.class),
                                          any(LineConsumer.class)))
                .thenAnswer(invocationOnMock -> {
                    Object[] arguments = invocationOnMock.getArguments();
                    String machineName = (String)arguments[3];
                    boolean isDev = (boolean)arguments[4];
                    CheServiceImpl service = (CheServiceImpl)arguments[6];
                    Machine machine = createMachine(workspaceId,
                                                    envName,
                                                    service,
                                                    machineName,
                                                    isDev);
                    return spy(new NoOpMachineInstance(machine));
                });


        when(environmentParser.parse(env)).thenReturn(cheServicesEnv);

        // when
        return engine.start(workspaceId,
                            envName,
                            env,
                            false,
                            messageConsumer);
    }

    private static MachineConfigImpl createConfig(boolean isDev) {
        return MachineConfigImpl.builder()
                                .setDev(isDev)
                                .setType("docker")
                                .setLimits(new MachineLimitsImpl(1024))
                                .setSource(new MachineSourceImpl("dockerfile").setLocation("location"))
                                .setName(UUID.randomUUID().toString())
                                .build();
    }

    private EnvironmentImpl createEnv() {
        // singletonMap, asList are wrapped into modifiable collections to ease env modifying by tests
        EnvironmentImpl env = new EnvironmentImpl();
        Map<String, ExtendedMachineImpl> machines = new HashMap<>();
        Map<String, ServerConf2Impl> servers = new HashMap<>();

        servers.put("ref1", new ServerConf2Impl("8080/tcp",
                                                "proto1",
                                                singletonMap("prop1", "propValue")));
        servers.put("ref2", new ServerConf2Impl("8080/udp", "proto1", null));
        servers.put("ref3", new ServerConf2Impl("9090", "proto1", null));
        machines.put("dev-machine", new ExtendedMachineImpl(asList("org.eclipse.che.ws-agent", "someAgent"),
                                                            servers,
                                                            singletonMap("memoryLimitBytes", "10000")));
        machines.put("machine2", new ExtendedMachineImpl(asList("someAgent2", "someAgent3"),
                                                         null,
                                                         singletonMap("memoryLimitBytes", "10000")));
        String environmentRecipeContent =
                "services:\n  " +
                "dev-machine:\n    image: codenvy/ubuntu_jdk8\n    mem_limit: 4294967296\n  " +
                "machine2:\n    image: codenvy/ubuntu_jdk8\n    mem_limit: 100000";
        env.setRecipe(new EnvironmentRecipeImpl("compose",
                                                "application/x-yaml",
                                                environmentRecipeContent,
                                                null));
        env.setMachines(machines);

        return env;
    }

    private CheServicesEnvironmentImpl createCheServicesEnv() {
        CheServicesEnvironmentImpl cheServicesEnvironment = new CheServicesEnvironmentImpl();
        Map<String, CheServiceImpl> services = new HashMap<>();

        services.put("dev-machine", new CheServiceImpl().withBuild(new CheServiceBuildContextImpl().withContext("image")));
        services.put("machine2", new CheServiceImpl().withBuild(new CheServiceBuildContextImpl().withContext("image")));

        cheServicesEnvironment.setServices(services);

        return cheServicesEnvironment;
    }

    private CheServicesEnvironmentImpl createCheServicesEnvByName(String name) {
        CheServicesEnvironmentImpl cheServicesEnvironment = new CheServicesEnvironmentImpl();

        Map<String, CheServiceImpl> services = new HashMap<>();
        services.put(name, new CheServiceImpl().withBuild(new CheServiceBuildContextImpl().withContext("image")));
        cheServicesEnvironment.setServices(services);

        return cheServicesEnvironment;
    }

    private static MachineImpl createMachine(String workspaceId,
                                             String envName,
                                             CheServiceImpl service,
                                             String serviceName,
                                             boolean isDev) {
        MachineSourceImpl machineSource;
        if (service.getBuild() != null && service.getBuild().getContext() != null) {
            machineSource = new MachineSourceImpl("dockerfile").setLocation(service.getBuild().getContext());
        } else if (service.getImage() != null) {
            machineSource = new MachineSourceImpl("image").setLocation(service.getImage());
        } else if (service.getBuild() != null &&
                   service.getBuild().getContext() == null &&
                   service.getBuild().getDockerfileContent() != null) {

            machineSource = new MachineSourceImpl("dockerfile").setContent(service.getBuild().getDockerfileContent());
        } else {
            throw new IllegalArgumentException("Build context or image should contain non empty value");
        }
        MachineLimitsImpl limits = new MachineLimitsImpl((int)Size.parseSizeToMegabytes(service.getMemLimit() + "b"));

        return MachineImpl.builder()
                          .setConfig(MachineConfigImpl.builder()
                                                      .setDev(isDev)
                                                      .setName(serviceName)
                                                      .setSource(machineSource)
                                                      .setLimits(limits)
                                                      .setType("docker")
                                                      .build())
                          .setId(service.getId())
                          .setOwner("userName")
                          .setStatus(MachineStatus.RUNNING)
                          .setWorkspaceId(workspaceId)
                          .setEnvName(envName)
                          .setRuntime(new MachineRuntimeInfoImpl(emptyMap(),
                                                                 emptyMap(),
                                                                 emptyMap()))
                          .build();
    }
}
