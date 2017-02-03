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

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.api.workspace.server.WorkspaceManager.CREATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.server.WorkspaceManager.UPDATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.AUTO_CREATE_SNAPSHOT;
import static org.eclipse.che.api.workspace.shared.Constants.AUTO_RESTORE_FROM_SNAPSHOT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Covers main cases of {@link WorkspaceManager}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceManagerTest {

    private static final String USER_ID     = "user123";
    private static final String NAMESPACE   = "userNS";
    private static final String NAMESPACE_2 = "userNS2";

    @Mock
    private WorkspaceDao                  workspaceDao;
    @Mock
    private WorkspaceRuntimes             runtimes;
    @Mock
    private AccountManager                accountManager;
    @Mock
    private SnapshotDao                   snapshotDao;
    @Mock
    private WorkspaceSharedPool           sharedPool;
    @Mock
    private EventService                  eventService;
    @Captor
    private ArgumentCaptor<WorkspaceImpl> workspaceCaptor;
    @Captor
    private ArgumentCaptor<Runnable>      taskCaptor;

    private WorkspaceManager workspaceManager;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        workspaceManager = new WorkspaceManager(workspaceDao,
                                                runtimes,
                                                eventService,
                                                accountManager,
                                                false,
                                                false,
                                                snapshotDao,
                                                sharedPool);
        when(accountManager.getByName(NAMESPACE)).thenReturn(new AccountImpl("accountId", NAMESPACE, "test"));
        when(accountManager.getByName(NAMESPACE_2)).thenReturn(new AccountImpl("accountId2", NAMESPACE_2, "test"));
        when(workspaceDao.create(any(WorkspaceImpl.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(workspaceDao.update(any(WorkspaceImpl.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        EnvironmentContext.setCurrent(new EnvironmentContext() {
            @Override
            public Subject getSubject() {
                return new SubjectImpl(NAMESPACE, USER_ID, "token", false);
            }
        });
    }

    @Test
    public void shouldBeAbleToCreateWorkspace() throws Exception {
        final WorkspaceConfig cfg = createConfig();

        final WorkspaceImpl workspace = workspaceManager.createWorkspace(cfg, NAMESPACE);

        assertNotNull(workspace);
        assertFalse(isNullOrEmpty(workspace.getId()));
        assertEquals(workspace.getNamespace(), NAMESPACE);
        assertEquals(workspace.getConfig(), cfg);
        assertFalse(workspace.isTemporary());
        assertEquals(workspace.getStatus(), STOPPED);
        assertNotNull(workspace.getAttributes().get(CREATED_ATTRIBUTE_NAME));
        verify(workspaceDao).create(workspace);
    }

    @Test
    public void getsWorkspaceByIdWithoutRuntime() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();

        final WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getId());

        assertEquals(result, workspace);
    }

    @Test
    public void getsWorkspaceByIdWithRuntime() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();
        mockRuntime(workspace, STARTING);

        WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getId());

        assertEquals(result.getStatus(), STARTING, "Workspace status must be taken from the runtime instance");
    }

    @Test
    public void shouldBeAbleToGetWorkspaceByName() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();

        WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getConfig().getName(), workspace.getNamespace());

        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void getWorkspaceShouldThrowNotFoundExceptionWhenWorkspaceDoesNotExist() throws Exception {
        when(workspaceDao.get(any())).thenThrow(new NotFoundException("not found"));

        workspaceManager.getWorkspace("workspace123");
    }

    @Test
    public void shouldBeAbleToGetWorkspaceByKey() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();

        WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getNamespace() + ":" + workspace.getConfig().getName());

        assertEquals(result, workspace);
    }

    @Test
    public void shouldBeAbleToGetWorkspaceByKeyWithoutOwner() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();

        WorkspaceImpl result = workspaceManager.getWorkspace(":" + workspace.getConfig().getName());

        assertEquals(result, workspace);
    }

    @Test
    public void shouldBeAbleToGetWorkspacesAvailableForUser() throws Exception {
        // given
        final WorkspaceConfig config = createConfig();

        final WorkspaceImpl workspace1 = createAndMockWorkspace(config, NAMESPACE);
        final WorkspaceImpl workspace2 = createAndMockWorkspace(config, NAMESPACE_2);

        when(workspaceDao.getWorkspaces(NAMESPACE)).thenReturn(asList(workspace1, workspace2));
        mockRuntime(workspace1, STOPPED);
        mockRuntime(workspace2, RUNNING);

        // when
        final List<WorkspaceImpl> result = workspaceManager.getWorkspaces(NAMESPACE, true);

        // then
        assertEquals(result.size(), 2);

        final WorkspaceImpl res1 = result.get(0);
        assertEquals(res1.getStatus(), STOPPED);
        assertFalse(res1.isTemporary(), "Workspace must be permanent");

        final WorkspaceImpl res2 = result.get(1);
        assertEquals(res2.getStatus(), RUNNING, "Workspace status wasn't changed to the runtime instance status");
        assertFalse(res2.isTemporary(), "Workspace must be permanent");
    }

    @Test
    public void shouldBeAbleToGetWorkspacesByNamespace() throws Exception {
        // given
        final WorkspaceImpl workspace = createAndMockWorkspace();
        mockRuntime(workspace, RUNNING);

        // when
        final List<WorkspaceImpl> result = workspaceManager.getByNamespace(workspace.getNamespace(), true);

        // then
        assertEquals(result.size(), 1);

        final WorkspaceImpl res1 = result.get(0);
        assertEquals(res1.getStatus(), RUNNING, "Workspace status wasn't changed to the runtime instance status");
        assertFalse(res1.isTemporary(), "Workspace must be permanent");
    }

    @Test
    public void getWorkspaceByNameShouldReturnWorkspaceWithStatusEqualToItsRuntimeStatus() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        mockRuntime(workspace, STARTING);

        final WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getConfig().getName(), workspace.getNamespace());

        assertEquals(result.getStatus(), STARTING, "Workspace status must be taken from the runtime instance");
    }

    @Test
    public void shouldBeAbleToUpdateWorkspace() throws Exception {
        WorkspaceImpl workspace = new WorkspaceImpl(createAndMockWorkspace());
        workspace.setTemporary(true);
        workspace.getAttributes().put("new attribute", "attribute");
        when(workspaceDao.update(any())).thenAnswer(inv -> inv.getArguments()[0]);

        workspaceManager.updateWorkspace(workspace.getId(), workspace);

        verify(workspaceDao).update(workspace);
    }

    @Test
    public void workspaceUpdateShouldReturnWorkspaceWithStatusEqualToItsRuntimeStatus() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        mockRuntime(workspace, STARTING);

        final WorkspaceImpl updated = workspaceManager.updateWorkspace(workspace.getId(), workspace);

        assertEquals(updated.getStatus(), STARTING);
    }

    @Test
    public void shouldRemoveWorkspace() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();

        workspaceManager.removeWorkspace(workspace.getId());

        verify(workspaceDao).remove(workspace.getId());
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotRemoveWorkspaceIfItIsNotStopped() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        when(runtimes.hasRuntime(workspace.getId())).thenReturn(true);

        workspaceManager.removeWorkspace(workspace.getId());
    }

    @Test
    public void shouldBeAbleToStartWorkspaceById() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        mockStart(workspace);

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        null);

        verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), false);
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldRecoverWorkspaceWhenRecoverParameterIsNullAndAutoRestoreAttributeIsSetAndSnapshotExists() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.getAttributes().put(AUTO_RESTORE_FROM_SNAPSHOT, "true");
        mockStart(workspace);
        SnapshotImpl.SnapshotBuilder snapshotBuilder = SnapshotImpl.builder()
                                                                   .generateId()
                                                                   .setEnvName("env")
                                                                   .setDev(true)
                                                                   .setMachineName("machine1")
                                                                   .setWorkspaceId(workspace.getId())
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(workspace.getId()))
                .thenReturn(asList(snapshot1, snapshot2));

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        null);

        verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), true);
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldRecoverWorkspaceWhenRecoverParameterIsTrueAndSnapshotExists() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        mockStart(workspace);
        SnapshotImpl.SnapshotBuilder snapshotBuilder = SnapshotImpl.builder()
                                                                   .generateId()
                                                                   .setEnvName("env")
                                                                   .setDev(true)
                                                                   .setMachineName("machine1")
                                                                   .setWorkspaceId(workspace.getId())
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(workspace.getId()))
                .thenReturn(asList(snapshot1, snapshot2));

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        true);

        verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), true);
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldNotRecoverWorkspaceWhenRecoverParameterIsNullAndAutoRestoreAttributesIsSetButSnapshotDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.getAttributes().put(AUTO_RESTORE_FROM_SNAPSHOT, "true");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        mockStart(workspace);

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        null);

        verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), false);
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldNotRecoverWorkspaceWhenRecoverParameterIsTrueButSnapshotDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        mockStart(workspace);

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        true);

        verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), false);
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldNotRecoverWorkspaceWhenRecoverParameterIsFalseAndAutoRestoreAttributeIsSetAndSnapshotExists() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.getAttributes().put(AUTO_RESTORE_FROM_SNAPSHOT, "true");
        mockStart(workspace);

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        false);

        verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), false);
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void workspaceStartShouldUseDefaultEnvIfNullEnvNameProvided() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        mockStart(workspace);

        workspaceManager.startWorkspace(workspace.getId(), null, null);

        verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), false);
    }

    @Test
    public void usesProvidedEnvironmentInsteadOfDefault() throws Exception {
        WorkspaceConfigImpl config = createConfig();
        config.getEnvironments().put("non-default-env", new EnvironmentImpl(null, null));
        WorkspaceImpl workspace = createAndMockWorkspace(config, NAMESPACE);
        mockStart(workspace);

        workspaceManager.startWorkspace(workspace.getId(), "non-default-env", false);

        verify(runtimes).startAsync(workspaceCaptor.capture(), eq("non-default-env"), eq(false));
        assertEquals(workspaceCaptor.getValue().getConfig(), config);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace '.*' doesn't contain environment '.*'")
    public void startShouldThrowNotFoundExceptionWhenProvidedEnvDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();

        workspaceManager.startWorkspace(workspace.getId(), "fake", null);
    }

    @Test
    public void shouldBeAbleToStartTemporaryWorkspace() throws Exception {
        mockAnyWorkspaceStart();

        workspaceManager.startWorkspace(createConfig(), NAMESPACE, true);

        verify(runtimes).startAsync(workspaceCaptor.capture(), anyString(), anyBoolean());
        final WorkspaceImpl captured = workspaceCaptor.getValue();
        assertTrue(captured.isTemporary());
    }

    @Test
    public void shouldBeAbleToStopWorkspace() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace(createConfig(), NAMESPACE);
        mockRuntime(workspace, RUNNING);

        // when
        workspaceManager.stopWorkspace(workspace.getId());

        // then
        captureRunAsyncCallsAndRunSynchronously();

        verify(runtimes).stop(workspace.getId());

        verify(workspaceDao).update(workspaceCaptor.capture());
        WorkspaceImpl updated = workspaceCaptor.getValue();
        assertNotNull(updated.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void createsSnapshotBeforeStoppingWorkspace() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();
        mockRuntime(workspace, RUNNING);

        workspaceManager.stopWorkspace(workspace.getId(), true);

        captureRunAsyncCallsAndRunSynchronously();
        verify(runtimes).snapshot(workspace.getId());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not stop the workspace 'test-namespace:dev-workspace' because its " +
                                            "status is 'STOPPING'. Workspace must be either 'STARTING' or 'RUNNING'")
    public void failsToStopNotRunningWorkspace() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();
        mockRuntime(workspace, STOPPING);

        workspaceManager.stopWorkspace(workspace.getId());
    }

    @Test
    public void shouldStopWorkspaceEventIfSnapshotCreationFailed() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();
        mockRuntime(workspace, RUNNING);
        doThrow(new ServerException("Test")).when(runtimes).snapshot(workspace.getId());

        workspaceManager.stopWorkspace(workspace.getId(), true);

        captureRunAsyncCallsAndRunSynchronously();
        verify(runtimes).stop(any());
    }

    @Test
    public void shouldRemoveTemporaryWorkspaceAfterStop() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.setTemporary(true);
        mockRuntime(workspace, RUNNING);

        workspaceManager.stopWorkspace(workspace.getId());

        captureRunAsyncCallsAndRunSynchronously();
        verify(workspaceDao).remove(workspace.getId());
    }

    @Test
    public void shouldRemoveTemporaryWorkspaceAfterStartFailed() throws Exception {
        WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.setTemporary(true);
        mockRuntime(workspace, RUNNING);
        doThrow(new ServerException("")).when(runtimes).stop(workspace.getId());

        workspaceManager.stopWorkspace(workspace.getId());

        captureRunAsyncCallsAndRunSynchronously();
        verify(workspaceDao).remove(workspace.getId());
    }

    @Test
    public void shouldBeAbleToGetSnapshots() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        final SnapshotImpl wsSnapshot = SnapshotImpl.builder()
                                                    .setDev(true)
                                                    .setEnvName("envName")
                                                    .setId("snap1")
                                                    .setMachineName("machine1")
                                                    .setWorkspaceId(workspace.getId()).build();
        when(snapshotDao.findSnapshots(workspace.getId())).thenReturn(singletonList(wsSnapshot));

        final List<SnapshotImpl> snapshots = workspaceManager.getSnapshot(workspace.getId());

        assertEquals(snapshots.size(), 1);
        assertEquals(snapshots.get(0), wsSnapshot);
    }

    @Test
    public void shouldNotCreateSnapshotIfWorkspaceIsTemporaryAndAutoCreateSnapshotActivated() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.getAttributes().put(Constants.AUTO_CREATE_SNAPSHOT, "true");
        mockRuntime(workspace, RUNNING);

        SnapshotImpl oldSnapshot = mock(SnapshotImpl.class);
        when(snapshotDao.getSnapshot(eq(workspace.getId()),
                                     eq(workspace.getConfig().getDefaultEnv()),
                                     anyString()))
                .thenReturn(oldSnapshot);
        workspace.setTemporary(true);

        workspaceManager.stopWorkspace(workspace.getId());

        captureRunAsyncCallsAndRunSynchronously();
        verify(runtimes, never()).snapshot(workspace.getId());
        verify(runtimes).stop(workspace.getId());
    }

    @Test
    public void shouldNotCreateSnapshotIfWorkspaceIsTemporaryAndAutoCreateSnapshotDisactivated() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.getAttributes().put(Constants.AUTO_CREATE_SNAPSHOT, "false");
        mockRuntime(workspace, RUNNING);

        SnapshotImpl oldSnapshot = mock(SnapshotImpl.class);
        when(snapshotDao.getSnapshot(eq(workspace.getId()),
                                     eq(workspace.getConfig().getDefaultEnv()),
                                     anyString()))
                .thenReturn(oldSnapshot);
        workspace.setTemporary(true);

        workspaceManager.stopWorkspace(workspace.getId());

        captureRunAsyncCallsAndRunSynchronously();
        verify(runtimes, never()).snapshot(workspace.getId());
        verify(runtimes).stop(workspace.getId());
    }

    @Test
    public void shouldCreateWorkspaceSnapshotUsingDefaultValueForAutoRestore() throws Exception {
        // given
        workspaceManager = new WorkspaceManager(workspaceDao,
                                                runtimes,
                                                eventService,
                                                accountManager,
                                                true,
                                                false,
                                                snapshotDao,
                                                sharedPool);

        final WorkspaceImpl workspace = createAndMockWorkspace();
        mockRuntime(workspace, RUNNING);

        SnapshotImpl oldSnapshot = mock(SnapshotImpl.class);
        when(snapshotDao.getSnapshot(eq(workspace.getId()),
                                     eq(workspace.getConfig().getDefaultEnv()),
                                     anyString()))
                .thenReturn(oldSnapshot);

        // when
        workspaceManager.stopWorkspace(workspace.getId());

        // then
        captureRunAsyncCallsAndRunSynchronously();
        verify(runtimes).snapshot(workspace.getId());
        verify(runtimes).stop(workspace.getId());
    }

    @Test
    public void shouldStartWorkspaceFromSnapshotUsingDefaultValueForAutoRestore() throws Exception {
        workspaceManager = new WorkspaceManager(workspaceDao,
                                                runtimes,
                                                eventService,
                                                accountManager,
                                                false,
                                                true,
                                                snapshotDao,
                                                sharedPool);
        WorkspaceImpl workspace = createAndMockWorkspace();
        mockStart(workspace);

        SnapshotImpl.SnapshotBuilder snapshotBuilder = SnapshotImpl.builder()
                                                                   .generateId()
                                                                   .setEnvName("env")
                                                                   .setDev(true)
                                                                   .setMachineName("machine1")
                                                                   .setWorkspaceId(workspace.getId())
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(workspace.getId()))
                .thenReturn(asList(snapshot1, snapshot2));

        workspaceManager.startWorkspace(workspace.getId(), workspace.getConfig().getDefaultEnv(), null);

        verify(runtimes).startAsync(workspace, workspace.getConfig().getDefaultEnv(), true);
    }

    @Test
    public void shouldBeAbleToRemoveMachinesSnapshots() throws Exception {
        // given
        String testWsId = "testWsId";
        String testNamespace = "testNamespace";
        WorkspaceImpl workspaceMock = mock(WorkspaceImpl.class);
        when(workspaceDao.get(testWsId)).thenReturn(workspaceMock);
        when(workspaceMock.getNamespace()).thenReturn(testNamespace);
        SnapshotImpl.SnapshotBuilder snapshotBuilder = SnapshotImpl.builder()
                                                                   .generateId()
                                                                   .setEnvName("env")
                                                                   .setDev(true)
                                                                   .setMachineName("machine1")
                                                                   .setWorkspaceId(testWsId)
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(testWsId)).thenReturn(asList(snapshot1, snapshot2));

        // when
        workspaceManager.removeSnapshots(testWsId);

        // then
        captureExecuteCallsAndRunSynchronously();
        verify(runtimes).removeBinaries(asList(snapshot1, snapshot2));
        InOrder snapshotDaoInOrder = inOrder(snapshotDao);
        snapshotDaoInOrder.verify(snapshotDao).removeSnapshot(snapshot1.getId());
        snapshotDaoInOrder.verify(snapshotDao).removeSnapshot(snapshot2.getId());
    }

    @Test
    public void shouldRemoveMachinesSnapshotsEvenSomeRemovalFails() throws Exception {
        // given
        String testWsId = "testWsId";
        String testNamespace = "testNamespace";
        WorkspaceImpl workspaceMock = mock(WorkspaceImpl.class);
        when(workspaceDao.get(testWsId)).thenReturn(workspaceMock);
        when(workspaceMock.getNamespace()).thenReturn(testNamespace);
        SnapshotImpl.SnapshotBuilder snapshotBuilder = SnapshotImpl.builder()
                                                                   .generateId()
                                                                   .setEnvName("env")
                                                                   .setDev(true)
                                                                   .setMachineName("machine1")
                                                                   .setWorkspaceId(testWsId)
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(testWsId)).thenReturn(asList(snapshot1, snapshot2));
        doThrow(new SnapshotException("test")).when(snapshotDao).removeSnapshot(snapshot1.getId());

        // when
        workspaceManager.removeSnapshots(testWsId);

        // then
        captureExecuteCallsAndRunSynchronously();
        verify(runtimes).removeBinaries(singletonList(snapshot2));
        verify(snapshotDao).removeSnapshot(snapshot1.getId());
        verify(snapshotDao).removeSnapshot(snapshot2.getId());
    }

    @Test
    public void shouldBeAbleToStartMachineInRunningWs() throws Exception {
        // given
        WorkspaceImpl workspace = createAndMockWorkspace();
        WorkspaceRuntimeImpl runtime = mockRuntime(workspace, RUNNING);
        MachineConfigImpl machineConfig = createMachine(workspace.getId(),
                                                        runtime.getActiveEnv(),
                                                        false).getConfig();

        // when
        workspaceManager.startMachine(machineConfig, workspace.getId());

        // then
        captureExecuteCallsAndRunSynchronously();
        verify(runtimes).startMachine(workspace.getId(), machineConfig);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Workspace .* is not running, new machine can't be started")
    public void shouldThrowExceptionOnStartMachineInNonRunningWs() throws Exception {
        // given
        WorkspaceImpl workspace = createAndMockWorkspace();
        MachineConfigImpl machineConfig = createMachine(workspace.getId(), "env1", false).getConfig();

        // when
        workspaceManager.startMachine(machineConfig, workspace.getId());
    }

    @Test
    public void shouldBeAbleToCreateSnapshot() throws Exception {
        // then
        WorkspaceImpl workspace = createAndMockWorkspace();
        mockRuntime(workspace, RUNNING);
        SnapshotImpl oldSnapshot = mock(SnapshotImpl.class);
        when(snapshotDao.getSnapshot(eq(workspace.getId()),
                                     eq(workspace.getConfig().getDefaultEnv()),
                                     anyString()))
                .thenReturn(oldSnapshot);

        // when
        workspaceManager.createSnapshot(workspace.getId());

        // then
        verify(runtimes).snapshotAsync(workspace.getId());
    }

    @Test
    public void shouldBeAbleToStopMachine() throws Exception {
        // given
        final WorkspaceImpl workspace = createAndMockWorkspace();
        WorkspaceRuntimeImpl runtime = mockRuntime(workspace, RUNNING);
        MachineImpl machine = runtime.getMachines().get(0);

        // when
        workspaceManager.stopMachine(workspace.getId(), machine.getId());

        // then
        verify(runtimes).stopMachine(workspace.getId(), machine.getId());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not .* the workspace '.*' because its status is '.*'.")
    public void shouldNotStopMachineIfWorkspaceIsNotRunning() throws Exception {
        // given
        final WorkspaceImpl workspace = createAndMockWorkspace();

        // when
        workspaceManager.stopMachine(workspace.getId(), "someId");
    }

    @Test
    public void shouldBeAbleToGetMachineInstanceIfWorkspaceIsRunning() throws Exception {
        // given
        final WorkspaceImpl workspace = createAndMockWorkspace();
        WorkspaceRuntimeImpl runtime = mockRuntime(workspace, RUNNING);
        MachineImpl machine = runtime.getMachines().get(0);

        // when
        workspaceManager.getMachineInstance(workspace.getId(), machine.getId());

        // then
        verify(runtimes).getMachine(workspace.getId(), machine.getId());
    }

    @Test
    public void shouldBeAbleToGetMachineInstanceIfWorkspaceIsStarting() throws Exception {
        // given
        final WorkspaceImpl workspace = createAndMockWorkspace();
        WorkspaceRuntimeImpl runtime = mockRuntime(workspace, STARTING);
        MachineImpl machine = runtime.getMachines().get(0);

        // when
        workspaceManager.getMachineInstance(workspace.getId(), machine.getId());

        // then
        verify(runtimes).getMachine(workspace.getId(), machine.getId());
    }

    @Test
    public void passedCreateSnapshotParameterIsUsedInPreferenceToAttribute() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.getAttributes().put(AUTO_CREATE_SNAPSHOT, "true");
        mockRuntime(workspace, RUNNING);

        workspaceManager.stopWorkspace(workspace.getId(), false);

        captureRunAsyncCallsAndRunSynchronously();
        verify(runtimes, never()).snapshot(workspace.getId());
    }

    @Test
    public void passedNullCreateSnapshotParameterIsIgnored() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.getAttributes().put(AUTO_CREATE_SNAPSHOT, "true");
        mockRuntime(workspace, RUNNING);

        workspaceManager.stopWorkspace(workspace.getId(), null);

        captureRunAsyncCallsAndRunSynchronously();
        verify(runtimes).snapshot(workspace.getId());
    }

    @Test
    public void passedFalseCreateSnapshotParameterIsUsedInPreferenceToAttribute() throws Exception {
        final WorkspaceImpl workspace = createAndMockWorkspace();
        workspace.getAttributes().put(AUTO_CREATE_SNAPSHOT, "true");
        mockRuntime(workspace, RUNNING);

        workspaceManager.stopWorkspace(workspace.getId(), false);

        captureRunAsyncCallsAndRunSynchronously();
        verify(runtimes, never()).snapshot(workspace.getId());
    }

    @Test
    public void stopsRunningWorkspacesOnShutdown() throws Exception {
        when(runtimes.refuseWorkspacesStart()).thenReturn(true);

        WorkspaceImpl stopped = createAndMockWorkspace();
        mockRuntime(stopped, STOPPED);

        WorkspaceImpl starting = createAndMockWorkspace();
        mockRuntime(starting, STARTING);

        WorkspaceImpl running = createAndMockWorkspace();
        mockRuntime(running, RUNNING);

        when(runtimes.getRuntimesIds()).thenReturn(new HashSet<>(asList(running.getId(), starting.getId())));

        // action
        workspaceManager.shutdown();

        captureRunAsyncCallsAndRunSynchronously();
        verify(runtimes).stop(running.getId());
        verify(runtimes).stop(starting.getId());
        verify(runtimes, never()).stop(stopped.getId());
        verify(runtimes).shutdown();
        verify(sharedPool).shutdown();
    }

    @Test
    public void getsRunningWorkspacesIds() {
        ImmutableSet<String> ids = ImmutableSet.of("id1", "id2", "id3");
        when(runtimes.getRuntimesIds()).thenReturn(ids);

        assertEquals(workspaceManager.getRunningWorkspacesIds(), ids);
    }

    private void captureRunAsyncCallsAndRunSynchronously() {
        verify(sharedPool, atLeastOnce()).runAsync(taskCaptor.capture());
        for (Runnable runnable : taskCaptor.getAllValues()) {
            runnable.run();
        }
    }

    private void captureExecuteCallsAndRunSynchronously() {
        verify(sharedPool, atLeastOnce()).execute(taskCaptor.capture());
        for (Runnable runnable : taskCaptor.getAllValues()) {
            runnable.run();
        }
    }

    private WorkspaceRuntimeImpl mockRuntime(WorkspaceImpl workspace, WorkspaceStatus status) {
        when(runtimes.getStatus(workspace.getId())).thenReturn(status);
        MachineImpl machine1 = spy(createMachine(workspace.getId(), workspace.getConfig().getDefaultEnv(), true));
        MachineImpl machine2 = spy(createMachine(workspace.getId(), workspace.getConfig().getDefaultEnv(), false));
        Map<String, MachineImpl> machines = new HashMap<>();
        machines.put(machine1.getId(), machine1);
        machines.put(machine2.getId(), machine2);
        WorkspaceRuntimeImpl runtime = new WorkspaceRuntimeImpl(workspace.getConfig().getDefaultEnv(), machines.values());
        doAnswer(inv -> {
            workspace.setStatus(status);
            workspace.setRuntime(runtime);
            return null;
        }).when(runtimes).injectRuntime(workspace);
        when(runtimes.isAnyRunning()).thenReturn(true);
        return runtime;
    }

    private WorkspaceImpl createAndMockWorkspace() throws NotFoundException, ServerException {
        return createAndMockWorkspace(createConfig(), "test-namespace");
    }

    private WorkspaceImpl createAndMockWorkspace(WorkspaceConfig cfg, String namespace) throws NotFoundException, ServerException {
        WorkspaceImpl workspace = WorkspaceImpl.builder()
                                               .generateId()
                                               .setConfig(cfg)
                                               .setAccount(new AccountImpl("id", namespace, "type"))
                                               .setStatus(WorkspaceStatus.STOPPED)
                                               .build();
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(workspaceDao.get(workspace.getConfig().getName(), workspace.getNamespace())).thenReturn(workspace);
        when(workspaceDao.get(workspace.getConfig().getName(), NAMESPACE)).thenReturn(workspace);
        when(workspaceDao.getByNamespace(workspace.getNamespace())).thenReturn(singletonList(workspace));
        when(workspaceDao.getByNamespace(NAMESPACE)).thenReturn(singletonList(workspace));
        return workspace;
    }

    private void mockStart(Workspace workspace) throws Exception {
        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = CompletableFuture.completedFuture(mock(WorkspaceRuntimeImpl.class));
        when(runtimes.startAsync(eq(workspace), anyString(), anyBoolean())).thenReturn(cmpFuture);
    }

    private void mockAnyWorkspaceStart() throws Exception {
        CompletableFuture<WorkspaceRuntimeImpl> cmpFuture = CompletableFuture.completedFuture(mock(WorkspaceRuntimeImpl.class));
        when(runtimes.startAsync(anyObject(), anyString(), anyBoolean())).thenReturn(cmpFuture);
    }

    private static WorkspaceConfigImpl createConfig() {
        EnvironmentImpl environment = new EnvironmentImpl(new EnvironmentRecipeImpl("type",
                                                                                    "contentType",
                                                                                    "content",
                                                                                    null),
                                                          singletonMap("dev-machine",
                                                                       new ExtendedMachineImpl(singletonList("org.eclipse.che.ws-agent"),
                                                                                               null,
                                                                                               singletonMap("memoryLimitBytes", "10000"))));
        return WorkspaceConfigImpl.builder()
                                  .setName("dev-workspace")
                                  .setDefaultEnv("dev-env")
                                  .setEnvironments(singletonMap("dev-env", environment))
                                  .build();
    }

    private MachineImpl createMachine(String workspaceId, String envName, boolean isDev) {
        return MachineImpl.builder()
                          .setConfig(MachineConfigImpl.builder()
                                                      .setDev(isDev)
                                                      .setName("machineName" + UUID.randomUUID())
                                                      .setSource(new MachineSourceImpl("type").setContent("content"))
                                                      .setLimits(new MachineLimitsImpl(1024))
                                                      .setType("docker")
                                                      .build())
                          .setId("id" + UUID.randomUUID())
                          .setOwner("userName")
                          .setStatus(MachineStatus.RUNNING)
                          .setWorkspaceId(workspaceId)
                          .setEnvName(envName)
                          .setRuntime(new MachineRuntimeInfoImpl(new HashMap<>(),
                                                                 new HashMap<>(),
                                                                 new HashMap<>()))
                          .build();
    }
}
