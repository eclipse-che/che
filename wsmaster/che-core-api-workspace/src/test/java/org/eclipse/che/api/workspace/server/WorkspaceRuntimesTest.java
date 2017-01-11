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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.server.launcher.AgentLauncherFactory;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.environment.server.CheEnvironmentEngine;
import org.eclipse.che.api.environment.server.NoOpMachineInstance;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes.RuntimeDescriptor;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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

/**
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceRuntimesTest {

    private static final String WORKSPACE_ID = "workspace123";
    private static final String ENV_NAME     = "default-env";

    @Mock
    private EventService         eventService;
    @Mock
    private CheEnvironmentEngine envEngine;
    @Mock
    private AgentSorter          agentSorter;
    @Mock
    private AgentLauncherFactory launcherFactory;
    @Mock
    private AgentRegistry        agentRegistry;
    @Mock
    private WorkspaceSharedPool  sharedPool;
    @Mock
    private SnapshotDao          snapshotDao;

    @Captor
    private ArgumentCaptor<WorkspaceStatusEvent>     eventCaptor;
    @Captor
    private ArgumentCaptor<Callable>                 taskCaptor;
    @Captor
    private ArgumentCaptor<Collection<SnapshotImpl>> snapshotsCaptor;

    private WorkspaceRuntimes runtimes;

    @BeforeMethod
    public void setUp(Method method) throws Exception {
        runtimes = spy(new WorkspaceRuntimes(eventService,
                                             envEngine,
                                             agentSorter,
                                             launcherFactory,
                                             agentRegistry,
                                             snapshotDao,
                                             sharedPool));

        List<Instance> machines = asList(createMachine(true), createMachine(false));
        when(envEngine.start(anyString(),
                             anyString(),
                             any(Environment.class),
                             anyBoolean(),
                             any()))
                .thenReturn(machines);
        when(envEngine.getMachines(WORKSPACE_ID)).thenReturn(machines);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id '.*' is not running.")
    public void shouldThrowNotFoundExceptionIfWorkspaceRuntimeDoesNotExist() throws Exception {
        runtimes.get(WORKSPACE_ID);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Dev machine is not found in active environment of workspace 'workspace123'")
    public void shouldThrowExceptionOnGetRuntimesIfDevMachineIsMissingInTheEnvironment() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();

        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);
        when(envEngine.getMachines(workspace.getId()))
                .thenReturn(asList(createMachine(false), createMachine(false)));

        // when
        runtimes.get(workspace.getId());
    }

    @Test
    public void shouldFetchMachinesFromEnvEngineOnGetRuntime() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();
        Instance devMachine = createMachine(true);
        List<Instance> machines = asList(devMachine, createMachine(false));
        when(envEngine.start(anyString(),
                             anyString(),
                             any(Environment.class),
                             anyBoolean(),
                             any()))
                .thenReturn(machines);
        when(envEngine.getMachines(WORKSPACE_ID)).thenReturn(machines);

        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);

        // when
        RuntimeDescriptor runtimeDescriptor = runtimes.get(workspace.getId());

        // then
        RuntimeDescriptor expected = new RuntimeDescriptor(WorkspaceStatus.RUNNING,
                                                           new WorkspaceRuntimeImpl(workspace.getConfig()
                                                                                             .getDefaultEnv(),
                                                                                    devMachine.getRuntime()
                                                                                              .projectsRoot(),
                                                                                    machines,
                                                                                    devMachine));
        verify(envEngine, times(2)).getMachines(workspace.getId());
        assertEquals(runtimeDescriptor, expected);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Could not perform operation because application server is stopping")
    public void shouldNotStartTheWorkspaceIfPostConstructWasIsInvoked() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();
        runtimes.cleanup();

        // when
        runtimes.start(createWorkspace(), workspace.getConfig().getDefaultEnv(), false);
    }

    @Test
    public void workspaceShouldNotHaveRuntimeIfEnvStartFails() throws Exception {
        // given
        when(envEngine.start(anyString(),
                             anyString(),
                             any(Environment.class),
                             anyBoolean(),
                             any()))
                .thenThrow(new ServerException("Test env start error"));
        WorkspaceImpl workspaceMock = createWorkspace();

        try {
            // when
            runtimes.start(workspaceMock,
                           workspaceMock.getConfig().getDefaultEnv(),
                           false);
        } catch (Exception ex) {
            // then
            assertFalse(runtimes.hasRuntime(workspaceMock.getId()));
        }
    }

    @Test
    public void workspaceShouldContainAllMachinesAndBeInRunningStatusAfterSuccessfulStart() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();

        // when
        RuntimeDescriptor runningWorkspace = runtimes.start(workspace,
                                                            workspace.getConfig().getDefaultEnv(),
                                                            false);

        // then
        assertEquals(runningWorkspace.getRuntimeStatus(), RUNNING);
        assertNotNull(runningWorkspace.getRuntime().getDevMachine());
        assertEquals(runningWorkspace.getRuntime().getMachines().size(), 2);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not start workspace '.*' because its status is 'RUNNING'")
    public void shouldNotStartWorkspaceIfItIsAlreadyRunning() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();

        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);
        // when
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);
    }

    @Test
    public void testCleanup() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);

        runtimes.cleanup();

        // when, then
        assertFalse(runtimes.hasRuntime(workspace.getId()));
    }

    @Test
    public void shouldStopRunningWorkspace() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();

        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);
        // when
        runtimes.stop(workspace.getId());

        // then
        assertFalse(runtimes.hasRuntime(workspace.getId()));
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id 'workspace123' is not running.")
    public void shouldThrowNotFoundExceptionWhenStoppingWorkspaceWhichDoesNotHaveRuntime() throws Exception {
        runtimes.stop(WORKSPACE_ID);
    }

    @Test
    public void startedRuntimeShouldBeTheSameToRuntimeTakenFromGetMethod() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();

        // when
        RuntimeDescriptor descriptorFromStartMethod = runtimes.start(workspace,
                                                                     workspace.getConfig().getDefaultEnv(),
                                                                     false);
        RuntimeDescriptor descriptorFromGetMethod = runtimes.get(workspace.getId());

        // then
        assertEquals(descriptorFromStartMethod,
                     descriptorFromGetMethod);
    }

    @Test
    public void startingEventShouldBePublishedBeforeStart() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();

        // when
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);

        // then
        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                               .withWorkspaceId(workspace.getId())
                                               .withStatus(WorkspaceStatus.STARTING)
                                               .withEventType(EventType.STARTING)
                                               .withPrevStatus(WorkspaceStatus.STOPPED));
    }

    @Test
    public void runningEventShouldBePublishedAfterEnvStart() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();

        // when
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);

        // then
        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                               .withStatus(WorkspaceStatus.RUNNING)
                                               .withWorkspaceId(workspace.getId())
                                               .withEventType(EventType.RUNNING)
                                               .withPrevStatus(WorkspaceStatus.STARTING));
    }

    @Test
    public void errorEventShouldBePublishedIfDevMachineFailedToStart() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();
        when(envEngine.start(anyString(),
                             anyString(),
                             any(Environment.class),
                             anyBoolean(),
                             any()))
                .thenReturn(singletonList(createMachine(false)));

        try {
            // when
            runtimes.start(workspace,
                           workspace.getConfig().getDefaultEnv(),
                           false);

        } catch (Exception e) {
            // then
            verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                                   .withWorkspaceId(workspace.getId())
                                                   .withEventType(EventType.ERROR)
                                                   .withPrevStatus(WorkspaceStatus.STARTING));
        }
    }

    @Test
    public void stoppingEventShouldBePublishedBeforeStop() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);

        // when
        runtimes.stop(workspace.getId());

        // then
        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                               .withStatus(WorkspaceStatus.STOPPING)
                                               .withWorkspaceId(workspace.getId())
                                               .withEventType(EventType.STOPPING)
                                               .withPrevStatus(WorkspaceStatus.RUNNING));
    }

    @Test
    public void stoppedEventShouldBePublishedAfterEnvStop() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);

        // when
        runtimes.stop(workspace.getId());

        // then
        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                               .withStatus(WorkspaceStatus.STOPPED)
                                               .withWorkspaceId(workspace.getId())
                                               .withEventType(EventType.STOPPED)
                                               .withPrevStatus(WorkspaceStatus.STOPPING));
    }

    @Test
    public void errorEventShouldBePublishedIfEnvFailedToStop() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);

        try {
            // when
            runtimes.stop(workspace.getId());
        } catch (Exception e) {
            // then
            verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                                   .withWorkspaceId(workspace.getId())
                                                   .withEventType(EventType.ERROR)
                                                   .withPrevStatus(WorkspaceStatus.STOPPING)
                                                   .withError("Test error"));
        }
    }

    @Test
    public void shouldBeAbleToStartMachine() throws Exception {
        // when
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);
        MachineConfigImpl config = createConfig(false);
        Instance instance = mock(Instance.class);
        when(envEngine.startMachine(anyString(), any(MachineConfig.class), any())).thenReturn(instance);
        when(instance.getConfig()).thenReturn(config);

        // when
        Instance actual = runtimes.startMachine(workspace.getId(), config);

        // then
        assertEquals(actual, instance);
        verify(envEngine).startMachine(eq(workspace.getId()), eq(config), any());
    }

    @Test
    public void shouldAddTerminalAgentOnMachineStart() throws Exception {
        // when
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);
        MachineConfigImpl config = createConfig(false);
        Instance instance = mock(Instance.class);
        when(envEngine.startMachine(anyString(), any(MachineConfig.class), any())).thenReturn(instance);
        when(instance.getConfig()).thenReturn(config);

        // when
        Instance actual = runtimes.startMachine(workspace.getId(), config);

        // then
        assertEquals(actual, instance);
        verify(envEngine).startMachine(eq(workspace.getId()),
                                       eq(config),
                                       eq(singletonList("org.eclipse.che.terminal")));
        verify(runtimes).launchAgents(instance, singletonList("org.eclipse.che.terminal"));
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id '.*' is not running")
    public void shouldNotStartMachineIfEnvironmentIsNotRunning() throws Exception {
        // when
        MachineConfigImpl config = createConfig(false);

        // when
        runtimes.startMachine("someWsID", config);

        // then
        verify(envEngine, never()).startMachine(anyString(), any(MachineConfig.class), any());
    }

    @Test
    public void shouldBeAbleToStopMachine() throws Exception {
        // when
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);

        // when
        runtimes.stopMachine(workspace.getId(), "testMachineId");

        // then
        verify(envEngine).stopMachine(workspace.getId(), "testMachineId");
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id 'someWsID' is not running")
    public void shouldNotStopMachineIfEnvironmentIsNotRunning() throws Exception {
        // when
        runtimes.stopMachine("someWsID", "someMachineId");

        // then
        verify(envEngine, never()).stopMachine(anyString(), anyString());
    }

    @Test
    public void shouldBeAbleToGetMachine() throws Exception {
        // given
        Instance expected = createMachine(false);
        when(envEngine.getMachine(WORKSPACE_ID, expected.getId())).thenReturn(expected);

        // when
        Instance actualMachine = runtimes.getMachine(WORKSPACE_ID, expected.getId());

        // then
        assertEquals(actualMachine, expected);
        verify(envEngine).getMachine(WORKSPACE_ID, expected.getId());
    }

    @Test
    public void shouldBeAbleToGetStatusOfRunningWorkspace() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);

        // when
        WorkspaceStatus status = runtimes.getStatus(workspace.getId());

        // then
        assertEquals(status, RUNNING);
    }


    @Test
    public void shouldBeAbleToGetStatusOfStoppedWorkspace() throws Exception {
        // given
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);
        runtimes.stop(workspace.getId());

        // when
        WorkspaceStatus status = runtimes.getStatus(workspace.getId());

        // then
        assertEquals(status, STOPPED);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "test exception")
    public void shouldThrowExceptionIfGetMachineFromEnvEngineThrowsException() throws Exception {
        // given
        Instance expected = createMachine(false);
        when(envEngine.getMachine(WORKSPACE_ID, expected.getId()))
                .thenThrow(new NotFoundException("test exception"));

        // when
        runtimes.getMachine(WORKSPACE_ID, expected.getId());

        // then
        verify(envEngine).getMachine(WORKSPACE_ID, expected.getId());
    }

    @Test
    public void shouldBeAbleToGetAllWorkspacesWithExistingRuntime() throws Exception {
        // then
        Map<String, WorkspaceRuntimes.WorkspaceState> expectedWorkspaces = new HashMap<>();
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace,
                       workspace.getConfig().getDefaultEnv(),
                       false);
        expectedWorkspaces.put(workspace.getId(),
                               new WorkspaceRuntimes.WorkspaceState(RUNNING,
                                                                    workspace.getConfig().getDefaultEnv()));
        WorkspaceImpl workspace2 = spy(createWorkspace());
        when(workspace2.getId()).thenReturn("testWsId");
        when(envEngine.getMachines(workspace2.getId()))
                .thenReturn(Collections.singletonList(createMachine(true)));
        runtimes.start(workspace2,
                       workspace2.getConfig().getDefaultEnv(),
                       false);
        expectedWorkspaces.put(workspace2.getId(),
                               new WorkspaceRuntimes.WorkspaceState(RUNNING,
                                                                    workspace2.getConfig().getDefaultEnv()));

        // when
        Map<String, WorkspaceRuntimes.WorkspaceState> actualWorkspaces = runtimes.getWorkspaces();

        // then
        assertEquals(actualWorkspaces, expectedWorkspaces);
    }

    @Test
    public void changesStatusFromRunningToSnapshotting() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace, workspace.getConfig().getDefaultEnv(), false);

        runtimes.snapshotAsync(workspace.getId());

        assertEquals(runtimes.get(workspace.getId()).getRuntimeStatus(), WorkspaceStatus.SNAPSHOTTING);
    }

    @Test
    public void changesStatusFromSnapshottingToRunning() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace, workspace.getConfig().getDefaultEnv(), false);

        runtimes.snapshotAsync(workspace.getId());

        captureAsyncTaskAndExecuteSynchronously();
        assertEquals(runtimes.get(workspace.getId()).getRuntimeStatus(), WorkspaceStatus.RUNNING);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id 'non-existing' is not running")
    public void throwsNotFoundExceptionWhenBeginningSnapshottingForNonExistingWorkspace() throws Exception {
        runtimes.snapshot("non-existing");
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Workspace with id '.*' is not 'RUNNING', it's status is 'SNAPSHOTTING'")
    public void throwsConflictExceptionWhenBeginningSnapshottingForNotRunningWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace, workspace.getConfig().getDefaultEnv(), false);

        runtimes.snapshotAsync(workspace.getId());
        runtimes.snapshotAsync(workspace.getId());
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "can't save")
    public void failsToCreateSnapshotWhenDevMachineSnapshottingFailed() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace, workspace.getConfig().getDefaultEnv(), false);
        when(envEngine.saveSnapshot(any(), any())).thenThrow(new ServerException("can't save"));

        try {
            runtimes.snapshot(workspace.getId());
        } catch (Exception x) {
            verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                                   .withWorkspaceId(workspace.getId())
                                                   .withStatus(WorkspaceStatus.SNAPSHOTTING)
                                                   .withPrevStatus(WorkspaceStatus.RUNNING)
                                                   .withEventType(EventType.SNAPSHOT_CREATING));
            verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                                   .withWorkspaceId(workspace.getId())
                                                   .withError("can't save")
                                                   .withStatus(WorkspaceStatus.RUNNING)
                                                   .withPrevStatus(WorkspaceStatus.SNAPSHOTTING)
                                                   .withEventType(EventType.SNAPSHOT_CREATION_ERROR));
            throw x;
        }
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "test")
    public void removesNewlyCreatedSnapshotsWhenFailedToSaveTheirsMetadata() throws Exception {
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace, workspace.getConfig().getDefaultEnv(), false);
        doThrow(new SnapshotException("test")).when(snapshotDao)
                                              .replaceSnapshots(any(), any(), any());
        SnapshotImpl snapshot = mock(SnapshotImpl.class);
        when(envEngine.saveSnapshot(any(), any())).thenReturn(snapshot);

        try {
            runtimes.snapshot(workspace.getId());
        } catch (Exception x) {
            verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                                   .withStatus(WorkspaceStatus.SNAPSHOTTING)
                                                   .withEventType(EventType.SNAPSHOT_CREATING)
                                                   .withPrevStatus(WorkspaceStatus.RUNNING)
                                                   .withWorkspaceId(workspace.getId()));
            verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                                   .withStatus(WorkspaceStatus.RUNNING)
                                                   .withEventType(EventType.SNAPSHOT_CREATION_ERROR)
                                                   .withWorkspaceId(workspace.getId())
                                                   .withPrevStatus(WorkspaceStatus.SNAPSHOTTING)
                                                   .withError("test"));
            verify(snapshotDao).replaceSnapshots(any(),
                                                 any(),
                                                 snapshotsCaptor.capture());
            verify(envEngine, times(snapshotsCaptor.getValue().size())).removeSnapshot(snapshot);
            throw x;
        }
    }

    @Test
    public void removesOldSnapshotsWhenNewSnapshotsMetadataSuccessfullySaved() throws Exception {
        WorkspaceImpl workspace = createWorkspace();
        runtimes.start(workspace, workspace.getConfig().getDefaultEnv(), false);
        SnapshotImpl oldSnapshot = mock(SnapshotImpl.class);
        doReturn((singletonList(oldSnapshot))).when(snapshotDao)
                                              .replaceSnapshots(any(), any(), any());

        runtimes.snapshot(workspace.getId());

        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                               .withStatus(WorkspaceStatus.SNAPSHOTTING)
                                               .withEventType(EventType.SNAPSHOT_CREATING)
                                               .withPrevStatus(WorkspaceStatus.RUNNING)
                                               .withWorkspaceId(workspace.getId()));
        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                               .withStatus(WorkspaceStatus.RUNNING)
                                               .withEventType(EventType.SNAPSHOT_CREATED)
                                               .withPrevStatus(WorkspaceStatus.SNAPSHOTTING)
                                               .withWorkspaceId(workspace.getId()));
        verify(envEngine).removeSnapshot(oldSnapshot);
    }

    private static Instance createMachine(boolean isDev) {
        return createMachine(createConfig(isDev));
    }

    private static Instance createMachine(MachineConfig cfg) {
        return new TestMachineInstance(MachineImpl.builder()
                                                  .setId(NameGenerator.generate("machine", 10))
                                                  .setWorkspaceId(WORKSPACE_ID)
                                                  .setEnvName(ENV_NAME)
                                                  .setConfig(new MachineConfigImpl(cfg))
                                                  .build());
    }

    private static MachineConfigImpl createConfig(boolean isDev) {
        return MachineConfigImpl.builder()
                                .setDev(isDev)
                                .setType("docker")
                                .setLimits(new MachineLimitsImpl(1024))
                                .setSource(new MachineSourceImpl("git").setLocation("location"))
                                .setName(UUID.randomUUID().toString())
                                .build();
    }

    private static WorkspaceImpl createWorkspace() {
        EnvironmentImpl environment = new EnvironmentImpl(null,
                                                          null);
        WorkspaceConfigImpl wsConfig = WorkspaceConfigImpl.builder()
                                                          .setName("test workspace")
                                                          .setEnvironments(singletonMap(ENV_NAME, environment))
                                                          .setDefaultEnv(ENV_NAME)
                                                          .build();
        return new WorkspaceImpl(WORKSPACE_ID, new AccountImpl("accountId", "user123", "test"), wsConfig);
    }

    @SuppressWarnings("unchecked")
    private void captureAsyncTaskAndExecuteSynchronously() throws Exception {
        verify(sharedPool).submit(taskCaptor.capture());
        taskCaptor.getValue().call();
    }

    private static class TestMachineInstance extends NoOpMachineInstance {

        MachineRuntimeInfoImpl runtime;

        public TestMachineInstance(Machine machine) {
            super(machine);
            runtime = mock(MachineRuntimeInfoImpl.class);
        }

        @Override
        public MachineRuntimeInfoImpl getRuntime() {
            return runtime;
        }
    }
}
