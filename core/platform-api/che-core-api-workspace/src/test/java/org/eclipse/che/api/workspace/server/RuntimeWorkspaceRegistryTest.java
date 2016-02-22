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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.MachineManager;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeWorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
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
 * Tests for {@link RuntimeWorkspaceRegistry}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class RuntimeWorkspaceRegistryTest {

    private static final String WORKSPACE_ID = "workspace123";

    @Mock
    private MachineManager machineManagerMock;

    private RuntimeWorkspaceRegistry registry;

    @BeforeMethod
    public void setUp() throws Exception {
        when(machineManagerMock.createMachineSync(any(), any(), any()))
                .thenAnswer(invocation -> machineMock((MachineConfig)invocation.getArguments()[0]));
        registry = new RuntimeWorkspaceRegistry(machineManagerMock);
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Required non-null workspace")
    public void testStartWithNullWorkspace() throws Exception {
        registry.start(null, "environment");
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Couldn't start workspace '.*', environment name is null")
    public void testStartWithNullEnvName() throws Exception {
        registry.start(workspaceMock(), null);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Couldn't start workspace '', workspace doesn't have environment 'non-existing'")
    public void testStartWithNonExistingEnvironmentName() throws Exception {
        registry.start(workspaceMock(), "non-existing");
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Couldn't start workspace '.*' from environment '.*', " +
                                            "environment recipe has unsupported type 'non-docker'")
    public void testStartWithNonDockerEnvironmentRecipe() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();
        EnvironmentImpl environment = new EnvironmentImpl(workspaceMock.getConfig().getDefaultEnv(),
                                                          new RecipeImpl().withType("non-docker"),
                                                          null);
        when(workspaceMock.getConfig().getEnvironments()).thenReturn(singletonList(environment));

        registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Couldn't start workspace '.*' from environment '.*', environment doesn't contain dev-machine")
    public void testStartWithEnvironmentWhichDoesNotContainDevMachine() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();
        EnvironmentImpl environment = new EnvironmentImpl(workspaceMock.getConfig().getDefaultEnv(),
                                                          new RecipeImpl().withType("docker"), null);
        environment.setMachineConfigs(emptyList());
        when(workspaceMock.getConfig().getEnvironments()).thenReturn(singletonList(environment));

        registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
    }

    @Test
    public void workspaceShouldBeInStartingStatusUntilDevMachineIsNotStarted() throws Exception {
        final MachineManager machineManagerMock = mock(MachineManager.class);
        final RuntimeWorkspaceRegistry registry = new RuntimeWorkspaceRegistry(machineManagerMock);
        final UsersWorkspaceImpl workspaceMock = workspaceMock();

        // check if workspace in starting status before dev machine is started
        when(machineManagerMock.createMachineSync(any(), any(), any())).thenAnswer(invocationOnMock -> {
            final RuntimeWorkspaceImpl startingWorkspace = registry.get(workspaceMock.getId());
            final MachineConfig cfg = (MachineConfig)invocationOnMock.getArguments()[0];
            if (cfg.isDev()) {
                assertEquals(startingWorkspace.getStatus(), WorkspaceStatus.STARTING, "Workspace status is not 'STARTING'");
            }
            return machineMock((MachineConfig)invocationOnMock.getArguments()[0]);
        });

        registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
    }

    @Test
    public void workspaceShouldNotHaveRuntimeIfDevMachineCreationFailed() throws Exception {
        final MachineManager machineManagerMock = mock(MachineManager.class);
        final RuntimeWorkspaceRegistry registry = new RuntimeWorkspaceRegistry(machineManagerMock);
        final UsersWorkspaceImpl workspaceMock = workspaceMock();
        when(machineManagerMock.createMachineSync(any(), any(), any())).thenThrow(new MachineException("Creation error"));

        try {
            registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
        } catch (MachineException ex) {
            assertFalse(registry.hasRuntime(workspaceMock.getId()));
        }
    }

    @Test
    public void workspaceShouldContainAllMachinesAndBeInRunningStatusAfterSuccessfulStart() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();

        final RuntimeWorkspaceImpl runningWorkspace = registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());

        assertEquals(runningWorkspace.getStatus(), WorkspaceStatus.RUNNING);
        assertNotNull(runningWorkspace.getDevMachine());
        assertTrue(runningWorkspace.getMachines().size() == 2);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Could not start workspace '' because its status is 'RUNNING'")
    public void shouldNotStartWorkspaceIfItIsAlreadyRunning() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();

        registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
        registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Couldn't stop '.*' workspace because its status is 'STARTING'")
    public void shouldNotStopWorkspaceIfItIsStarting() throws Exception {
        final MachineManager machineManagerMock = mock(MachineManager.class);
        final RuntimeWorkspaceRegistry registry = new RuntimeWorkspaceRegistry(machineManagerMock);
        final UsersWorkspaceImpl workspaceMock = workspaceMock();

        when(machineManagerMock.createMachineSync(any(), any(), any())).thenAnswer(invocationOnMock -> {
            registry.stop(workspaceMock.getId());
            return machineMock((MachineConfig)invocationOnMock.getArguments()[0]);
        });

        registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
    }

    @Test
    public void shouldDestroyDevMachineIfWorkspaceWasStoppedWhileDevMachineWasStarting() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();
        registry = spy(new RuntimeWorkspaceRegistry(machineManagerMock));
        doReturn(false).when(registry).addRunningMachine(any());

        try {
            registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
        } catch (ServerException ex) {
            assertEquals(ex.getMessage(), "Workspace 'workspace123' had been stopped before its dev-machine was started");
        }

        verify(machineManagerMock, never()).destroy(any(), anyBoolean());
    }

    @Test
    public void shouldDestroyNonDevMachineIfWorkspaceWasStoppedWhileDevMachineWasStarting() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();

        doAnswer(invocation -> {
            final MachineConfig machineCfg = (MachineConfig)invocation.getArguments()[0];
            if (!machineCfg.isDev()) {
                registry.stop(workspaceMock.getId());
            }
            return machineMock((MachineConfig)invocation.getArguments()[0]);
        }).when(machineManagerMock).createMachineSync(any(), anyString(), anyString());

        try {
            registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
        } catch (ServerException ex) {
            assertEquals(ex.getMessage(), "Workspace '" + WORKSPACE_ID + "' had been stopped before all its machines were started");
        }
        verify(machineManagerMock, times(workspaceMock.getConfig()
                                                      .getEnvironments()
                                                      .get(0)
                                                      .getMachineConfigs().size())).destroy(any(), anyBoolean());
    }

    @Test
    public void shouldNotDestroyNonDevMachineIfRegistryWasStoppedWhileDevMachineWasStarting() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();

        doAnswer(invocation -> {
            final MachineConfig machineCfg = (MachineConfig)invocation.getArguments()[0];
            if (!machineCfg.isDev()) {
                registry.stopRegistry();
            }
            return machineMock((MachineConfig)invocation.getArguments()[0]);
        }).when(machineManagerMock).createMachineSync(any(), anyString(), anyString());

        try {
            registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
        } catch (ServerException ex) {
            assertEquals(ex.getMessage(), "Workspace '" + WORKSPACE_ID + "' had been stopped before all its machines were started");
        }
        verify(machineManagerMock, never()).destroy(any(), anyBoolean());
    }

    @Test
    public void shouldStopRunningWorkspace() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();

        registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());
        registry.stop(workspaceMock.getId());

        assertFalse(registry.hasRuntime(workspaceMock.getId()));
    }

    @Test
    public void testGetWorkspaceWithNullWorkspaceId() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();
        final RuntimeWorkspaceImpl running = registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());

        assertEquals(registry.get(workspaceMock.getId()), running);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetWorkspaceWhichIsNotRunning() throws Exception {
        registry.get("not-running");
    }

    @Test
    public void testGetWorkspacesByOwner() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();
        final RuntimeWorkspaceImpl runtimeWorkspace = registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());

        assertEquals(registry.getByOwner(workspaceMock.getOwner()), singletonList(runtimeWorkspace));
    }

    @Test
    public void testGetWorkspaceByOwnerWhenUserDoesNotHaveRunningWorkspaces() throws Exception {
        assertTrue(registry.getByOwner("user123").isEmpty());
    }

    @Test
    public void testRegistryStop() throws Exception {
        final UsersWorkspaceImpl workspaceMock = workspaceMock();
        registry.start(workspaceMock, workspaceMock.getConfig().getDefaultEnv());

        registry.stopRegistry();

        assertFalse(registry.hasRuntime(workspaceMock.getId()));
    }

    private static MachineImpl machineMock(MachineConfig cfg) {
        return MachineImpl.builder()
                          .setId(NameGenerator.generate("machine", 10))
                          .setWorkspaceId(WORKSPACE_ID)
                          .setConfig(new MachineConfigImpl(cfg))
                          .build();
    }

    private static UsersWorkspaceImpl workspaceMock() {
        // prepare default environment
        final EnvironmentImpl envState = mock(EnvironmentImpl.class, RETURNS_MOCKS);

        final Recipe recipe = mock(Recipe.class);
        when(recipe.getType()).thenReturn("docker");
        when(envState.getRecipe()).thenReturn(recipe);

        final MachineConfigImpl cfg = mock(MachineConfigImpl.class);
        when(cfg.isDev()).thenReturn(true);

        final MachineConfigImpl cf2 = mock(MachineConfigImpl.class);
        when(envState.getMachineConfigs()).thenReturn((asList(cfg, cf2)));

        // prepare workspace
        final RuntimeWorkspaceImpl workspace = mock(RuntimeWorkspaceImpl.class, RETURNS_MOCKS);
        final WorkspaceConfigImpl  wsConfig = mock(WorkspaceConfigImpl.class, RETURNS_MOCKS);
        when(workspace.getConfig()).thenReturn(wsConfig);
        when(wsConfig.getEnvironments()).thenReturn(Collections.singletonList(envState));
        when(workspace.getDevMachine()).thenReturn(mock(MachineImpl.class, RETURNS_MOCKS));
        when(workspace.getId()).thenReturn(WORKSPACE_ID);

        return workspace;
    }
}
