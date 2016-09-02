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
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.environment.server.MachineProcessManager;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes.RuntimeDescriptor;
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
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.workspace.server.WorkspaceManager.CREATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.server.WorkspaceManager.UPDATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.AUTO_RESTORE_FROM_SNAPSHOT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
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

    private static final String USER_ID   = "user123";
    private static final String NAMESPACE = "userNS";

    @Mock
    private EventService                  eventService;
    @Mock
    private WorkspaceDao                  workspaceDao;
    @Mock
    private WorkspaceValidator            workspaceConfigValidator;
    @Mock
    private MachineProcessManager         client;
    @Mock
    private WorkspaceHooks                workspaceHooks;
    @Mock
    private MachineProcessManager         machineProcessManager;
    @Mock
    private WorkspaceRuntimes             runtimes;
    @Mock
    private SnapshotDao                   snapshotDao;
    @Captor
    private ArgumentCaptor<WorkspaceImpl> workspaceCaptor;

    private WorkspaceManager workspaceManager;

    @BeforeMethod
    public void setUp() throws Exception {
        workspaceManager = spy(new WorkspaceManager(workspaceDao,
                                                    runtimes,
                                                    eventService,
                                                    false,
                                                    false,
                                                    snapshotDao));
        workspaceManager.setHooks(workspaceHooks);

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

        final WorkspaceImpl workspace = workspaceManager.createWorkspace(cfg, "user123", "account");

        assertNotNull(workspace);
        assertFalse(isNullOrEmpty(workspace.getId()));
        assertEquals(workspace.getNamespace(), "user123");
        assertEquals(workspace.getConfig(), cfg);
        assertFalse(workspace.isTemporary());
        assertEquals(workspace.getStatus(), STOPPED);
        assertNotNull(workspace.getAttributes().get(CREATED_ATTRIBUTE_NAME));
        verify(workspaceHooks).beforeCreate(workspace, "account");
        verify(workspaceHooks).afterCreate(workspace, "account");
        verify(workspaceDao).create(workspace);
    }


    @Test
    public void shouldBeAbleToGetWorkspaceById() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));

        final WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getId());

        assertEquals(result, workspace);
    }

    @Test
    public void getWorkspaceByIdShouldReturnWorkspaceWithStatusEqualToItsRuntimeStatus() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.get(workspace.getId())).thenReturn(descriptor);

        final WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getId());

        assertEquals(result.getStatus(), STARTING, "Workspace status must be taken from the runtime instance");
    }

    @Test
    public void shouldBeAbleToGetWorkspaceByName() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getConfig().getName(), workspace.getNamespace())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));

        final WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getConfig().getName(), workspace.getNamespace());

        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void getWorkspaceShouldThrowNotFoundExceptionWhenWorkspaceDoesNotExist() throws Exception {
        when(workspaceDao.get(any())).thenThrow(new NotFoundException("not found"));

        workspaceManager.getWorkspace("workspace123");
    }

    @Test
    public void shouldBeAbleToGetWorkspaceByKey() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), NAMESPACE, "account");
        when(workspaceDao.get(workspace.getConfig().getName(), workspace.getNamespace())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));

        final WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getNamespace() + ":" + workspace.getConfig().getName());
        assertEquals(result, workspace);
    }

    @Test
    public void shouldBeAbleToGetWorkspaceByKeyWithoutOwner() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), NAMESPACE, "account");
        when(workspaceDao.get(workspace.getConfig().getName(), workspace.getNamespace())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));

        final WorkspaceImpl result = workspaceManager.getWorkspace(":" + workspace.getConfig().getName());
        assertEquals(result, workspace);
    }

    @Test
    public void shouldBeAbleToGetWorkspacesAvailableForUser() throws Exception {
        // given
        final WorkspaceConfig config = createConfig();

        final WorkspaceImpl workspace1 = workspaceManager.createWorkspace(config, "user123", null);
        final WorkspaceImpl workspace2 = workspaceManager.createWorkspace(config, "user321", null);

        when(workspaceDao.getWorkspaces("user123")).thenReturn(asList(workspace1, workspace2));
        final RuntimeDescriptor descriptor = createDescriptor(workspace2, RUNNING);
        when(runtimes.get(workspace2.getId())).thenReturn(descriptor);

        // when
        final List<WorkspaceImpl> result = workspaceManager.getWorkspaces("user123");

        // then
        assertEquals(result.size(), 2);

        final WorkspaceImpl res1 = result.get(0);
        assertEquals(res1.getStatus(), STOPPED, "Workspace status wasn't changed from STARTING to STOPPED");
        assertFalse(res1.isTemporary(), "Workspace must be permanent");

        final WorkspaceImpl res2 = result.get(1);
        assertEquals(res2.getStatus(), RUNNING, "Workspace status wasn't changed to the runtime instance status");
        assertFalse(res2.isTemporary(), "Workspace must be permanent");
    }

    @Test
    public void shouldBeAbleToGetWorkspacesByNamespace() throws Exception {
        // given
        final WorkspaceConfig config = createConfig();

        final WorkspaceImpl workspace2 = workspaceManager.createWorkspace(config, "user321", null);

        when(workspaceDao.getByNamespace("user321")).thenReturn(singletonList(workspace2));
        final RuntimeDescriptor descriptor = createDescriptor(workspace2, RUNNING);
        when(runtimes.get(workspace2.getId())).thenReturn(descriptor);

        // when
        final List<WorkspaceImpl> result = workspaceManager.getByNamespace("user321");

        // then
        assertEquals(result.size(), 1);

        final WorkspaceImpl res1 = result.get(0);
        assertEquals(res1.getStatus(), RUNNING, "Workspace status wasn't changed to the runtime instance status");
        assertFalse(res1.isTemporary(), "Workspace must be permanent");
    }

    @Test
    public void getWorkspaceByNameShouldReturnWorkspaceWithStatusEqualToItsRuntimeStatus() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getConfig().getName(), workspace.getNamespace())).thenReturn(workspace);
        final RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.get(any())).thenReturn(descriptor);

        final WorkspaceImpl result = workspaceManager.getWorkspace(workspace.getConfig().getName(), workspace.getNamespace());

        assertEquals(result.getStatus(), STARTING, "Workspace status must be taken from the runtime instance");
    }

    @Test
    public void shouldBeAbleToUpdateWorkspace() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(new WorkspaceImpl(workspace));
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));
        workspace.setTemporary(true);
        workspace.getAttributes().put("new attribute", "attribute");

        final WorkspaceImpl updated = workspaceManager.updateWorkspace(workspace.getId(), workspace);

        verify(workspaceDao).update(any(WorkspaceImpl.class));
        assertEquals(updated.getStatus(), STOPPED);
        assertEquals(updated.getAttributes(), workspace.getAttributes());
        assertFalse(updated.isTemporary());
        assertEquals(workspace.getConfig(), updated.getConfig());
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void workspaceUpdateShouldReturnWorkspaceWithStatusEqualToItsRuntimeStatus() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.get(workspace.getId())).thenReturn(descriptor);

        final WorkspaceImpl updated = workspaceManager.updateWorkspace(workspace.getId(), workspace);

        assertEquals(updated.getStatus(), STARTING);
    }

    @Test
    public void shouldRemoveWorkspace() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);

        workspaceManager.removeWorkspace(workspace.getId());

        verify(workspaceDao).remove(workspace.getId());
        verify(workspaceHooks).afterRemove(workspace.getId());
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotRemoveWorkspaceIfItIsNotStopped() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(runtimes.hasRuntime(workspace.getId())).thenReturn(true);

        workspaceManager.removeWorkspace(workspace.getId());
    }

    @Test
    public void shouldBeAbleToStartWorkspaceById() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        "account",
                                        null);

        verify(runtimes, timeout(2000)).start(workspace, workspace.getConfig().getDefaultEnv(), false);
        verify(workspaceHooks, timeout(2000)).beforeStart(workspace, workspace.getConfig().getDefaultEnv(), "account");
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldRecoverWorkspaceWhenRecoverParameterIsNullAndAutoRestoreAttributeIsSetAndSnapshotExists() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        workspace.getAttributes().put(AUTO_RESTORE_FROM_SNAPSHOT, "true");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));
        SnapshotImpl.SnapshotBuilder snapshotBuilder = SnapshotImpl.builder()
                                                                   .generateId()
                                                                   .setEnvName("env")
                                                                   .setDev(true)
                                                                   .setMachineName("machine1")
                                                                   .setNamespace(workspace.getNamespace())
                                                                   .setWorkspaceId(workspace.getId())
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(workspace.getNamespace(), workspace.getId()))
                .thenReturn(asList(snapshot1, snapshot2));

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        "account",
                                        null);

        verify(runtimes, timeout(2000)).start(workspace,
                                              workspace.getConfig().getDefaultEnv(),
                                              true);
        verify(workspaceHooks, timeout(2000)).beforeStart(workspace,
                                                          workspace.getConfig().getDefaultEnv(),
                                                          "account");
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldRecoverWorkspaceWhenRecoverParameterIsTrueAndSnapshotExists() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));
        SnapshotImpl.SnapshotBuilder snapshotBuilder = SnapshotImpl.builder()
                                                                   .generateId()
                                                                   .setEnvName("env")
                                                                   .setDev(true)
                                                                   .setMachineName("machine1")
                                                                   .setNamespace(workspace.getNamespace())
                                                                   .setWorkspaceId(workspace.getId())
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(workspace.getNamespace(), workspace.getId()))
                .thenReturn(asList(snapshot1, snapshot2));

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        "account",
                                        true);

        verify(runtimes, timeout(2000)).start(workspace, workspace.getConfig().getDefaultEnv(), true);
        verify(workspaceHooks, timeout(2000)).beforeStart(workspace, workspace.getConfig().getDefaultEnv(), "account");
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldNotRecoverWorkspaceWhenRecoverParameterIsNullAndAutoRestoreAttributesIsSetButSnapshotDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        workspace.getAttributes().put(AUTO_RESTORE_FROM_SNAPSHOT, "true");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        "account",
                                        null);

        verify(runtimes, timeout(2000)).start(workspace, workspace.getConfig().getDefaultEnv(), false);
        verify(workspaceHooks, timeout(2000)).beforeStart(workspace, workspace.getConfig().getDefaultEnv(), "account");
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldNotRecoverWorkspaceWhenRecoverParameterIsTrueButSnapshotDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        "account",
                                        true);

        verify(runtimes, timeout(2000)).start(workspace, workspace.getConfig().getDefaultEnv(), false);
        verify(workspaceHooks, timeout(2000)).beforeStart(workspace, workspace.getConfig().getDefaultEnv(), "account");
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldNotRecoverWorkspaceWhenRecoverParameterIsFalseAndAutoRestoreAttributeIsSetAndSnapshotExists() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        workspace.getAttributes().put(AUTO_RESTORE_FROM_SNAPSHOT, "true");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));

        workspaceManager.startWorkspace(workspace.getId(),
                                        workspace.getConfig().getDefaultEnv(),
                                        "account",
                                        false);

        verify(runtimes, timeout(2000)).start(workspace, workspace.getConfig().getDefaultEnv(), false);
        verify(workspaceHooks, timeout(2000)).beforeStart(workspace, workspace.getConfig().getDefaultEnv(), "account");
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not start workspace '.*' because its status is '.*'")
    public void shouldNotBeAbleToStartWorkspaceIfItIsRunning() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.get(workspace.getId())).thenReturn(descriptor);

        workspaceManager.startWorkspace(workspace.getId(), null, null, null);
    }

    @Test
    public void workspaceStartShouldUseDefaultEnvIfNullEnvNameProvided() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(workspace.getId())).thenThrow(new NotFoundException(""));
        final RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.start(any(), anyString(), anyBoolean())).thenReturn(descriptor);

        workspaceManager.startWorkspace(workspace.getId(), null, "account", null);

        // timeout is needed because this invocation will run in separate thread asynchronously
        verify(runtimes, timeout(2000)).start(workspace, workspace.getConfig().getDefaultEnv(), false);
    }

    @Test
    public void performAsyncStartShouldUseProvidedEnvInsteadOfDefault() throws Exception {
        final WorkspaceConfigImpl config = createConfig();
        final EnvironmentImpl nonDefaultEnv = new EnvironmentImpl(null, null);
        config.getEnvironments().put("non-default-env", nonDefaultEnv);
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(config, "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(workspace.getId())).thenThrow(new NotFoundException(""));
        final RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.start(any(), anyString(), anyBoolean())).thenReturn(descriptor);
        workspaceManager.startWorkspace(workspace.getId(), "non-default-env", "account", null);

        // timeout is needed because this invocation will run in separate thread asynchronously
        verify(runtimes, timeout(2000)).start(workspace, "non-default-env", false);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace '.*' doesn't contain environment '.*'")
    public void startShouldThrowNotFoundExceptionWhenProvidedEnvDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(workspace.getId())).thenThrow(new NotFoundException(""));
        final RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.start(any(), anyString(), anyBoolean())).thenReturn(descriptor);

        workspaceManager.startWorkspace(workspace.getId(), "fake", "account", null);
    }

    @Test
    public void shouldBeAbleToStartTemporaryWorkspace() throws Exception {
        final WorkspaceConfigImpl config = createConfig();
        when(runtimes.start(any(), anyString(), anyBoolean())).thenReturn(mock(RuntimeDescriptor.class));
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));

        final WorkspaceImpl runtime = workspaceManager.startWorkspace(createConfig(), "user123", true, "account");

        verify(runtimes, timeout(2000)).start(workspaceCaptor.capture(), anyString(), anyBoolean());
        final WorkspaceImpl captured = workspaceCaptor.getValue();
        assertTrue(captured.isTemporary());
        verify(workspaceHooks).beforeCreate(captured, "account");
        verify(workspaceHooks).afterCreate(runtime, "account");
        verify(workspaceHooks).beforeStart(captured, config.getDefaultEnv(), "account");
        verify(workspaceManager).performAsyncStart(captured, captured.getConfig().getDefaultEnv(), false, "account");
    }

    @Test
    public void shouldBeAbleToStopWorkspace() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        final RuntimeDescriptor descriptor = createDescriptor(workspace, RUNNING);
        when(runtimes.get(any())).thenReturn(descriptor);
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);

        workspaceManager.stopWorkspace(workspace.getId());

        verify(runtimes, timeout(2000)).stop(workspace.getId());
        assertNotNull(workspace.getAttributes().get(UPDATED_ATTRIBUTE_NAME));
    }

    @Test
    public void shouldCreateWorkspaceSnapshotBeforeStoppingWorkspace() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        workspace.getAttributes().put(Constants.AUTO_CREATE_SNAPSHOT, "true");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeDescriptor descriptor = createDescriptor(workspace, RUNNING);
        when(runtimes.get(any())).thenReturn(descriptor);

        workspaceManager.stopWorkspace(workspace.getId());

        verify(workspaceManager, timeout(2000)).createSnapshotSync(anyObject(), anyString(), anyString());
        verify(runtimes, timeout(2000)).stop(any());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not stop the workspace " +
                                            "'.*' because its status is 'STARTING'.")
    public void shouldFailCreatingSnapshotWhenStoppingWorkspaceWhichIsNotRunning() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        workspace.getAttributes().put(Constants.AUTO_CREATE_SNAPSHOT, "true");
        final RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenReturn(descriptor);

        workspaceManager.stopWorkspace(workspace.getId());
    }

    @Test
    public void shouldStopWorkspaceEventIfSnapshotCreationFailed() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeDescriptor descriptor = createDescriptor(workspace, RUNNING);
        when(runtimes.get(any())).thenReturn(descriptor);

        workspaceManager.stopWorkspace(workspace.getId());

        verify(runtimes, timeout(2000)).stop(any());
    }

    @Test
    public void shouldRemoveTemporaryWorkspaceAfterStop() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        workspace.setTemporary(true);
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeDescriptor descriptor = createDescriptor(workspace, RUNNING);
        when(runtimes.get(any())).thenReturn(descriptor);

        workspaceManager.stopWorkspace(workspace.getId());

        verify(runtimes, timeout(2000)).stop(workspace.getId());
        verify(workspaceDao).remove(workspace.getId());
    }

    @Test
    public void shouldRemoveTemporaryWorkspaceAfterStartFailed() throws Exception {
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        workspace.setTemporary(true);
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));
        when(runtimes.start(any(), anyString(), anyBoolean())).thenThrow(new ConflictException(""));

        workspaceManager.startWorkspace(workspace.getId(), null, null, null);

        verify(workspaceDao, timeout(2000)).remove(workspace.getId());
    }

    @Test
    public void shouldBeAbleToGetSnapshots() throws Exception {
        final String wsId = "workspace123";
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), NAMESPACE, "account");
        when(workspaceDao.get(wsId)).thenReturn(workspace);
        when(snapshotDao.findSnapshots(NAMESPACE, wsId)).thenReturn(singletonList(any()));

        final List<SnapshotImpl> snapshots = workspaceManager.getSnapshot("workspace123");

        assertEquals(snapshots.size(), 1);
    }

    @Test
    public void shouldCreateWorkspaceSnapshotUsingDefaultValueForAutoRestore() throws Exception {
        workspaceManager = spy(new WorkspaceManager(workspaceDao,
                                                    runtimes,
                                                    eventService,
                                                    true,
                                                    false,
                                                    snapshotDao));
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeDescriptor descriptor = createDescriptor(workspace, RUNNING);
        when(runtimes.get(any())).thenReturn(descriptor);

        workspaceManager.stopWorkspace(workspace.getId());

        verify(workspaceManager, timeout(2000)).createSnapshotSync(workspace.getRuntime(), workspace.getNamespace(), workspace.getId());
        verify(runtimes, timeout(2000)).stop(any());
    }

    @Test
    public void shouldStartWorkspaceFromSnapshotUsingDefaultValueForAutoRestore() throws Exception {
        workspaceManager = spy(new WorkspaceManager(workspaceDao,
                                                    runtimes,
                                                    eventService,
                                                    false,
                                                    true,
                                                    snapshotDao));
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(runtimes.get(any())).thenThrow(new NotFoundException(""));
        SnapshotImpl.SnapshotBuilder snapshotBuilder = SnapshotImpl.builder()
                                                                   .generateId()
                                                                   .setEnvName("env")
                                                                   .setDev(true)
                                                                   .setMachineName("machine1")
                                                                   .setNamespace(workspace.getNamespace())
                                                                   .setWorkspaceId(workspace.getId())
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(workspace.getNamespace(), workspace.getId()))
                .thenReturn(asList(snapshot1, snapshot2));

        workspaceManager.startWorkspace(workspace.getId(), workspace.getConfig().getDefaultEnv(), "account", null);

        verify(runtimes, timeout(2000)).start(workspace, workspace.getConfig().getDefaultEnv(), true);
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
                                                                   .setNamespace(testNamespace)
                                                                   .setWorkspaceId(testWsId)
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(testNamespace, testWsId)).thenReturn(asList(snapshot1, snapshot2));

        // when
        workspaceManager.removeSnapshots(testWsId);

        // then
        InOrder runtimesInOrder = inOrder(runtimes);
        runtimesInOrder.verify(runtimes).removeSnapshot(snapshot1);
        runtimesInOrder.verify(runtimes).removeSnapshot(snapshot2);
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
                                                                   .setNamespace(testNamespace)
                                                                   .setWorkspaceId(testWsId)
                                                                   .setType("docker")
                                                                   .setMachineSource(new MachineSourceImpl("image"));
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.generateId()
                                                .setDev(false)
                                                .setMachineName("machine2")
                                                .build();
        when(snapshotDao.findSnapshots(testNamespace, testWsId)).thenReturn(asList(snapshot1, snapshot2));
        doThrow(new SnapshotException("test")).when(snapshotDao).removeSnapshot(snapshot1.getId());

        // when
        workspaceManager.removeSnapshots(testWsId);

        // then
        verify(runtimes).removeSnapshot(snapshot1);
        verify(runtimes).removeSnapshot(snapshot2);
        verify(snapshotDao).removeSnapshot(snapshot1.getId());
        verify(snapshotDao).removeSnapshot(snapshot2.getId());
    }

    @Test
    public void shouldBeAbleToStartMachineInRunningWs() throws Exception {
        // given
        String testWsId = "testWsId";
        String testActiveEnv = "testActiveEnv";
        WorkspaceImpl workspaceMock = mock(WorkspaceImpl.class);
        doReturn(workspaceMock).when(workspaceManager).getWorkspace(testWsId);
        WorkspaceRuntimeImpl wsRuntimeMock = mock(WorkspaceRuntimeImpl.class);
        when(workspaceMock.getStatus()).thenReturn(RUNNING);
        when(workspaceMock.getRuntime()).thenReturn(wsRuntimeMock);
        when(wsRuntimeMock.getActiveEnv()).thenReturn(testActiveEnv);
        MachineConfigImpl machineConfig = createMachine().getConfig();

        // when
        workspaceManager.startMachine(machineConfig, testWsId);

        // then
        verify(runtimes, timeout(2000)).startMachine(testWsId, machineConfig);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Workspace .* is not running, new machine can't be started")
    public void shouldThrowExceptionOnStartMachineInNonRunningWs() throws Exception {
        // given
        String testWsId = "testWsId";
        WorkspaceImpl workspaceMock = mock(WorkspaceImpl.class);
        doReturn(workspaceMock).when(workspaceManager).getWorkspace(testWsId);
        when(workspaceMock.getStatus()).thenReturn(STARTING);
        MachineConfigImpl machineConfig = createMachine().getConfig();

        // when
        workspaceManager.startMachine(machineConfig, testWsId);
    }

    @Test
    public void shouldBeAbleToCreateSnapshot() throws Exception {
        // then
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        RuntimeDescriptor descriptor = createDescriptor(workspace, RUNNING);
        when(runtimes.get(any())).thenReturn(descriptor);

        // when
        workspaceManager.createSnapshot(workspace.getId());

        // then
        verify(workspaceManager, timeout(1_000)).createSnapshotSync(any(WorkspaceRuntimeImpl.class),
                                                    eq(workspace.getNamespace()),
                                                    eq(workspace.getId()));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not .* the workspace '.*' because its status is '.*'.")
    public void shouldNotCreateSnapshotIfWorkspaceIsNotRunning() throws Exception {
        // then
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.get(any())).thenReturn(descriptor);

        // when
        workspaceManager.createSnapshot(workspace.getId());
    }

    @Test
    public void shouldBeAbleToStopMachine() throws Exception {
        // given
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        RuntimeDescriptor descriptor = createDescriptor(workspace, RUNNING);
        when(runtimes.get(any())).thenReturn(descriptor);
        MachineImpl machine = descriptor.getRuntime().getMachines().get(0);

        // when
        workspaceManager.stopMachine(workspace.getId(), machine.getId());

        // then
        verify(runtimes).stopMachine(workspace.getId(), machine.getId());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not .* the workspace '.*' because its status is '.*'.")
    public void shouldNotStopMachineIfWorkspaceIsNotRunning() throws Exception {
        // given
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.get(any())).thenReturn(descriptor);

        // when
        workspaceManager.stopMachine(workspace.getId(), "someId");
    }

    @Test
    public void shouldBeAbleToGetMachineInstanceIfWorkspaceIsRunning() throws Exception {
        // given
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        RuntimeDescriptor descriptor = createDescriptor(workspace, RUNNING);
        when(runtimes.get(any())).thenReturn(descriptor);
        MachineImpl machine = descriptor.getRuntime().getMachines().get(0);

        // when
        workspaceManager.getMachineInstance(workspace.getId(), machine.getId());

        // then
        verify(runtimes).getMachine(workspace.getId(), machine.getId());
    }

    @Test
    public void shouldBeAbleToGetMachineInstanceIfWorkspaceIsStarting() throws Exception {
        // given
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        RuntimeDescriptor descriptor = createDescriptor(workspace, STARTING);
        when(runtimes.get(any())).thenReturn(descriptor);
        MachineImpl machine = descriptor.getRuntime().getMachines().get(0);

        // when
        workspaceManager.getMachineInstance(workspace.getId(), machine.getId());

        // then
        verify(runtimes).getMachine(workspace.getId(), machine.getId());
    }

    private RuntimeDescriptor createDescriptor(WorkspaceImpl workspace, WorkspaceStatus status) {
        EnvironmentImpl environment = workspace.getConfig().getEnvironments().get(workspace.getConfig().getDefaultEnv());
        assertNotNull(environment);

        final WorkspaceRuntimeImpl runtime = new WorkspaceRuntimeImpl(workspace.getConfig().getDefaultEnv());
        MachineImpl machine = spy(createMachine());
        runtime.getMachines().add(machine);
        MachineImpl machine2 = spy(createMachine());
        runtime.getMachines().add(machine2);

        final RuntimeDescriptor descriptor = mock(RuntimeDescriptor.class);
        when(descriptor.getRuntimeStatus()).thenReturn(status);
        when(descriptor.getRuntime()).thenReturn(runtime);
        return descriptor;
    }

    private static WorkspaceConfigImpl createConfig() {
        EnvironmentImpl environment = new EnvironmentImpl(new EnvironmentRecipeImpl("type",
                                                                                    "contentType",
                                                                                    "content",
                                                                                    null),
                                                          singletonMap("dev-machine",
                                                                       new ExtendedMachineImpl(singletonList("ws-agent"),
                                                                                               null,
                                                                                               new HashMap<>(singletonMap("memoryLimitBytes", "10000")))));
        return WorkspaceConfigImpl.builder()
                                  .setName("dev-workspace")
                                  .setDefaultEnv("dev-env")
                                  .setEnvironments(singletonMap("dev-env", environment))
                                  .build();
    }

    private MachineImpl createMachine() {
        return MachineImpl.builder()
                          .setConfig(MachineConfigImpl.builder()
                                                      .setDev(false)
                                                      .setName("machineName")
                                                      .setSource(new MachineSourceImpl("type").setContent("content"))
                                                      .setLimits(new MachineLimitsImpl(1024))
                                                      .setType("docker")
                                                      .build())
                          .setId("id")
                          .setOwner("userName")
                          .setStatus(MachineStatus.RUNNING)
                          .setWorkspaceId("wsId")
                          .setEnvName("envName")
                          .setRuntime(new MachineRuntimeInfoImpl(new HashMap<>(),
                                                                 new HashMap<>(),
                                                                 new HashMap<>()))
                          .build();
    }
}
