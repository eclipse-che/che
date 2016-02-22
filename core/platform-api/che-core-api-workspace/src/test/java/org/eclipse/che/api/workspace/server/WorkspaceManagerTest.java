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
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeWorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.commons.user.UserImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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

    private static final String USER_ID = "user123";

    @Mock
    private EventService                       eventService;
    @Mock
    private WorkspaceDao                       workspaceDao;
    @Mock
    private WorkspaceConfigValidator           workspaceConfigValidator;
    @Mock
    private MachineManager                     client;
    @Mock
    private WorkspaceHooks                     workspaceHooks;
    @Mock
    private MachineManager                     machineManager;
    @Mock
    private RuntimeWorkspaceRegistry           registry;
    @Captor
    private ArgumentCaptor<UsersWorkspaceImpl> workspaceCaptor;

    private WorkspaceManager workspaceManager;

    @BeforeMethod
    public void setUpManager() throws Exception {
        workspaceManager = spy(new WorkspaceManager(workspaceDao, registry, workspaceConfigValidator, eventService, machineManager));
        workspaceManager.setHooks(workspaceHooks);

        when(workspaceDao.create(any(UsersWorkspaceImpl.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(workspaceDao.update(any(UsersWorkspaceImpl.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        EnvironmentContext.setCurrent(new EnvironmentContext() {
            @Override
            public User getUser() {
                return new UserImpl("name", USER_ID, "token", new ArrayList<>(), false);
            }
        });
    }

    @Test
    public void shouldBeAbleToCreateWorkspace() throws Exception {
        final WorkspaceConfig cfg = createConfig();

        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(cfg, "user123", "account");

        assertNotNull(workspace);
        assertFalse(isNullOrEmpty(workspace.getId()));
        assertEquals(workspace.getOwner(), "user123");
        assertEquals(workspace.getConfig().getName(), cfg.getName());
        assertEquals(workspace.getStatus(), STOPPED);
        verify(workspaceHooks).beforeCreate(workspace, "account");
        verify(workspaceHooks).afterCreate(workspace, "account");
        verify(workspaceDao).create(workspace);
    }

    @Test
    public void shouldBeAbleToGetWorkspaceById() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(registry.get(any())).thenThrow(new NotFoundException(""));

        final UsersWorkspaceImpl result = workspaceManager.getWorkspace(workspace.getId());

        assertEquals(result, workspace);
    }

    @Test
    public void getWorkspaceByIdShouldReturnWorkspaceWithStatusEqualToItsRuntimeStatus() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeWorkspaceImpl runtime = createRuntime(workspace);
        runtime.setStatus(STARTING);
        when(registry.get(workspace.getId())).thenReturn(runtime);

        final UsersWorkspaceImpl result = workspaceManager.getWorkspace(workspace.getId());

        assertEquals(result.getStatus(), STARTING, "Workspace status must be taken from the runtime instance");
    }

    @Test
    public void shouldBeAbleToGetWorkspaceByName() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getConfig().getName(), workspace.getOwner())).thenReturn(workspace);
        when(registry.get(any())).thenThrow(new NotFoundException(""));

        final UsersWorkspaceImpl result = workspaceManager.getWorkspace(workspace.getConfig().getName(), workspace.getOwner());

        assertEquals(result, workspace);
    }

    @Test
    public void getWorkspaceByNameShouldReturnWorkspaceWithStatusEqualToItsRuntimeStatus() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getConfig().getName(), workspace.getOwner())).thenReturn(workspace);
        final RuntimeWorkspaceImpl runtime = createRuntime(workspace);
        runtime.setStatus(STARTING);
        when(registry.get(workspace.getId())).thenReturn(runtime);

        final UsersWorkspaceImpl result = workspaceManager.getWorkspace(workspace.getConfig().getName(), workspace.getOwner());

        assertEquals(result.getStatus(), STARTING, "Workspace status must be taken from the runtime instance");
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void getWorkspaceShouldThrowNotFoundExceptionWhenWorkspaceDoesNotExist() throws Exception {
        when(workspaceDao.get(any())).thenThrow(new NotFoundException("not found"));

        workspaceManager.getWorkspace("workspace123");
    }


    @Test
    public void shouldBeAbleToGetWorkspacesByOwner() throws Exception {
        // given
        final WorkspaceConfig config = createConfig();

        final UsersWorkspaceImpl workspace1 = workspaceManager.createWorkspace(config, "user123", null);
        workspace1.setStatus(STARTING);
        workspace1.setTemporary(true);

        final UsersWorkspaceImpl workspace2 = workspaceManager.createWorkspace(config, "user123", null);
        final RuntimeWorkspaceImpl workspace2Runtime = createRuntime(workspace2);
        workspace2Runtime.setStatus(RUNNING);
        when(registry.getByOwner("user123")).thenReturn(singletonList(workspace2Runtime));

        when(workspaceDao.getByOwner("user123")).thenReturn(asList(workspace1, workspace2));

        // when
        final List<UsersWorkspaceImpl> result = workspaceManager.getWorkspaces("user123");

        // then
        assertEquals(result.size(), 2);

        final UsersWorkspaceImpl res1 = result.get(0);
        assertEquals(res1.getStatus(), STOPPED, "Workspace status wasn't changed from STARTING to STOPPED");
        assertFalse(res1.isTemporary(), "Workspace must be permanent");
        assertNotNull(res1.getConfig()
                          .getEnvironments()
                          .get(0)
                          .getMachineConfigs()
                          .get(0));

        final UsersWorkspaceImpl res2 = result.get(1);
        assertEquals(res2.getStatus(), workspace2Runtime.getStatus(), "Workspace status wasn't changed to the runtime instance status");
        assertFalse(res2.isTemporary(), "Workspace must be permanent");
        assertNotNull(res2.getConfig()
                          .getEnvironments()
                          .get(0)
                          .getMachineConfigs()
                          .get(0));
    }

    @Test
    public void shouldBeAbleToUpdateWorkspace() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        when(registry.get(any())).thenThrow(new NotFoundException(""));
        final WorkspaceConfig update = createConfig();

        final UsersWorkspace updated = workspaceManager.updateWorkspace(workspace.getId(), update);

        verify(workspaceDao).update(any(UsersWorkspaceImpl.class));
        assertEquals(updated.getStatus(), STOPPED);
        assertFalse(updated.isTemporary());
        assertNotNull(updated.getConfig()
                             .getEnvironments()
                             .get(0)
                             .getMachineConfigs()
                             .get(0));
    }

    @Test
    public void workspaceUpdateShouldReturnWorkspaceWithStatusEqualToItsRuntimeStatus() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeWorkspaceImpl runtime = createRuntime(workspace);
        runtime.setStatus(STARTING);
        when(registry.get(workspace.getId())).thenReturn(runtime);
        final WorkspaceConfig update = createConfig();

        UsersWorkspace updated = workspaceManager.updateWorkspace(workspace.getId(), update);

        verify(workspaceDao).update(any(UsersWorkspaceImpl.class));
        assertEquals(updated.getStatus(), STARTING);
    }

    @Test
    public void shouldRemoveWorkspace() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);

        workspaceManager.removeWorkspace(workspace.getId());

        verify(workspaceDao).remove(workspace.getId());
        verify(workspaceHooks).afterRemove(workspace.getId());
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotRemoveWorkspaceIfItIsNotStopped() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(registry.hasRuntime(workspace.getId())).thenReturn(true);

        workspaceManager.removeWorkspace(workspace.getId());
    }

    @Test
    public void shouldBeAbleToGetRuntimeWorkspaceById() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account123");
        final RuntimeWorkspaceImpl runtime = createRuntime(workspace);
        when(registry.get(runtime.getId())).thenReturn(runtime);

        final RuntimeWorkspaceImpl result = workspaceManager.getRuntimeWorkspace(runtime.getId());

        assertNotNull(result.getConfig()
                            .getEnvironments()
                            .get(0)
                            .getMachineConfigs()
                            .get(0));
    }

    @Test
    public void shouldBeAbleToGetRuntimeWorkspaces() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account123");
        final RuntimeWorkspaceImpl runtime = createRuntime(workspace);
        when(registry.getByOwner("user123")).thenReturn(asList(runtime, runtime));

        final List<RuntimeWorkspaceImpl> result = workspaceManager.getRuntimeWorkspaces("user123");

        assertEquals(result, asList(runtime, runtime));
    }

    @Test
    public void shouldBeAbleToStartWorkspaceById() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        doReturn(createRuntime(workspace)).when(workspaceManager).performSyncStart(any(), anyString(), anyBoolean(), anyString());
        when(registry.get(any())).thenThrow(new NotFoundException(""));

        final UsersWorkspace result = workspaceManager.startWorkspaceById(workspace.getId(),
                                                                          workspace.getConfig().getDefaultEnv(),
                                                                          "account");

        assertEquals(result.getStatus(), STARTING);
        assertFalse(result.isTemporary());
        assertNotNull(result.getConfig()
                            .getEnvironments()
                            .get(0)
                            .getMachineConfigs()
                            .get(0));
        verify(workspaceManager).performAsyncStart(workspace, workspace.getConfig().getDefaultEnv(), false, "account");
    }

    @Test
    public void shouldBeAbleToStartWorkspaceByName() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getConfig().getName(), "user123")).thenReturn(workspace);
        doReturn(createRuntime(workspace)).when(workspaceManager).performSyncStart(any(), anyString(), anyBoolean(), anyString());
        when(registry.get(any())).thenThrow(new NotFoundException(""));

        final UsersWorkspace result = workspaceManager.startWorkspaceByName(workspace.getConfig().getName(),
                                                                            "user123",
                                                                            workspace.getConfig().getDefaultEnv(),
                                                                            "account");

        assertEquals(result.getStatus(), STARTING);
        assertFalse(result.isTemporary());
        assertNotNull(result.getConfig()
                            .getEnvironments()
                            .get(0)
                            .getMachineConfigs()
                            .get(0));
        verify(workspaceManager).performAsyncStart(workspace, workspace.getConfig().getDefaultEnv(), false, "account");
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not start workspace '.*' because its status is '.*'")
    public void shouldNotBeAbleToStartWorkspaceIfItIsRunning() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        final RuntimeWorkspaceImpl runtimeWorkspace = mock(RuntimeWorkspaceImpl.class);
        when(registry.get(workspace.getId())).thenReturn(runtimeWorkspace);
        when(runtimeWorkspace.getConfig()).thenReturn(workspace.getConfig());

        workspaceManager.startWorkspaceById(workspace.getId(), null, null);
    }

    @Test
    public void shouldStartWorkspaceWhenPerformingSyncStart() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(registry.start(any(), anyString(), anyBoolean())).thenReturn(createRuntime(workspace));

        workspaceManager.performSyncStart(workspace, workspace.getConfig().getDefaultEnv(), false, "account");

        verify(registry).start(workspace, workspace.getConfig().getDefaultEnv(), false);
        verify(workspaceHooks).beforeStart(workspace, workspace.getConfig().getDefaultEnv(), "account");
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "registry exception")
    public void syncStartShouldRethrowRegistryExceptions() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(registry.start(any(), anyString(), anyBoolean())).thenThrow(new ServerException("registry exception"));

        workspaceManager.performSyncStart(workspace, workspace.getConfig().getDefaultEnv(), false, "account");
    }

    @Test
    public void shouldBeAbleToStartTemporaryWorkspace() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        final UsersWorkspace workspace = workspaceManager.createWorkspace(config, "user123", "account");
        when(registry.start(workspaceCaptor.capture(), anyString(), anyBoolean())).thenReturn(createRuntime(workspace));

        final RuntimeWorkspaceImpl runtime = workspaceManager.startTemporaryWorkspace(config, "account");

        final UsersWorkspaceImpl captured = workspaceCaptor.getValue();
        assertTrue(captured.isTemporary());
        assertNotNull(captured.getConfig()
                              .getEnvironments()
                              .get(0)
                              .getMachineConfigs()
                              .get(0));
        verify(workspaceHooks).beforeCreate(captured, "account");
        verify(workspaceHooks).afterCreate(runtime, "account");
    }

    @Test
    public void shouldBeAbleToRecoverWorkspace() throws Exception {
        final UsersWorkspaceImpl workspace = workspaceManager.createWorkspace(createConfig(), "user123", "account");
        when(workspaceDao.get(workspace.getId())).thenReturn(workspace);
        doReturn(createRuntime(workspace)).when(workspaceManager).performSyncStart(any(), anyString(), anyBoolean(), anyString());
        when(registry.get(any())).thenThrow(new NotFoundException(""));

        final UsersWorkspace result = workspaceManager.recoverWorkspace(workspace.getId(),
                                                                        workspace.getConfig().getDefaultEnv(),
                                                                        "account");

        assertEquals(result.getStatus(), STARTING);
        assertFalse(result.isTemporary());
        assertNotNull(result.getConfig()
                            .getEnvironments()
                            .get(0)
                            .getMachineConfigs()
                            .get(0));
        verify(workspaceManager).performAsyncStart(workspace, workspace.getConfig().getDefaultEnv(), true, "account");
    }

    @Test
    public void shouldBeAbleToStopWorkspace() throws Exception {
        doNothing().when(workspaceManager).performAsyncStop(any());

        workspaceManager.stopWorkspace("workspace123");

        verify(workspaceManager).performAsyncStop(any());
    }

    @Test
    public void shouldBeAbleToGetSnapshots() throws Exception {
        when(machineManager.getSnapshots("user123", "workspace123")).thenReturn(singletonList(any()));

        assertEquals(workspaceManager.getSnapshot("workspace123").size(), 1);
    }

    private RuntimeWorkspaceImpl createRuntime(UsersWorkspace workspace) {
        return RuntimeWorkspaceImpl.builder()
                                   .fromWorkspace(workspace)
                                   .build();
    }

    private static WorkspaceConfigDto createConfig() {
        MachineConfigDto devMachine = newDto(MachineConfigDto.class).withDev(true)
                                                                    .withName("dev-machine")
                                                                    .withType("docker")
                                                                    .withSource(newDto(MachineSourceDto.class).withLocation("location")
                                                                                                              .withType("recipe"));
        EnvironmentDto devEnv = newDto(EnvironmentDto.class).withName("dev-env")
                                                            .withMachineConfigs(new ArrayList<>(singletonList(devMachine)))
                                                            .withRecipe(null);
        return newDto(WorkspaceConfigDto.class).withName("dev-workspace")
                                               .withEnvironments(new ArrayList<>(singletonList(devEnv)))
                                               .withDefaultEnv("dev-env");
    }
}
