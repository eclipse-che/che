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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes.RuntimeDescriptor;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.lang.NameGenerator;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
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
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceRuntimesTest {

    private static final String WORKSPACE_ID = "workspace123";
    private static final String MACHINE_ID = "machine123";

    @Mock
    private MachineManager machineManagerMock;

    @Mock
    private EventService eventService;
    @Mock
    private MachineImpl  machine;

    private WorkspaceRuntimes runtimes;

    @BeforeMethod
    public void setUp() throws Exception {
        when(machineManagerMock.createMachineSync(any(), any(), any()))
                .thenAnswer(invocation -> createMachine((MachineConfig)invocation.getArguments()[0]));
        runtimes = new WorkspaceRuntimes(machineManagerMock, eventService);
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
        when(machineManagerMock.createMachineSync(anyObject(), anyString(), anyString())).thenAnswer(invocationOnMock -> {
            final RuntimeDescriptor descriptor = runtimes.get(workspace.getId());
            final MachineConfig cfg = (MachineConfig)invocationOnMock.getArguments()[0];
            if (cfg.isDev()) {
                assertEquals(descriptor.getRuntimeStatus(), STARTING, "Workspace status is not 'STARTING'");
            }
            return createMachine((MachineConfig)invocationOnMock.getArguments()[0]);
        });

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv(), false);

        verify(machineManagerMock, times(2)).createMachineSync(anyObject(), anyString(), anyString());
    }

    @Test
    public void workspaceShouldNotHaveRuntimeIfDevMachineCreationFailed() throws Exception {
        final MachineManager machineManagerMock = mock(MachineManager.class);
        final WorkspaceRuntimes runtimes = new WorkspaceRuntimes(machineManagerMock, eventService);
        final WorkspaceImpl workspaceMock = createWorkspace();
        when(machineManagerMock.createMachineSync(any(), any(), any())).thenThrow(new MachineException("Creation error"));

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
          expectedExceptionsMessageRegExp = "Couldn't stop '.*' workspace because its status is 'STARTING'")
    public void shouldNotStopWorkspaceIfItIsStarting() throws Exception {
        final MachineManager machineManagerMock = mock(MachineManager.class);
        final WorkspaceRuntimes registry = new WorkspaceRuntimes(machineManagerMock, eventService);
        final WorkspaceImpl workspace = createWorkspace();

        when(machineManagerMock.createMachineSync(any(), any(), any())).thenAnswer(invocationOnMock -> {
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
        }).when(machineManagerMock).createMachineSync(any(), anyString(), anyString());

        try {
            runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        } catch (ConflictException ex) {
            assertEquals(ex.getMessage(), "Workspace '" + workspace.getId() + "' start interrupted. " +
                                          "Workspace was stopped before all its machines were started");
        }
        verify(machineManagerMock, times(2)).destroy(any(), anyBoolean());
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
        }).when(machineManagerMock).createMachineSync(any(), anyString(), anyString());


        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        final RuntimeDescriptor descriptor = runtimes.get(workspace.getId());
        assertEquals(descriptor.getRuntime().getMachines().size(), 1);
        assertEquals(descriptor.getRuntimeStatus(), RUNNING);
        verify(machineManagerMock, times(2)).createMachineSync(any(), any(), any());
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
        }).when(machineManagerMock).createMachineSync(any(), anyString(), anyString());

        try {
            runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        } catch (ServerException ex) {
            assertEquals(ex.getMessage(), "Could not perform operation because application server is stopping");
        }
        verify(machineManagerMock, never()).destroy(any(), anyBoolean());
    }

    @Test
    public void runtimeShouldBeInStoppingStatusIfWorkspacesDevMachineIsNotStopped() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        doAnswer(invocation -> {
            assertEquals(runtimes.get(workspace.getId()).getRuntimeStatus(), STOPPING);
            return null;
        }).when(machineManagerMock).destroy(any(), anyBoolean());

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
        }).when(machineManagerMock).destroy(any(), anyBoolean());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        runtimes.stop(workspace.getId());

        assertFalse(runtimes.hasRuntime(workspace.getId()));
        verify(machineManagerMock, times(2)).destroy(anyString(), anyBoolean());
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
    public void testCleanup() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        runtimes.cleanup();

        assertFalse(runtimes.hasRuntime(workspace.getId()));
    }

    @Test
    public void startingEventShouldBePublishedBeforeStart() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManagerMock, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());

        doAnswer(invocation -> {
            verify(runtimes).publishEvent(EventType.STARTING, workspace.getId(), null);
            return null;
        }).when(machineManagerMock).createMachineSync(any(), any(), any());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
    }

    @Test
    public void runningEventShouldBePublishedAfterDevMachineStarted() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManagerMock, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());

        doAnswer(invocation -> {
            final MachineConfig cfg = (MachineConfig)invocation.getArguments()[0];
            if (!cfg.isDev()) {
                verify(runtimes).publishEvent(EventType.RUNNING, workspace.getId(), null);
            }
            return createMachine(cfg);
        }).when(machineManagerMock).createMachineSync(any(), any(), any());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
    }

    @Test
    public void errorEventShouldBePublishedIfDevMachineFailedToStart() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManagerMock, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());
        doNothing().when(runtimes).cleanupStartResources(any());

        doAnswer(invocation -> {
            final MachineConfig cfg = (MachineConfig)invocation.getArguments()[0];
            if (cfg.isDev()) {
                throw new MachineException("Start error");
            }
            return createMachine(cfg);
        }).when(machineManagerMock).createMachineSync(any(), any(), any());

        try {
            runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        } catch (MachineException ex) {
            verify(runtimes).publishEvent(EventType.ERROR, workspace.getId(), ex.getLocalizedMessage());
        }
    }

    @Test
    public void stoppingEventShouldBePublishedBeforeStop() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManagerMock, eventService));
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
        }).when(machineManagerMock).destroy(anyString(), anyBoolean());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        runtimes.stop(workspace.getId());
    }

    @Test
    public void stoppedEventShouldBePublishedAfterDevMachineStopped() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManagerMock, eventService));
        doNothing().when(runtimes).publishEvent(any(), any(), any());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());
        runtimes.stop(workspace.getId());

        verify(runtimes).publishEvent(EventType.STOPPED, workspace.getId(), null);
    }

    @Test
    public void errorEventShouldBePublishedIfDevMachineFailedToStop() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes = spy(new WorkspaceRuntimes(machineManagerMock, eventService));
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
        }).when(machineManagerMock).destroy(anyString(), anyBoolean());

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        try {
            runtimes.stop(workspace.getId());
        } catch (MachineException ex) {
            verify(runtimes).publishEvent(EventType.ERROR, workspace.getId(), ex.getLocalizedMessage());
        }
    }

    @Test
    public void shouldAddNewMachineIntoRuntime() throws NotFoundException, ServerException, ConflictException {
        final WorkspaceImpl workspace = createWorkspace();
        final MachineImpl machine = createMachine(new MachineConfigImpl());

        when(machineManagerMock.getMachine(MACHINE_ID)).thenReturn(machine);

        runtimes.start(workspace, workspace.getConfig().getDefaultEnv());

        runtimes.addMachine(MACHINE_ID);

        assertTrue(runtimes.get(WORKSPACE_ID)
                           .getRuntime()
                           .getMachines()
                           .contains(machine));
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Could not perform operation because application server is stopping")
    public void shouldThrowServerExceptionIfAddMachineAfterPreDestroy() throws NotFoundException, ServerException, ConflictException {
        when(machineManagerMock.getMachine(MACHINE_ID)).thenReturn(machine);
        when(machine.getWorkspaceId()).thenReturn(WORKSPACE_ID);

        runtimes.cleanup();

        runtimes.addMachine(MACHINE_ID);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfWorkspaceIsNotRunningWhenAddMachine() throws NotFoundException, ServerException, ConflictException {
        when(machineManagerMock.getMachine(MACHINE_ID)).thenReturn(machine);
        when(machine.getWorkspaceId()).thenReturn(WORKSPACE_ID);

        runtimes.addMachine(MACHINE_ID);
    }

    private static MachineImpl createMachine(MachineConfig cfg) {
        return MachineImpl.builder()
                          .setId(NameGenerator.generate("machine", 10))
                          .setWorkspaceId(WORKSPACE_ID)
                          .setConfig(new MachineConfigImpl(cfg))
                          .build();
    }

    private static WorkspaceImpl createWorkspace() {
        final MachineConfigImpl devCfg = MachineConfigImpl.builder()
                                                          .setDev(true)
                                                          .setType("docker")
                                                          .setLimits(new LimitsImpl(1024))
                                                          .setSource(new MachineSourceImpl("git", "location"))
                                                          .setName("dev-machine")
                                                          .build();
        final MachineConfigImpl nonDevCfg = MachineConfigImpl.builder()
                                                             .fromConfig(devCfg)
                                                             .setName("non-dev")
                                                             .setDev(false)
                                                             .build();
        final EnvironmentImpl environment = new EnvironmentImpl("dev-env",
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
