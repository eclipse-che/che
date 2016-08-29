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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineLogMessage;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.MessageConsumer;
import org.eclipse.che.api.environment.server.exception.EnvironmentNotRunningException;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class CheEnvironmentEngineTest {
    @Mock
    MessageConsumer<MachineLogMessage> messageConsumer;
    @Mock
    InstanceProvider                   instanceProvider;

    @Mock
    SnapshotDao              snapshotDao;
    @Mock
    MachineInstanceProviders machineInstanceProviders;
    @Mock
    EventService             eventService;


    CheEnvironmentEngine engine;

    @BeforeMethod
    public void setUp() throws Exception {
        engine = spy(new CheEnvironmentEngine(snapshotDao,
                                              machineInstanceProviders,
                                              "/tmp",
                                              256,
                                              eventService));

        when(machineInstanceProviders.getProvider("docker")).thenReturn(instanceProvider);
        when(instanceProvider.getRecipeTypes()).thenReturn(Collections.singleton("dockerfile"));

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
        String workspaceId = "wsId";
        List<Instance> expectedMachines = new ArrayList<>();
        when(instanceProvider.createInstance(any(Machine.class),
                                             any(LineConsumer.class)))
                .thenAnswer(invocationOnMock -> {
                    Object[] arguments = invocationOnMock.getArguments();
                    Machine machine = (Machine)arguments[0];
                    Instance instance = spy(new NoOpMachineInstance(machine));
                    expectedMachines.add(instance);
                    return instance;
                });

        // when
        List<Instance> machines = engine.start(workspaceId, env, false, messageConsumer);

        // then
        assertEquals(machines, expectedMachines);
        verify(instanceProvider, times(env.getMachineConfigs().size()))
                .createInstance(any(Machine.class), any(LineConsumer.class));
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

        // when
        engine.start(instance.getWorkspaceId(),
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
          expectedExceptionsMessageRegExp = "Environment with ID '.*' is not found")
    public void shouldThrowExceptionOnEnvStopIfItIsNotRunning() throws Exception {
        engine.stop("wsIdOFNonExistingEnv");
    }

    @Test
    public void destroyOfMachineOnEnvStopShouldNotPreventStopOfOthers() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);
        doThrow(new MachineException("test exception")).when(instance).destroy();
        assertTrue(instances.size() > 1, "This test requires at least 2 instances in environment");

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
        String workspaceId = instances.get(0).getWorkspaceId();

        when(engine.generateMachineId()).thenReturn("newMachineId");
        Instance newMachine = mock(Instance.class);
        when(newMachine.getId()).thenReturn("newMachineId");
        when(newMachine.getWorkspaceId()).thenReturn(workspaceId);
        doReturn(newMachine).when(instanceProvider).createInstance(any(Machine.class), any(LineConsumer.class));

        MachineConfigImpl config = createConfig(false);

        // when
        Instance actualInstance = engine.startMachine(workspaceId, config);

        // then
        assertEquals(actualInstance, newMachine);
        ArgumentCaptor<Machine> argumentCaptor = ArgumentCaptor.forClass(Machine.class);
        verify(instanceProvider, times(3)).createInstance(argumentCaptor.capture(), any(LineConsumer.class));
        assertEquals(argumentCaptor.getValue().getConfig(), config);
    }

    @Test(expectedExceptions = EnvironmentNotRunningException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' is not running")
    public void shouldThrowExceptionOnMachineStartIfEnvironmentIsNotRunning() throws Exception {
        MachineConfigImpl config = createConfig(false);

        // when
        engine.startMachine("wsIdOfNotRunningEnv", config);
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
        engine.startMachine(instance.getWorkspaceId(), config);
    }

    @Test
    public void machineStartShouldPublishEvents() throws Exception {
        // given
        List<Instance> instances = startEnv();
        Instance instance = instances.get(0);

        MachineConfigImpl config = createConfig(false);
        when(engine.generateMachineId()).thenReturn("newMachineId");

        // when
        engine.startMachine(instance.getWorkspaceId(), config);

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

    private List<Instance> startEnv() throws Exception {
        EnvironmentImpl env = createEnv();
        String workspaceId = "wsId";
        when(instanceProvider.createInstance(any(Machine.class),
                                             any(LineConsumer.class)))
                .thenAnswer(invocationOnMock -> {
                    Object[] arguments = invocationOnMock.getArguments();
                    Machine machine = (Machine)arguments[0];
                    return spy(new NoOpMachineInstance(machine));
                });

        // when
        return engine.start(workspaceId, env, false, messageConsumer);
    }

    private static MachineConfigImpl createConfig(boolean isDev) {
        return MachineConfigImpl.builder()
                                .setDev(isDev)
                                .setType("docker")
                                .setLimits(new LimitsImpl(1024))
                                .setSource(new MachineSourceImpl("dockerfile").setLocation("location"))
                                .setName(UUID.randomUUID().toString())
                                .build();
    }

    private EnvironmentImpl createEnv() {
        List<MachineConfigImpl> machines = new ArrayList<>();
        machines.add(createConfig(true));
        machines.add(createConfig(false));
        return new EnvironmentImpl("envName",
                                   null,
                                   machines);
    }
}
