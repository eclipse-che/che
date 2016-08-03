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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes.RuntimeDescriptor;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceRuntimesTest {

    private static final String WORKSPACE_ID = "workspace123";
    private static final String ENV_NAME     = "default-env";

    @Mock
    private MachineManager machineManager;

    @Mock
    private EventService eventService;

    @Mock
    private RuntimeDescriptor descriptor;

    private WorkspaceRuntimes runtimes;

    @BeforeMethod
    public void setUp() throws Exception {
        when(machineManager.createMachineSync(any(), any(), any(), any(LineConsumer.class)))
                .thenAnswer(invocation -> createMachine((MachineConfig)invocation.getArguments()[0]));
        runtimes = new WorkspaceRuntimes(machineManager, eventService);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id '.*' is not running.")
    public void shouldThrowNotFoundExceptionIfWorkspaceRuntimeDoesNotExist() throws Exception {
        runtimes.get("workspace123");
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Could not perform operation because application server is stopping")
    public void shouldNotStartTheWorkspaceIfPostConstructWasIsInvoked() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes.cleanup();

        runtimes.start(createWorkspace(), workspace.getConfig().getDefaultEnv(), false);
    }

    @Test
    public void workspaceShouldBeInStartingStatusUntilDevMachineIsNotStarted() throws Exception {
        final MachineManager machineManagerMock = mock(MachineManager.class);
        final WorkspaceRuntimes runtimes = new WorkspaceRuntimes(machineManagerMock, eventService);
        final WorkspaceImpl workspace = createWorkspace();

        // check if workspace in starting status before dev machine is started
        when(machineManagerMock.createMachineSync(anyObject(), anyString(), anyString(), any(LineConsumer.class)))
                .thenAnswer(invocationOnMock -> {
                    final RuntimeDescriptor descriptor = runtimes.get(workspace.getId());
                    final MachineConfig cfg = (MachineConfig)invocationOnMock.getArguments()[0];
                    if (cfg.isDev()) {
                        assertEquals(descriptor.getRuntimeStatus(), STARTING, "Workspace status is not 'STARTING'");
                    }
                    return createMachine((MachineConfig)invocationOnMock.getArguments()[0]);
                });

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv(), false);

        verify(machineManagerMock, times(2)).createMachineSync(anyObject(), anyString(), anyString(), any(LineConsumer.class));
    }

    @Test
    public void workspaceShouldNotHaveRuntimeIfDevMachineCreationFailed() throws Exception {
        final MachineManager machineManagerMock = mock(MachineManager.class);
        final WorkspaceRuntimes runtimes = new WorkspaceRuntimes(machineManagerMock, eventService);
        final WorkspaceImpl workspaceMock = createWorkspace();
        when(machineManagerMock.createMachineSync(any(), any(), any(), any(LineConsumer.class)))
                .thenThrow(new MachineException("Creation error"));

        try {
            runtimes.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
        } catch (MachineException ex) {
            assertFalse(runtimes.hasRuntime(workspaceMock.getId()));
        }
    }

    @Test
    public void workspaceShouldContainAllMachinesAndBeInRunningStatusAfterSuccessfulStart() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        final RuntimeDescriptor runningWorkspace = runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        assertEquals(runningWorkspace.getRuntimeStatus(), RUNNING);
        assertNotNull(runningWorkspace.getRuntime().getDevMachine());
        assertEquals(runningWorkspace.getRuntime().getMachines().size(), 2);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not start workspace '.*' because its status is 'RUNNING'")
    public void shouldNotStartWorkspaceIfItIsAlreadyRunning() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Couldn't stop '.*' workspace because its status is 'STARTING'. " +
                                            "Workspace can be stopped only if it is 'RUNNING'")
    public void shouldNotStopWorkspaceIfItIsStarting() throws Exception {
        final MachineManager machineManagerMock = mock(MachineManager.class);
        final WorkspaceRuntimes registry = new WorkspaceRuntimes(machineManagerMock, eventService);
        final WorkspaceImpl workspace = createWorkspace();

        when(machineManagerMock.createMachineSync(any(), any(), any(), any(LineConsumer.class))).thenAnswer(invocationOnMock -> {
            registry.stop(workspace.getId());
            return createMachine((MachineConfig)invocationOnMock.getArguments()[0]);
        });

        registry.start(workspace, workspace.getConfig().getDefaultEnv());
    }

    @Test
    public void shouldDestroyNonDevMachineIfWorkspaceWasStoppedWhileNonDevMachineWasStarting() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        doAnswer(invocation -> {
            final MachineConfig machineCfg = (MachineConfig)invocation.getArguments()[0];
            if (!machineCfg.isDev()) {
                runtimes.stop(workspace.getId());
            }
            return createMachine((MachineConfig)invocation.getArguments()[0]);
        }).when(machineManager).createMachineSync(any(), anyString(), anyString(), any(LineConsumer.class));

        try {
            runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        } catch (ConflictException ex) {
            assertEquals(ex.getMessage(), "Workspace '" + workspace.getId() + "' start interrupted. " +
                                          "Workspace stopped before all its machines started");
        }
        verify(machineManager, times(2)).destroy(any(), anyBoolean());
    }

    @Test
    public void testCleanup() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        runtimes.cleanup();

        assertFalse(runtimes.hasRuntime(workspace.getId()));
    }

    @Test
    public void startShouldIgnoreFailedToStartNonDevMachine() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        doAnswer(invocation -> {
            final MachineConfig machineCfg = (MachineConfig)invocation.getArguments()[0];
            if (!machineCfg.isDev()) {
                throw new MachineException("Failed to start");
            }
            return createMachine((MachineConfig)invocation.getArguments()[0]);
        }).when(machineManager).createMachineSync(any(), anyString(), anyString(), any(LineConsumer.class));


        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        final RuntimeDescriptor descriptor = runtimes.get(workspace.getId());
        assertEquals(descriptor.getRuntime().getMachines().size(), 1);
        assertEquals(descriptor.getRuntimeStatus(), RUNNING);
        verify(machineManager, times(2)).createMachineSync(any(), any(), any(), any(LineConsumer.class));
    }

    @Test
    public void shouldNotDestroyNonDevMachineIfRegistryWasStoppedWhileDevMachineWasStarting() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        doAnswer(invocation -> {
            final MachineConfig machineCfg = (MachineConfig)invocation.getArguments()[0];
            if (!machineCfg.isDev()) {
                runtimes.cleanup();
            }
            return createMachine((MachineConfig)invocation.getArguments()[0]);
        }).when(machineManager).createMachineSync(any(), anyString(), anyString(), any(LineConsumer.class));

        try {
            runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        } catch (ServerException ex) {
            assertEquals(ex.getMessage(), "Could not perform operation because application server is stopping");
        }
        verify(machineManager, never()).destroy(any(), anyBoolean());
    }

    @Test
    public void runtimeShouldBeInStoppingStatusIfWorkspacesDevMachineIsNotStopped() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        doAnswer(invocation -> {
            assertEquals(runtimes.get(workspace.getId()).getRuntimeStatus(), STOPPING);
            return null;
        }).when(machineManager).destroy(any(), anyBoolean());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        runtimes.stop(workspace.getId());
    }

    @Test
    public void runtimeStopShouldIgnoreNonDevMachineFail() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        doAnswer(invocation -> {
            if (!runtimes.get(workspace.getId())
                         .getRuntime()
                         .getDevMachine()
                         .getId()
                         .equals(invocation.getArguments()[0])) {
                throw new MachineException("Destroy failed");
            }
            return null;
        }).when(machineManager).destroy(any(), anyBoolean());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        runtimes.stop(workspace.getId());

        assertFalse(runtimes.hasRuntime(workspace.getId()));
        verify(machineManager, times(2)).destroy(anyString(), anyBoolean());
    }

    @Test
    public void shouldStopRunningWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        runtimes.stop(workspace.getId());

        assertFalse(runtimes.hasRuntime(workspace.getId()));
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id 'workspace123' is not running.")
    public void shouldThrowNotFoundExceptionWhenStoppingWorkspaceWhichDoesNotHaveRuntime() throws Exception {
        runtimes.stop("workspace123");
    }

    @Test
    public void startedRuntimeShouldBeTheSameToRuntimeTakenFromGetMethod() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        final RuntimeDescriptor descriptor = runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        assertEquals(runtimes.get(workspace.getId()).getRuntime(), descriptor.getRuntime());
    }

    @Test
    public void startingEventShouldBePublishedBeforeStart() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());

        doAnswer(invocation -> {
            verify(runtimes).publishEvent(EventType.STARTING, workspace.getId(), null);
            return null;
        }).when(machineManager).createMachineSync(any(), any(), any(), any(LineConsumer.class));

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
    }

    @Test
    public void runningEventShouldBePublishedAfterDevMachineStarted() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());

        doAnswer(invocation -> {
            final MachineConfig cfg = (MachineConfig)invocation.getArguments()[0];
            if (!cfg.isDev()) {
                verify(runtimes).publishEvent(EventType.RUNNING, workspace.getId(), null);
            }
            return createMachine(cfg);
        }).when(machineManager).createMachineSync(any(), any(), any(), any(LineConsumer.class));

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
    }

    @Test
    public void errorEventShouldBePublishedIfDevMachineFailedToStart() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());
        doNothing().when(runtimes).cleanupStartResources(any());

        doAnswer(invocation -> {
            final MachineConfig cfg = (MachineConfig)invocation.getArguments()[0];
            if (cfg.isDev()) {
                throw new MachineException("Start error");
            }
            return createMachine(cfg);
        }).when(machineManager).createMachineSync(any(), any(), any(), any(LineConsumer.class));

        try {
            runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        } catch (MachineException ex) {
            verify(runtimes).publishEvent(EventType.ERROR, workspace.getId(), ex.getLocalizedMessage());
        }
    }

    @Test
    public void stoppingEventShouldBePublishedBeforeStop() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());

        doAnswer(invocation -> {
            if ((!runtimes.get(workspace.getId())
                          .getRuntime()
                          .getDevMachine()
                          .getId()
                          .equals(invocation.getArguments()[0]))) {
                verify(runtimes).publishEvent(EventType.STOPPING, workspace.getId(), null);
            }
            return null;
        }).when(machineManager).destroy(anyString(), anyBoolean());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        runtimes.stop(workspace.getId());
    }

    @Test
    public void stoppedEventShouldBePublishedAfterDevMachineStopped() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        runtimes.stop(workspace.getId());

        verify(runtimes).publishEvent(EventType.STOPPED, workspace.getId(), null);
    }

    @Test
    public void errorEventShouldBePublishedIfDevMachineFailedToStop() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());

        doAnswer(invocation -> {
            if ((runtimes.get(workspace.getId())
                         .getRuntime()
                         .getDevMachine()
                         .getId()
                         .equals(invocation.getArguments()[0]))) {
                throw new MachineException("Stop error");
            }
            return null;
        }).when(machineManager).destroy(anyString(), anyBoolean());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        try {
            runtimes.stop(workspace.getId());
        } catch (MachineException ex) {
            verify(runtimes).publishEvent(EventType.ERROR, workspace.getId(), ex.getLocalizedMessage());
        }
    }

    @Test
    public void shouldNotAddMachineIfWorkspaceDescriptorDoesNotExist() throws Exception {
        assertFalse(runtimes.addMachine(createMachine(true)), "should not be added");
    }

    @Test
    public void shouldPromiseToAddMachineIfStatusIsStartingAndCreatedMachineIsDev() throws Exception {
        when(descriptor.getRuntimeStatus()).thenReturn(STARTING);
        runtimes.descriptors.put(WORKSPACE_ID, descriptor);

        assertTrue(runtimes.addMachine(createMachine(true)), "should be added later");
    }

    @Test(dataProvider = "inconsistentMachines")
    public void machineShouldNotBeAdded(boolean isDev, WorkspaceStatus status) throws Exception {
        when(descriptor.getRuntimeStatus()).thenReturn(status);
        runtimes.descriptors.put(WORKSPACE_ID, descriptor);

        assertFalse(runtimes.addMachine(createMachine(isDev)), "should not be added");
    }

    @Test
    public void machineShouldBeAddedIfStartQueueDoesNotExist() throws Exception {
        // prepare runtime
        final WorkspaceRuntimeImpl runtime = mock(WorkspaceRuntimeImpl.class);
        final ArrayList<MachineImpl> machines = new ArrayList<>();
        when(runtime.getMachines()).thenReturn(machines);
        // prepare descriptor
        when(descriptor.getRuntime()).thenReturn(runtime);
        when(descriptor.getRuntimeStatus()).thenReturn(RUNNING);
        // register mocks
        runtimes.descriptors.put(WORKSPACE_ID, descriptor);

        final MachineImpl machine = createMachine(false);
        assertTrue(runtimes.addMachine(machine), "should be added");
        assertFalse(machines.isEmpty());
        assertEquals(machines.get(0), machine);
    }

    @Test
    public void machineShouldBeAddedIfStartQueueDoesNotContainIt() throws Exception {
        // prepare runtime
        final WorkspaceRuntimeImpl runtime = mock(WorkspaceRuntimeImpl.class);
        final ArrayList<MachineImpl> machines = new ArrayList<>();
        when(runtime.getMachines()).thenReturn(machines);
        // prepare descriptor
        when(descriptor.getRuntime()).thenReturn(runtime);
        when(descriptor.getRuntimeStatus()).thenReturn(RUNNING);
        // register mocks
        runtimes.descriptors.put(WORKSPACE_ID, descriptor);
        runtimes.startQueues.put(WORKSPACE_ID, new ArrayDeque<>());

        final MachineImpl machine = createMachine(false);
        assertTrue(runtimes.addMachine(machine), "should be added");
        assertFalse(machines.isEmpty());
        assertEquals(machines.get(0), machine);
    }

    @Test
    public void shouldPromiseThatMachineWillBeAddedIfStartQueueContainsMachine() throws Exception {
        // prepare runtime
        final WorkspaceRuntimeImpl runtime = mock(WorkspaceRuntimeImpl.class);
        final ArrayList<MachineImpl> machines = new ArrayList<>();
        when(runtime.getMachines()).thenReturn(machines);
        // prepare descriptor
        when(descriptor.getRuntime()).thenReturn(runtime);
        when(descriptor.getRuntimeStatus()).thenReturn(RUNNING);
        // prepare queue
        final ArrayDeque<MachineConfigImpl> queue = new ArrayDeque<>();
        queue.add(createConfig(false));
        // register mocks
        runtimes.descriptors.put(WORKSPACE_ID, descriptor);
        runtimes.startQueues.put(WORKSPACE_ID, queue);

        final MachineImpl machine = createMachine(false);
        assertTrue(runtimes.addMachine(machine), "should be added");
        assertTrue(machines.isEmpty());
    }

    @Test
    public void shouldDestroyMachineIfItIsNotAddedWhenEventReceived() throws Exception {
        // prepare runtimes
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doReturn(false).when(runtimes).addMachine(any());
        // prepare machine
        final MachineImpl machine = createMachine(true);
        when(machineManager.getMachine(machine.getId())).thenReturn(machine);
        // prepare event
        final MachineStatusEvent event = DtoFactory.newDto(MachineStatusEvent.class)
                                                   .withDev(true)
                                                   .withEventType(MachineStatusEvent.EventType.RUNNING)
                                                   .withMachineId(machine.getId());


        runtimes.new AddMachineEventSubscriber().onEvent(event);

        verify(machineManager).destroy(machine.getId(), true);
    }

    @Test(dataProvider = "machineEventTypesExceptOfRunning")
    public void eventTypesExceptOfRunningShouldBeIgnoredByAddMachineSubscriber(MachineStatusEvent.EventType type)
            throws Exception {
        // prepare runtimes
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doReturn(false).when(runtimes).addMachine(any());
        // prepare machine
        final MachineImpl machine = createMachine(true);
        when(machineManager.getMachine(machine.getId())).thenReturn(machine);
        // prepare event
        final MachineStatusEvent event = DtoFactory.newDto(MachineStatusEvent.class)
                                                   .withDev(true)
                                                   .withEventType(type)
                                                   .withMachineId(machine.getId());


        runtimes.new AddMachineEventSubscriber().onEvent(event);

        verify(machineManager, never()).destroy(machine.getId(), true);
    }

    @Test(dataProvider = "workspaceStatusesExceptOfRunning")
    public void shouldNotRemoveMachineWhenDescriptorStatusIsNotRunning(WorkspaceStatus status) throws Exception {
        // prepare runtime
        final WorkspaceRuntimeImpl runtime = mock(WorkspaceRuntimeImpl.class);
        final MachineImpl machine = createMachine(false);
        final ArrayList<MachineImpl> machines = new ArrayList<>();
        machines.add(machine);
        when(runtime.getMachines()).thenReturn(machines);
        // prepare descriptor
        when(descriptor.getRuntime()).thenReturn(runtime);
        when(descriptor.getRuntimeStatus()).thenReturn(status);
        // register mocks
        runtimes.descriptors.put(WORKSPACE_ID, descriptor);

        runtimes.removeMachine(machine.getId(), machine.getConfig().getName(), machine.getWorkspaceId());

        assertFalse(machines.isEmpty(), "should not remove machine");
    }

    @Test
    public void shouldNotRemoveMachineWhenMachineNameIsDifferent() throws Exception {
        // prepare runtime
        final WorkspaceRuntimeImpl runtime = mock(WorkspaceRuntimeImpl.class);
        final MachineImpl machine = createMachine(false);
        final ArrayList<MachineImpl> machines = new ArrayList<>();
        machines.add(machine);
        when(runtime.getMachines()).thenReturn(machines);
        // prepare descriptor
        when(descriptor.getRuntime()).thenReturn(runtime);
        when(descriptor.getRuntimeStatus()).thenReturn(RUNNING);
        // register mocks
        runtimes.descriptors.put(WORKSPACE_ID, descriptor);

        runtimes.removeMachine(machine.getId(), machine.getConfig().getName() + "2", machine.getWorkspaceId());

        assertFalse(machines.isEmpty(), "should not remove machine");
    }

    @Test
    public void shouldRemoveMachineIfStatusIsRunning() throws Exception {
        // prepare runtime
        final WorkspaceRuntimeImpl runtime = mock(WorkspaceRuntimeImpl.class);
        final MachineImpl machine = createMachine(false);
        final ArrayList<MachineImpl> machines = new ArrayList<>();
        machines.add(machine);
        when(runtime.getMachines()).thenReturn(machines);
        // prepare descriptor
        when(descriptor.getRuntime()).thenReturn(runtime);
        when(descriptor.getRuntimeStatus()).thenReturn(RUNNING);
        // register mocks
        runtimes.descriptors.put(WORKSPACE_ID, descriptor);

        runtimes.removeMachine(machine.getId(), machine.getConfig().getName(), machine.getWorkspaceId());

        assertTrue(machines.isEmpty(), "should remove machine");
    }

    @Test(dataProvider = "machineEventTypesExceptOfDestroyed")
    public void eventTypesExceptOfDestroyedShouldBeIgnoredByRemoveMachineSubscriber(MachineStatusEvent.EventType type)
            throws Exception {
        // prepare runtimes
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doNothing().when(runtimes).removeMachine(anyString(), anyString(), anyString());
        // prepare event
        final MachineImpl machine = createMachine(true);
        final MachineStatusEvent event = DtoFactory.newDto(MachineStatusEvent.class)
                                                   .withDev(false)
                                                   .withEventType(type)
                                                   .withMachineId(machine.getId());


        runtimes.new RemoveMachineEventSubscriber().onEvent(event);

        verify(runtimes, never()).removeMachine(anyString(), anyString(), anyString());
    }

    @Test
    public void removeMachineSubscriberShouldRemoveMachineIfItIsDevAndEventIsDestroyed() throws Exception {
        // prepare runtimes
        runtimes = spy(new WorkspaceRuntimes(machineManager, eventService));
        doNothing().when(runtimes).removeMachine(anyString(), anyString(), anyString());
        // prepare event
        final MachineImpl machine = createMachine(true);
        final MachineStatusEvent event = DtoFactory.newDto(MachineStatusEvent.class)
                                                   .withDev(false)
                                                   .withEventType(MachineStatusEvent.EventType.DESTROYED)
                                                   .withMachineId(machine.getId())
                                                   .withMachineName(machine.getConfig().getName())
                                                   .withWorkspaceId(machine.getWorkspaceId());

        runtimes.new RemoveMachineEventSubscriber().onEvent(event);

        verify(runtimes).removeMachine(machine.getId(), machine.getConfig().getName(), machine.getWorkspaceId());
    }

    @Test
    public void shouldReuseRunningMachineIfFailedToStart() throws Exception {
        // prepare workspace
        final WorkspaceImpl workspace = createWorkspace();
        // prepare machine
        final MachineImpl machine = createMachine(true);
        machine.setStatus(MachineStatus.RUNNING);
        when(machineManager.getMachines()).thenReturn(singletonList(machine));
        // force machine manager to throw conflict exception
        final RuntimeDescriptor descriptorMock = mock(RuntimeDescriptor.class);
        when(descriptorMock.getRuntimeStatus()).thenReturn(WorkspaceStatus.RUNNING);
        doThrow(new ConflictException("already exists")).when(machineManager).createMachineSync(eq(machine.getConfig()),
                                                                                                eq(machine.getWorkspaceId()),
                                                                                                eq(workspace.getConfig().getDefaultEnv()),
                                                                                                any(LineConsumer.class));

        final RuntimeDescriptor descriptor = runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        assertEquals(descriptor.getRuntime().getDevMachine(), machine);
        verify(machineManager).getMachines();
    }

    @DataProvider(name = "workspaceStatusesExceptOfRunning")
    private Object[][] workspaceStatusesExceptOfRunning() {
        final EnumSet<WorkspaceStatus> events = EnumSet.allOf(WorkspaceStatus.class);
        events.remove(WorkspaceStatus.RUNNING);
        return rotate(events.toArray(new Object[events.size()]));
    }

    @DataProvider(name = "machineEventTypesExceptOfRunning")
    private Object[][] machineEventTypesExceptOfRunning() {
        final EnumSet<MachineStatusEvent.EventType> events = EnumSet.allOf(MachineStatusEvent.EventType.class);
        events.remove(MachineStatusEvent.EventType.RUNNING);
        return rotate(events.toArray(new Object[events.size()]));
    }

    @DataProvider(name = "machineEventTypesExceptOfDestroyed")
    private Object[][] machineEventTypesExceptOfDestroyed() {
        final EnumSet<MachineStatusEvent.EventType> events = EnumSet.allOf(MachineStatusEvent.EventType.class);
        events.remove(MachineStatusEvent.EventType.DESTROYED);
        return rotate(events.toArray(new Object[events.size()]));
    }

    @DataProvider(name = "inconsistentMachines")
    private Object[][] inconsistentMachinesProvider() {
        return new Object[][] {
                {true, WorkspaceStatus.RUNNING},
                {true, WorkspaceStatus.STOPPING},
                {false, WorkspaceStatus.STARTING},
                {false, WorkspaceStatus.STOPPING}
        };
    }

    private static Object[][] rotate(Object[] array) {
        final Object[][] result = new Object[array.length][1];
        for (int i = 0; i < array.length; i++) {
            result[i] = new Object[] {array[i]};
        }
        return result;
    }

    private static MachineImpl createMachine(boolean isDev) {
        return createMachine(createConfig(isDev));
    }

    private static MachineImpl createMachine(MachineConfig cfg) {
        return MachineImpl.builder()
                          .setId(NameGenerator.generate("machine", 10))
                          .setWorkspaceId(WORKSPACE_ID)
                          .setEnvName(ENV_NAME)
                          .setConfig(new MachineConfigImpl(cfg))
                          .build();
    }

    private static MachineConfigImpl createConfig(boolean isDev) {
        return MachineConfigImpl.builder()
                                .setDev(isDev)
                                .setType("docker")
                                .setLimits(new LimitsImpl(1024))
                                .setSource(new MachineSourceImpl("git").setLocation("location"))
                                .setName("dev-machine")
                                .build();
    }

    private static WorkspaceImpl createWorkspace() {
        final MachineConfigImpl devCfg = createConfig(true);
        final MachineConfigImpl nonDevCfg = MachineConfigImpl.builder()
                                                             .fromConfig(devCfg)
                                                             .setName("non-dev")
                                                             .setDev(false)
                                                             .build();
        final EnvironmentImpl environment = new EnvironmentImpl(ENV_NAME,
                                                                new RecipeImpl(),
                                                                asList(nonDevCfg, devCfg));
        final WorkspaceConfigImpl wsConfig = WorkspaceConfigImpl.builder()
                                                                .setName("test workspace")
                                                                .setEnvironments(singletonList(environment))
                                                                .setDefaultEnv(environment.getName())
                                                                .build();
        return new WorkspaceImpl(WORKSPACE_ID, "user123", wsConfig);
    }
}
