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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.server.launcher.AgentLauncherFactory;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.environment.server.CheEnvironmentEngine;
import org.eclipse.che.api.environment.server.NoOpMachineInstance;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.exception.EnvironmentNotRunningException;
import org.eclipse.che.api.environment.server.exception.EnvironmentStartInterruptedException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes.RuntimeState;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceRuntimesTest {

    @Mock
    private EventService                 eventService;
    @Mock
    private CheEnvironmentEngine         envEngine;
    @Mock
    private AgentSorter                  agentSorter;
    @Mock
    private AgentLauncherFactory         launcherFactory;
    @Mock
    private AgentRegistry                agentRegistry;
    @Mock
    private WorkspaceSharedPool          sharedPool;
    @Mock
    private SnapshotDao                  snapshotDao;
    @Mock
    private Future<WorkspaceRuntimeImpl> runtimeFuture;
    @Mock
    private WorkspaceRuntimes.StartTask  startTask;

    @Captor
    private ArgumentCaptor<WorkspaceStatusEvent>     eventCaptor;
    @Captor
    private ArgumentCaptor<Callable<?>>              taskCaptor;
    @Captor
    private ArgumentCaptor<Collection<SnapshotImpl>> snapshotsCaptor;

    private WorkspaceRuntimes                   runtimes;
    private ConcurrentMap<String, RuntimeState> runtimeStates;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        runtimes = new WorkspaceRuntimes(eventService,
                                         envEngine,
                                         agentSorter,
                                         launcherFactory,
                                         agentRegistry,
                                         snapshotDao,
                                         sharedPool,
                                         runtimeStates = new ConcurrentHashMap<>());
    }

    @Test(dataProvider = "allStatuses")
    public void getsStatus(WorkspaceStatus status) throws Exception {
        setRuntime("workspace", status);

        assertEquals(runtimes.getStatus("workspace"), status);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id 'non_running' is not running")
    public void throwsNotFoundExceptionWhenGettingNonExistingRuntime() throws Exception {
        runtimes.getRuntime("non_running");
    }

    @Test
    public void returnsStoppedStatusWhenWorkspaceIsNotRunning() throws Exception {
        assertEquals(runtimes.getStatus("not_running"), WorkspaceStatus.STOPPED);
    }

    @Test
    public void getsRuntime() throws Exception {
        setRuntime("workspace", WorkspaceStatus.RUNNING, "env-name");
        List<Instance> machines = prepareMachines("workspace", "env-name");

        assertEquals(runtimes.getRuntime("workspace"), new WorkspaceRuntimeImpl("env-name", machines));
        verify(envEngine).getMachines("workspace");
    }

    @Test
    public void hasRuntime() {
        setRuntime("workspace", WorkspaceStatus.STARTING);

        assertTrue(runtimes.hasRuntime("workspace"));
    }

    @Test
    public void doesNotHaveRuntime() {
        assertFalse(runtimes.hasRuntime("not_running"));
    }

    @Test
    public void injectsRuntime() throws Exception {
        setRuntime("workspace", WorkspaceStatus.RUNNING, "env-name");
        List<Instance> machines = prepareMachines("workspace", "env-name");
        WorkspaceImpl workspace = WorkspaceImpl.builder()
                                               .setId("workspace")
                                               .build();

        runtimes.injectRuntime(workspace);

        assertEquals(workspace.getStatus(), WorkspaceStatus.RUNNING);
        assertEquals(workspace.getRuntime(), new WorkspaceRuntimeImpl("env-name", machines));
    }

    @Test
    public void injectsStoppedStatusWhenWorkspaceDoesNotHaveRuntime() throws Exception {
        WorkspaceImpl workspace = WorkspaceImpl.builder()
                                               .setId("workspace")
                                               .build();

        runtimes.injectRuntime(workspace);

        assertEquals(workspace.getStatus(), WorkspaceStatus.STOPPED);
        assertNull(workspace.getRuntime());
    }

    @Test
    public void injectsStatusAndEmptyMachinesWhenCanNotGetEnvironmentMachines() throws Exception {
        setRuntime("workspace", WorkspaceStatus.RUNNING, "env-name");
        setNoMachinesForWorkspace("workspace");
        WorkspaceImpl workspace = WorkspaceImpl.builder()
                                               .setId("workspace")
                                               .build();

        runtimes.injectRuntime(workspace);

        assertEquals(workspace.getStatus(), WorkspaceStatus.RUNNING);
        assertEquals(workspace.getRuntime().getActiveEnv(), "env-name");
        assertTrue(workspace.getRuntime().getMachines().isEmpty());
    }

    @Test
    public void startsWorkspace() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        List<Instance> machines = allowEnvironmentStart(workspace, "env-name");
        prepareMachines(workspace.getId(), machines);

        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = runtimes.startAsync(workspace, "env-name", false);
        captureAsyncTaskAndExecuteSynchronously();
        WorkspaceRuntimeImpl runtime = cmpFuture.get();

        assertEquals(runtimes.getStatus(workspace.getId()), WorkspaceStatus.RUNNING);
        assertEquals(runtime.getActiveEnv(), "env-name");
        assertEquals(runtime.getMachines().size(), machines.size());
        verifyEventsSequence(event("workspace",
                                   WorkspaceStatus.STOPPED,
                                   WorkspaceStatus.STARTING,
                                   EventType.STARTING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STARTING,
                                   WorkspaceStatus.RUNNING,
                                   EventType.RUNNING,
                                   null));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not start workspace 'test-workspace' because its status is 'RUNNING'")
    public void throwsConflictExceptionWhenWorkspaceIsRunning() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        setRuntime(workspace.getId(), WorkspaceStatus.RUNNING);

        runtimes.startAsync(workspace, "env-name", false);
    }

    @Test
    public void cancelsWorkspaceStartIfEnvironmentStartIsInterrupted() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        rejectEnvironmentStart(workspace, "env-name", new EnvironmentStartInterruptedException(workspace.getId(), "env-name"));

        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = runtimes.startAsync(workspace, "env-name", false);

        captureAndVerifyRuntimeStateAfterInterruption(workspace, cmpFuture);
    }

    @Test
    public void failsWorkspaceStartWhenEnvironmentStartIsFailed() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        rejectEnvironmentStart(workspace, "env-name", new EnvironmentException("no no no!"));

        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = runtimes.startAsync(workspace, "env-name", false);

        try {
            captureAsyncTaskAndExecuteSynchronously();
        } catch (EnvironmentException x) {
            assertEquals(x.getMessage(), "no no no!");
            verifyCompletionException(cmpFuture, EnvironmentException.class, "no no no!");
        }
        assertFalse(runtimes.hasRuntime(workspace.getId()));
        verifyEventsSequence(event("workspace",
                                   WorkspaceStatus.STOPPED,
                                   WorkspaceStatus.STARTING,
                                   EventType.STARTING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STARTING,
                                   WorkspaceStatus.STOPPED,
                                   EventType.ERROR,
                                   "Start of environment 'env-name' failed. Error: no no no!"));
    }

    @Test
    public void interruptsStartAfterEnvironmentIsStartedButRuntimeStatusIsNotRunning() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        // let's say status is changed to STOPPING by stop method,
        // but starting thread hasn't been interrupted yet
        allowEnvironmentStart(workspace, "env-name", () -> setRuntime("workspace", WorkspaceStatus.STOPPING));

        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = runtimes.startAsync(workspace, "env-name", false);

        captureAndVerifyRuntimeStateAfterInterruption(workspace, cmpFuture);
        verifyEventsSequence(event("workspace",
                                   WorkspaceStatus.STOPPED,
                                   WorkspaceStatus.STARTING,
                                   EventType.STARTING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STARTING,
                                   WorkspaceStatus.STOPPING,
                                   EventType.STOPPING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STOPPING,
                                   WorkspaceStatus.STOPPED,
                                   EventType.STOPPED,
                                   null));
        verify(envEngine).stop(workspace.getId());
    }

    @Test
    public void interruptsStartAfterEnvironmentIsStartedButThreadIsInterrupted() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        // the status is successfully updated from STARTING -> RUNNING but after
        // that thread is interrupted so #stop is waiting for starting thread to stop the environment
        allowEnvironmentStart(workspace, "env-name", () -> Thread.currentThread().interrupt());

        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = runtimes.startAsync(workspace, "env-name", false);

        captureAndVerifyRuntimeStateAfterInterruption(workspace, cmpFuture);
        verifyEventsSequence(event("workspace",
                                   WorkspaceStatus.STOPPED,
                                   WorkspaceStatus.STARTING,
                                   EventType.STARTING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STARTING,
                                   WorkspaceStatus.STOPPING,
                                   EventType.STOPPING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STOPPING,
                                   WorkspaceStatus.STOPPED,
                                   EventType.STOPPED,
                                   null));
        verify(envEngine).stop(workspace.getId());
    }

    @Test
    public void throwsStartInterruptedExceptionWhenStartIsInterruptedAndEnvironmentStopIsFailed() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        // let's say status is changed to STOPPING by stop method,
        // but starting thread hasn't been interrupted yet
        allowEnvironmentStart(workspace, "env-name", () -> Thread.currentThread().interrupt());
        rejectEnvironmentStop(workspace, new ServerException("no!"));

        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = runtimes.startAsync(workspace, "env-name", false);

        captureAndVerifyRuntimeStateAfterInterruption(workspace, cmpFuture);
        verifyEventsSequence(event("workspace",
                                   WorkspaceStatus.STOPPED,
                                   WorkspaceStatus.STARTING,
                                   EventType.STARTING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STARTING,
                                   WorkspaceStatus.STOPPING,
                                   EventType.STOPPING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STOPPING,
                                   WorkspaceStatus.STOPPED,
                                   EventType.ERROR,
                                   "no!"));
        verify(envEngine).stop(workspace.getId());
    }

    @Test
    public void releasesClientsWhoWaitForStartTaskResultAndTaskIsCompleted() throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();
        CountDownLatch releasedLatch = new CountDownLatch(5);
        // this thread + 5 awaiting threads
        CyclicBarrier callTaskBarrier = new CyclicBarrier(6);

        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        allowEnvironmentStart(workspace, "env-name");

        // the action
        runtimes.startAsync(workspace, "env-name", false);

        // register waiters
        for (int i = 0; i < 5; i++) {
            WorkspaceRuntimes.StartTask startTask = runtimeStates.get(workspace.getId()).startTask;
            pool.submit(() -> {
                // wait all the task to meet this barrier
                callTaskBarrier.await();

                // wait for start task to finish
                startTask.await();

                // good, release a part
                releasedLatch.countDown();
                return null;
            });
        }

        callTaskBarrier.await();
        captureAsyncTaskAndExecuteSynchronously();
        try {
            assertTrue(releasedLatch.await(2, TimeUnit.SECONDS), "start task wait clients are not released");
        } finally {
            shutdownAndWaitPool(pool);
        }
    }

    @Test
    public void stopsRunningWorkspace() throws Exception {
        setRuntime("workspace", WorkspaceStatus.RUNNING);

        runtimes.stop("workspace");

        verify(envEngine).stop("workspace");
        verifyEventsSequence(event("workspace",
                                   WorkspaceStatus.RUNNING,
                                   WorkspaceStatus.STOPPING,
                                   EventType.STOPPING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STOPPING,
                                   WorkspaceStatus.STOPPED,
                                   EventType.STOPPED,
                                   null));
        assertFalse(runtimeStates.containsKey("workspace"));
    }

    @Test
    public void stopsTheRunningWorkspaceWhileServerExceptionOccurs() throws Exception {
        setRuntime("workspace", WorkspaceStatus.RUNNING);
        doThrow(new ServerException("no!")).when(envEngine).stop("workspace");

        try {
            runtimes.stop("workspace");
        } catch (ServerException x) {
            assertEquals(x.getMessage(), "no!");
        }

        verify(envEngine).stop("workspace");
        assertFalse(runtimeStates.containsKey("workspace"));
        verifyEventsSequence(event("workspace",
                                   WorkspaceStatus.RUNNING,
                                   WorkspaceStatus.STOPPING,
                                   EventType.STOPPING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STOPPING,
                                   WorkspaceStatus.STOPPED,
                                   EventType.ERROR,
                                   "no!"));
    }

    @Test
    public void stopsTheRunningWorkspaceAndRethrowsTheErrorDifferentFromServerException() throws Exception {
        setRuntime("workspace", WorkspaceStatus.RUNNING);
        doThrow(new EnvironmentNotRunningException("no!")).when(envEngine).stop("workspace");

        try {
            runtimes.stop("workspace");
        } catch (ServerException x) {
            assertEquals(x.getMessage(), "no!");
            assertTrue(x.getCause() instanceof EnvironmentNotRunningException);
        }

        verify(envEngine).stop("workspace");
        assertFalse(runtimeStates.containsKey("workspace"));
        verifyEventsSequence(event("workspace",
                                   WorkspaceStatus.RUNNING,
                                   WorkspaceStatus.STOPPING,
                                   EventType.STOPPING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STOPPING,
                                   WorkspaceStatus.STOPPED,
                                   EventType.ERROR,
                                   "no!"));
    }

    @Test
    public void cancellationOfPendingStartTask() throws Throwable {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        when(sharedPool.submit(any())).thenReturn(Futures.immediateFuture(null));

        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = runtimes.startAsync(workspace, "env-name", false);

        // the real start is not being executed, fake sharedPool suppressed it
        // so the situation is the same to the one if the task is cancelled before
        // executor service started executing it
        runtimes.stop(workspace.getId());

        // start awaiting clients MUST receive interruption
        try {
            cmpFuture.get();
        } catch (ExecutionException x) {
            verifyCompletionException(cmpFuture,
                                      EnvironmentStartInterruptedException.class,
                                      "Start of environment 'env-name' in workspace 'workspace' is interrupted");
        }

        // if there is a state when the future is being cancelled,
        // and start task is marked as used, executor must not execute the
        // task but throw cancellation exception instead, once start task is
        // completed clients receive interrupted exception and cancellation doesn't bother them
        try {
            captureAsyncTaskAndExecuteSynchronously();
        } catch (CancellationException cancelled) {
            assertEquals(cancelled.getMessage(), "Start of the workspace 'workspace' was cancelled");
        }

        verifyEventsSequence(event("workspace",
                                   WorkspaceStatus.STOPPED,
                                   WorkspaceStatus.STARTING,
                                   EventType.STARTING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STARTING,
                                   WorkspaceStatus.STOPPING,
                                   EventType.STOPPING,
                                   null),
                             event("workspace",
                                   WorkspaceStatus.STOPPING,
                                   WorkspaceStatus.STOPPED,
                                   EventType.STOPPED,
                                   null));
    }

    @Test
    public void cancellationOfRunningStartTask() throws Exception {
        setRuntime("workspace",
                   WorkspaceStatus.STARTING,
                   "env-name",
                   runtimeFuture,
                   startTask);
        doThrow(new EnvironmentStartInterruptedException("workspace", "env-name")).when(startTask).await();

        runtimes.stop("workspace");

        verify(runtimeFuture).cancel(true);
        verify(startTask).await();
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id 'workspace' is not running")
    public void throwsNotFoundExceptionWhenStoppingNotRunningWorkspace() throws Exception {
        runtimes.stop("workspace");
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Couldn't stop the workspace 'workspace' because its status is '.*'.*",
          dataProvider = "notAllowedToStopStatuses")
    public void doesNotStopTheWorkspaceWhenStatusIsWrong(WorkspaceStatus status) throws Exception {
        setRuntime("workspace", status);

        runtimes.stop("workspace");
    }

    @Test
    public void cleanup() throws Exception {
        setRuntime("workspace", WorkspaceStatus.RUNNING, "env-name");

        runtimes.cleanup();

        assertFalse(runtimes.hasRuntime("workspace"));
        verify(envEngine).stop("workspace");
    }

    @Test
    public void startedRuntimeAndReturnedFromGetMethodAreTheSame() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        allowEnvironmentStart(workspace, "env-name");
        prepareMachines(workspace.getId(), "env-name");

        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = runtimes.startAsync(workspace, "env-name", false);
        captureAsyncTaskAndExecuteSynchronously();

        assertEquals(cmpFuture.get(), runtimes.getRuntime(workspace.getId()));
    }

    @Test
    public void shouldBeAbleToStartMachine() throws Exception {
        // when
        setRuntime("workspace", WorkspaceStatus.RUNNING, "env-name");
        MachineConfig config = newMachine("workspace", "env-name", "new", false).getConfig();
        Instance instance = mock(Instance.class);
        when(envEngine.startMachine(anyString(), any(MachineConfig.class), any())).thenReturn(instance);
        when(instance.getConfig()).thenReturn(config);

        // when
        Instance actual = runtimes.startMachine("workspace", config);

        // then
        assertEquals(actual, instance);
        verify(envEngine).startMachine(eq("workspace"), eq(config), any());
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id '.*' is not running")
    public void shouldNotStartMachineIfEnvironmentIsNotRunning() throws Exception {
        // when
        runtimes.startMachine("someWsID", mock(MachineConfig.class));

        // then
        verify(envEngine, never()).startMachine(anyString(), any(MachineConfig.class), any());
    }

    @Test
    public void shouldBeAbleToStopMachine() throws Exception {
        // when
        setRuntime("workspace", WorkspaceStatus.RUNNING);

        // when
        runtimes.stopMachine("workspace", "testMachineId");

        // then
        verify(envEngine).stopMachine("workspace", "testMachineId");
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
        Instance expected = newMachine("workspace", "env-name", "existing", false);
        when(envEngine.getMachine("workspace", expected.getId())).thenReturn(expected);

        // when
        Instance actualMachine = runtimes.getMachine("workspace", expected.getId());

        // then
        assertEquals(actualMachine, expected);
        verify(envEngine).getMachine("workspace", expected.getId());
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "test exception")
    public void shouldThrowExceptionIfGetMachineFromEnvEngineThrowsException() throws Exception {
        // given
        Instance expected = newMachine("workspace", "env-name", "existing", false);
        when(envEngine.getMachine("workspace", expected.getId()))
                .thenThrow(new NotFoundException("test exception"));

        // when
        runtimes.getMachine("workspace", expected.getId());

        // then
        verify(envEngine).getMachine("workspace", expected.getId());
    }

    @Test
    public void changesStatusFromRunningToSnapshotting() throws Exception {
        setRuntime("workspace", WorkspaceStatus.RUNNING);

        runtimes.snapshotAsync("workspace");

        assertEquals(runtimes.getStatus("workspace"), WorkspaceStatus.SNAPSHOTTING);
    }

    @Test
    public void changesStatusFromSnapshottingToRunning() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        setRuntime(workspace.getId(), WorkspaceStatus.RUNNING, "env-name");

        runtimes.snapshotAsync(workspace.getId());

        captureAsyncTaskAndExecuteSynchronously();
        assertEquals(runtimes.getStatus(workspace.getId()), WorkspaceStatus.RUNNING);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id 'non-existing' is not running")
    public void throwsNotFoundExceptionWhenBeginningSnapshottingForNonExistingWorkspace() throws Exception {
        runtimes.snapshot("non-existing");
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Workspace with id '.*' is not 'RUNNING', it's status is 'SNAPSHOTTING'")
    public void throwsConflictExceptionWhenBeginningSnapshottingForNotRunningWorkspace() throws Exception {
        setRuntime("workspace", WorkspaceStatus.RUNNING);

        runtimes.snapshotAsync("workspace");
        runtimes.snapshotAsync("workspace");
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "can't save")
    public void failsToCreateSnapshotWhenDevMachineSnapshottingFailed() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        setRuntime(workspace.getId(), WorkspaceStatus.RUNNING);
        prepareMachines(workspace.getId(), "env-name");
        when(envEngine.saveSnapshot(any(), any())).thenThrow(new ServerException("can't save"));

        try {
            runtimes.snapshot(workspace.getId());
        } catch (Exception x) {
            verifyEventsSequence(event(workspace.getId(),
                                       WorkspaceStatus.RUNNING,
                                       WorkspaceStatus.SNAPSHOTTING,
                                       EventType.SNAPSHOT_CREATING,
                                       null),
                                 event(workspace.getId(),
                                       WorkspaceStatus.SNAPSHOTTING,
                                       WorkspaceStatus.RUNNING,
                                       EventType.SNAPSHOT_CREATION_ERROR,
                                       "can't save"));
            throw x;
        }
    }

    @Test
    public void removesNewlyCreatedSnapshotsWhenFailedToSaveTheirsMetadata() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        setRuntime(workspace.getId(), WorkspaceStatus.RUNNING, "env-name");
        doThrow(new SnapshotException("test")).when(snapshotDao)
                                              .replaceSnapshots(any(), any(), any());
        SnapshotImpl snapshot = mock(SnapshotImpl.class);
        when(envEngine.saveSnapshot(any(), any())).thenReturn(snapshot);

        try {
            runtimes.snapshot(workspace.getId());
        } catch (ServerException x) {
            assertEquals(x.getMessage(), "test");
        }

        verify(snapshotDao).replaceSnapshots(any(), any(), snapshotsCaptor.capture());
        verify(envEngine, times(snapshotsCaptor.getValue().size())).removeSnapshot(snapshot);
        verifyEventsSequence(event(workspace.getId(),
                                   WorkspaceStatus.RUNNING,
                                   WorkspaceStatus.SNAPSHOTTING,
                                   EventType.SNAPSHOT_CREATING,
                                   null),
                             event(workspace.getId(),
                                   WorkspaceStatus.SNAPSHOTTING,
                                   WorkspaceStatus.RUNNING,
                                   EventType.SNAPSHOT_CREATION_ERROR,
                                   "test"));
    }

    @Test
    public void removesOldSnapshotsWhenNewSnapshotsMetadataSuccessfullySaved() throws Exception {
        WorkspaceImpl workspace = newWorkspace("workspace", "env-name");
        setRuntime(workspace.getId(), WorkspaceStatus.RUNNING);
        SnapshotImpl oldSnapshot = mock(SnapshotImpl.class);
        doReturn((singletonList(oldSnapshot))).when(snapshotDao)
                                              .replaceSnapshots(any(), any(), any());

        runtimes.snapshot(workspace.getId());

        verify(envEngine).removeSnapshot(oldSnapshot);
        verifyEventsSequence(event(workspace.getId(),
                                   WorkspaceStatus.RUNNING,
                                   WorkspaceStatus.SNAPSHOTTING,
                                   EventType.SNAPSHOT_CREATING,
                                   null),
                             event(workspace.getId(),
                                   WorkspaceStatus.SNAPSHOTTING,
                                   WorkspaceStatus.RUNNING,
                                   EventType.SNAPSHOT_CREATED,
                                   null));
    }

    @Test
    public void getsRuntimesIds() {
        setRuntime("workspace1", WorkspaceStatus.STARTING);
        setRuntime("workspace2", WorkspaceStatus.RUNNING);
        setRuntime("workspace3", WorkspaceStatus.STOPPING);
        setRuntime("workspace4", WorkspaceStatus.SNAPSHOTTING);

        assertEquals(runtimes.getRuntimesIds(), Sets.newHashSet("workspace1",
                                                                "workspace2",
                                                                "workspace3",
                                                                "workspace4"));
    }

    private void captureAsyncTaskAndExecuteSynchronously() throws Exception {
        verify(sharedPool).submit(taskCaptor.capture());
        taskCaptor.getValue().call();
    }


    private void captureAndVerifyRuntimeStateAfterInterruption(Workspace workspace,
                                                               CompletableFuture<WorkspaceRuntimeImpl> cmpFuture) throws Exception {
        try {
            captureAsyncTaskAndExecuteSynchronously();
        } catch (EnvironmentStartInterruptedException x) {
            String expectedMessage = "Start of environment 'env-name' in workspace 'workspace' is interrupted";
            assertEquals(x.getMessage(), expectedMessage);
            verifyCompletionException(cmpFuture, EnvironmentStartInterruptedException.class, expectedMessage);
        }
        assertFalse(runtimes.hasRuntime(workspace.getId()));
    }

    private void verifyCompletionException(Future<?> f, Class<? extends Exception> expectedEx, String expectedMessage) {
        assertTrue(f.isDone());
        try {
            f.get();
        } catch (ExecutionException execEx) {
            if (expectedEx.isInstance(execEx.getCause())) {
                assertEquals(execEx.getCause().getMessage(), expectedMessage);
            } else {
                fail(execEx.getMessage(), execEx);
            }
        } catch (InterruptedException interruptedEx) {
            fail(interruptedEx.getMessage(), interruptedEx);
        }
    }

    private void verifyEventsSequence(WorkspaceStatusEvent... expected) {
        Iterator<WorkspaceStatusEvent> it = captureEvents().iterator();
        for (WorkspaceStatusEvent expEvent : expected) {
            if (!it.hasNext()) {
                fail(format("It is expected to receive the status changed event '%s' -> '%s' " +
                            "but there are no more events published",
                            expEvent.getPrevStatus(),
                            expEvent.getStatus()));
            }
            WorkspaceStatusEvent cur = it.next();
            if (cur.getPrevStatus() != expEvent.getPrevStatus() || cur.getStatus() != expEvent.getStatus()) {
                fail(format("Expected to receive status change '%s' -> '%s', while received '%s' -> '%s'",
                            expEvent.getPrevStatus(),
                            expEvent.getStatus(),
                            cur.getPrevStatus(),
                            cur.getStatus()));
            }
            assertEquals(cur, expEvent);
        }
        if (it.hasNext()) {
            WorkspaceStatusEvent next = it.next();
            fail(format("No more events expected, but received '%s' -> '%s'",
                        next.getPrevStatus(),
                        next.getStatus()));
        }
    }

    private static WorkspaceStatusEvent event(String workspaceId,
                                              WorkspaceStatus prevStatus,
                                              WorkspaceStatus status,
                                              EventType eventType,
                                              String error) {
        return DtoFactory.newDto(WorkspaceStatusEvent.class)
                         .withWorkspaceId(workspaceId)
                         .withStatus(status)
                         .withPrevStatus(prevStatus)
                         .withEventType(eventType)
                         .withError(error);
    }

    private List<WorkspaceStatusEvent> captureEvents() {
        verify(eventService, atLeastOnce()).publish(eventCaptor.capture());
        return eventCaptor.getAllValues();
    }

    private void setRuntime(String workspaceId, WorkspaceStatus status) {
        runtimeStates.put(workspaceId, new RuntimeState(status, null, null, null));
    }

    private void setRuntime(String workspaceId, WorkspaceStatus status, String envName) {
        runtimeStates.put(workspaceId, new RuntimeState(status, envName, null, null));
    }

    private void setRuntime(String workspaceId,
                            WorkspaceStatus status,
                            String envName,
                            Future<WorkspaceRuntimeImpl> startFuture,
                            WorkspaceRuntimes.StartTask startTask) {
        runtimeStates.put(workspaceId, new RuntimeState(status, envName, startTask, startFuture));
    }

    private void setNoMachinesForWorkspace(String workspaceId) throws EnvironmentNotRunningException {
        when(envEngine.getMachines(workspaceId)).thenThrow(new EnvironmentNotRunningException(""));
    }

    private List<Instance> allowEnvironmentStart(Workspace workspace,
                                                 String envName,
                                                 TestAction beforeReturn) throws Exception {
        Environment environment = workspace.getConfig().getEnvironments().get(envName);
        ArrayList<Instance> machines = new ArrayList<>(environment.getMachines().size());
        for (Map.Entry<String, ? extends ExtendedMachine> entry : environment.getMachines().entrySet()) {
            machines.add(newMachine(workspace.getId(),
                                    envName,
                                    entry.getKey(),
                                    entry.getValue().getAgents().contains("org.eclipse.che.ws-agent")));
        }
        when(envEngine.start(eq(workspace.getId()),
                             eq(envName),
                             eq(workspace.getConfig().getEnvironments().get(envName)),
                             anyBoolean(),
                             any(),
                             any())).thenAnswer(invocation -> {
            if (beforeReturn != null) {
                beforeReturn.call();
            }
            return machines;
        });
        return machines;
    }

    private List<Instance> allowEnvironmentStart(Workspace workspace, String envName) throws Exception {
        return allowEnvironmentStart(workspace, envName, null);
    }

    private void rejectEnvironmentStart(Workspace workspace, String envName, Exception x) throws Exception {
        when(envEngine.start(eq(workspace.getId()),
                             eq(envName),
                             eq(workspace.getConfig().getEnvironments().get(envName)),
                             anyBoolean(),
                             any(),
                             any())).thenThrow(x);
    }

    private void rejectEnvironmentStop(Workspace workspace, Exception x) throws Exception {
        doThrow(x).when(envEngine).stop(workspace.getId());
    }

    private List<Instance> prepareMachines(String workspaceId, String envName) throws EnvironmentNotRunningException {
        List<Instance> machines = new ArrayList<>(3);
        machines.add(newMachine(workspaceId, envName, "machine1", true));
        machines.add(newMachine(workspaceId, envName, "machine2", false));
        machines.add(newMachine(workspaceId, envName, "machine3", false));
        prepareMachines(workspaceId, machines);
        return machines;
    }

    private void prepareMachines(String workspaceId, List<Instance> machines) throws EnvironmentNotRunningException {
        when(envEngine.getMachines(workspaceId)).thenReturn(machines);
    }

    private Instance newMachine(String workspaceId, String envName, String name, boolean isDev) {
        MachineImpl machine = MachineImpl.builder()
                                         .setConfig(MachineConfigImpl.builder()
                                                                     .setDev(isDev)
                                                                     .setName(name)
                                                                     .setType("docker")
                                                                     .setSource(new MachineSourceImpl("type"))
                                                                     .setLimits(new MachineLimitsImpl(1024))
                                                                     .build())
                                         .setWorkspaceId(workspaceId)
                                         .setEnvName(envName)
                                         .setOwner("owner")
                                         .setRuntime(new MachineRuntimeInfoImpl(Collections.emptyMap(),
                                                                                Collections.emptyMap(),
                                                                                Collections.emptyMap()))
                                         .setStatus(MachineStatus.RUNNING)
                                         .build();
        return new NoOpMachineInstance(machine);
    }

    private WorkspaceImpl newWorkspace(String workspaceId, String envName) {
        EnvironmentImpl environment = new EnvironmentImpl();
        Map<String, ExtendedMachineImpl> machines = environment.getMachines();
        machines.put("dev", new ExtendedMachineImpl(Arrays.asList("org.eclipse.che.terminal",
                                                                  "org.eclipse.che.ws-agent"),
                                                    Collections.emptyMap(),
                                                    Collections.emptyMap()));
        machines.put("db", new ExtendedMachineImpl(singletonList("org.eclipse.che.terminal"),
                                                   Collections.emptyMap(),
                                                   Collections.emptyMap()));
        return WorkspaceImpl.builder()
                            .setId(workspaceId)
                            .setTemporary(false)
                            .setConfig(WorkspaceConfigImpl.builder()
                                                          .setName("test-workspace")
                                                          .setDescription("this is test workspace")
                                                          .setDefaultEnv(envName)
                                                          .setEnvironments(ImmutableMap.of(envName,
                                                                                           environment))
                                                          .build())
                            .build();
    }

    private void shutdownAndWaitPool(ExecutorService pool) throws InterruptedException {
        pool.shutdownNow();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
            fail("Can't shutdown test pool");
        }
    }

    @FunctionalInterface
    private interface TestAction {
        void call() throws Exception;
    }

    @DataProvider
    private static Object[][] allStatuses() {
        WorkspaceStatus[] values = WorkspaceStatus.values();
        WorkspaceStatus[][] result = new WorkspaceStatus[values.length][1];
        for (int i = 0; i < values.length; i++) {
            result[i][0] = values[i];
        }
        return result;
    }

    @DataProvider
    private static Object[][] notAllowedToStopStatuses() {
        return new WorkspaceStatus[][] {
                {WorkspaceStatus.STOPPING},
                {WorkspaceStatus.SNAPSHOTTING}
        };
    }
}
