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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.environment.server.MachineProcessManager;
import org.eclipse.che.api.environment.server.MachineServiceLinksInjector;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_ENVIRONMENT_STATUS_CHANNEL;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_WEBSOCKET_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.GET_ALL_USER_WORKSPACES;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_SNAPSHOT;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_IDE_URL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_REMOVE_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_SELF;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_START_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_STOP_WORKSPACE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link WorkspaceService}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class WorkspaceServiceTest {

    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER      = new ApiExceptionMapper();
    private static final String             NAMESPACE   = "user";
    private static final String             USER_ID     = "user123";
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER      = new EnvironmentFilter();

    @Mock
    private WorkspaceManager      wsManager;
    @Mock
    private MachineProcessManager machineProcessManager;
    @Mock
    private WorkspaceValidator    validator;

    private WorkspaceService      service;

    @BeforeMethod
    public void setup() {
        service = new WorkspaceService(wsManager,
                                       validator,
                                       new WorkspaceServiceLinksInjector(new MachineServiceLinksInjector()));
    }

    @Test
    public void shouldCreateWorkspace() throws Exception {
        final WorkspaceConfigDto configDto = createConfigDto();
        final WorkspaceImpl workspace = createWorkspace(configDto);
        when(wsManager.createWorkspace(any(), any(), any(), any())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(configDto)
                                         .when()
                                         .post(SECURE_PATH + "/workspace" +
                                               "?attribute=stackId:stack123" +
                                               "&attribute=factoryId:factory123" +
                                               "&attribute=custom:custom:value");

        assertEquals(response.getStatusCode(), 201);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class)), workspace);
        verify(validator).validateConfig(any());
        verify(validator).validateAttributes(any());
        verify(wsManager).createWorkspace(anyObject(),
                                          anyString(),
                                          eq(ImmutableMap.of("stackId", "stack123",
                                                             "factoryId", "factory123",
                                                             "custom", "custom:value")),
                                          eq(null));
    }

    @Test
    public void shouldStartTheWorkspaceAfterItIsCreatedWhenStartAfterCreateParamIsTrue() throws Exception {
        final WorkspaceConfigDto configDto = createConfigDto();
        final WorkspaceImpl workspace = createWorkspace(configDto);
        when(wsManager.createWorkspace(any(), any(), any(), any())).thenReturn(workspace);

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .body(configDto)
               .when()
               .post(SECURE_PATH + "/workspace" +
                     "?attribute=stackId:stack123" +
                     "&attribute=factoryId:factory123" +
                     "&attribute=custom:custom:value" +
                     "&start-after-create=true");

        verify(wsManager).startWorkspace(workspace.getId(), null, null, false);
        verify(wsManager).createWorkspace(anyObject(),
                                          anyString(),
                                          eq(ImmutableMap.of("stackId", "stack123",
                                                             "factoryId", "factory123",
                                                             "custom", "custom:value")),
                                          eq(null));
    }

    @Test
    public void createShouldReturn400WhenAttributesAreNotValid() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(createConfigDto())
                                         .when()
                                         .post(SECURE_PATH + "/workspace?attribute=stackId=stack123");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapError(response), "Attribute 'stackId=stack123' is not valid, " +
                                            "it should contain name and value separated " +
                                            "with colon. For example: attributeName:attributeValue");
    }

    @Test
    public void createShouldReturn400WhenConfigIsNotSent() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/workspace?attribute=stackId=stack123");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapError(response), "Workspace configuration required");
    }

    @Test
    public void shouldGetWorkspaceById() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/workspace/" + workspace.getId());

        assertEquals(response.getStatusCode(), 200);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class)), workspace);
    }

    @Test
    public void shouldGetWorkspaces() throws Exception {
        final WorkspaceImpl workspace1 = createWorkspace(createConfigDto());
        final WorkspaceImpl workspace2 = createWorkspace(createConfigDto(), STARTING);
        when(wsManager.getWorkspaces(USER_ID)).thenReturn(asList(workspace1, workspace2));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/workspace");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(unwrapDtoList(response, WorkspaceDto.class).stream()
                                                                .map(WorkspaceImpl::new)
                                                                .collect(toList()),
                     asList(workspace1, workspace2));
    }

    @Test
    public void shouldGetWorkspacesByStatus() throws Exception {
        final WorkspaceImpl workspace1 = createWorkspace(createConfigDto());
        final WorkspaceImpl workspace2 = createWorkspace(createConfigDto(), STARTING);
        when(wsManager.getWorkspaces(USER_ID)).thenReturn(asList(workspace1, workspace2));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/workspace?status=starting");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(unwrapDtoList(response, WorkspaceDto.class).stream()
                                                                .map(WorkspaceImpl::new)
                                                                .collect(toList()),
                     singletonList(workspace2));
    }

    @Test
    public void shouldUpdateTheWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.updateWorkspace(any(), any())).thenReturn(workspace);
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        final WorkspaceDto workspaceDto = DtoConverter.asDto(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(workspaceDto)
                                         .when()
                                         .put(SECURE_PATH + "/workspace/" + workspace.getId());

        assertEquals(response.getStatusCode(), 200);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class)), workspace);
        verify(validator).validateWorkspace(any());
    }

    @Test
    public void shouldDeleteWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());

        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        when(wsManager.getSnapshot(anyString())).thenReturn(ImmutableList.of(mock(SnapshotImpl.class)));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/" + workspace.getId());

        assertEquals(response.getStatusCode(), 204);
        verify(wsManager).removeSnapshots(workspace.getId());
        verify(wsManager).removeWorkspace(workspace.getId());
    }

    @Test
    public void shouldStartWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.startWorkspace(any(), any(), any(), any())).thenReturn(workspace);
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .post(SECURE_PATH + "/workspace/" + workspace.getId() + "/runtime" +
                                               "?environment=" + workspace.getConfig().getDefaultEnv());

        assertEquals(response.getStatusCode(), 200);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class)), workspace);
        verify(wsManager).startWorkspace(workspace.getId(), workspace.getConfig().getDefaultEnv(), null, null);
    }

    @Test
    public void shouldRestoreWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.startWorkspace(any(), any(), any(), any())).thenReturn(workspace);
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .post(SECURE_PATH + "/workspace/" + workspace.getId() + "/runtime" +
                                               "?environment=" + workspace.getConfig().getDefaultEnv() + "&restore=true");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class)), workspace);
        verify(wsManager).startWorkspace(workspace.getId(), workspace.getConfig().getDefaultEnv(), null, true);
    }

    @Test
    public void shouldNotRestoreWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.startWorkspace(any(), any(), any(), any())).thenReturn(workspace);
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .post(SECURE_PATH + "/workspace/" + workspace.getId() + "/runtime" +
                                               "?environment=" + workspace.getConfig().getDefaultEnv() + "&restore=false");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class)), workspace);
        verify(wsManager).startWorkspace(workspace.getId(), workspace.getConfig().getDefaultEnv(), null, false);
    }

    @Test
    public void shouldStartWorkspaceFromConfig() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.startWorkspace(anyObject(),
                                      anyString(),
                                      anyBoolean(),
                                      anyString())).thenReturn(workspace);
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        final WorkspaceDto workspaceDto = DtoConverter.asDto(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(workspaceDto.getConfig())
                                         .when()
                                         .post(SECURE_PATH + "/workspace/runtime");

        assertEquals(response.getStatusCode(), 200);
        verify(validator).validateConfig(any());
    }

    @Test
    public void shouldStopWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/" + workspace.getId() + "/runtime");

        assertEquals(response.getStatusCode(), 204);
        verify(wsManager).stopWorkspace(workspace.getId());
    }

    @Test
    public void shouldCreateSnapshot() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .post(SECURE_PATH + "/workspace/" + workspace.getId() + "/snapshot");

        assertEquals(response.getStatusCode(), 204);
        verify(wsManager).createSnapshot(workspace.getId());
    }

    @Test
    public void shouldAddCommand() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        when(wsManager.updateWorkspace(any(), any())).thenReturn(workspace);
        final CommandDto commandDto = createCommandDto();
        final int commandsSizeBefore = workspace.getConfig().getCommands().size();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(commandDto)
                                         .when()
                                         .post(SECURE_PATH + "/workspace/" + workspace.getId() + "/command");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class))
                             .getConfig()
                             .getCommands()
                             .size(), commandsSizeBefore + 1);
        verify(validator).validateConfig(workspace.getConfig());
        verify(wsManager).updateWorkspace(any(), any());
    }

    @Test
    public void shouldUpdateCommand() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        when(wsManager.updateWorkspace(any(), any())).thenReturn(workspace);
        final CommandDto commandDto = createCommandDto();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(commandDto)
                                         .when()
                                         .put(SECURE_PATH + "/workspace/" + workspace.getId() + "/command/" + commandDto.getName());

        assertEquals(response.getStatusCode(), 200);
        verify(validator).validateConfig(workspace.getConfig());
        verify(wsManager).updateWorkspace(any(), any());
    }

    @Test
    public void shouldRespond404WhenUpdatingCommandWhichDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(createCommandDto())
                                         .when()
                                         .put(SECURE_PATH + "/workspace/" + workspace.getId() + "/command/fake");

        assertEquals(response.getStatusCode(), 404);
        assertEquals(unwrapError(response), "Workspace '" + workspace.getId() + "' doesn't contain command 'fake'");
        verify(wsManager, never()).updateWorkspace(any(), any());
    }

    @Test
    public void shouldDeleteCommand() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        final int commandsSizeBefore = workspace.getConfig().getCommands().size();
        final CommandImpl firstCommand = workspace.getConfig().getCommands().iterator().next();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/" + workspace.getId()
                                                 + "/command/" + firstCommand.getName());

        assertEquals(response.getStatusCode(), 204);
        assertEquals(workspace.getConfig().getCommands().size(), commandsSizeBefore - 1);
        verify(wsManager).updateWorkspace(any(), any());
    }

    @Test
    public void shouldAddEnvironment() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        when(wsManager.updateWorkspace(any(), any())).thenReturn(workspace);
        final EnvironmentDto envDto = createEnvDto();
        final int envsSizeBefore = workspace.getConfig().getEnvironments().size();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(envDto)
                                         .when()
                                         .queryParam("name", "new-env")
                                         .post(SECURE_PATH + "/workspace/" + workspace.getId() + "/environment");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class))
                             .getConfig()
                             .getEnvironments()
                             .size(), envsSizeBefore + 1);
        verify(validator).validateConfig(workspace.getConfig());
        verify(wsManager).updateWorkspace(any(), any());
    }

    @Test
    public void shouldUpdateEnvironment() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        when(wsManager.updateWorkspace(any(), any())).thenReturn(workspace);
        final EnvironmentDto envDto = createEnvDto();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(envDto)
                                         .when()
                                         .put(SECURE_PATH + "/workspace/" + workspace.getId()
                                              + "/environment/" + workspace.getConfig().getDefaultEnv());

        assertEquals(response.getStatusCode(), 200);
        assertEquals(workspace.getConfig().getEnvironments().size(), 1);
        verify(validator).validateConfig(workspace.getConfig());
        verify(wsManager).updateWorkspace(any(), any());
    }

    @Test
    public void shouldRespond404WhenUpdatingEnvironmentWhichDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(createCommandDto())
                                         .when()
                                         .put(SECURE_PATH + "/workspace/" + workspace.getId() + "/environment/fake");

        assertEquals(response.getStatusCode(), 404);
        assertEquals(unwrapError(response), "Workspace '" + workspace.getId() + "' doesn't contain environment 'fake'");
        verify(wsManager, never()).updateWorkspace(any(), any());
    }

    @Test
    public void shouldDeleteEnvironment() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        Map.Entry<String, EnvironmentImpl> envEntry =
                workspace.getConfig().getEnvironments().entrySet().iterator().next();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/" + workspace.getId()
                                                 + "/environment/" + envEntry.getKey());

        assertEquals(response.getStatusCode(), 204);
        verify(wsManager).updateWorkspace(any(), any());
    }


    @Test
    public void shouldAddProject() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        when(wsManager.updateWorkspace(any(), any())).thenReturn(workspace);
        final ProjectConfigDto projectDto = createProjectDto();
        final int projectsSizeBefore = workspace.getConfig().getProjects().size();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(projectDto)
                                         .when()
                                         .post(SECURE_PATH + "/workspace/" + workspace.getId() + "/project");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class))
                             .getConfig()
                             .getProjects()
                             .size(), projectsSizeBefore + 1);
        verify(validator).validateConfig(workspace.getConfig());
        verify(wsManager).updateWorkspace(any(), any());
    }

    @Test
    public void shouldUpdateProject() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        when(wsManager.updateWorkspace(any(), any())).thenReturn(workspace);
        final ProjectConfigDto projectDto = createProjectDto();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(projectDto)
                                         .when()
                                         .put(SECURE_PATH + "/workspace/" + workspace.getId()
                                              + "/project" + projectDto.getPath());

        assertEquals(response.getStatusCode(), 200);
        verify(validator).validateConfig(workspace.getConfig());
        verify(wsManager).updateWorkspace(any(), any());
    }

    @Test
    public void shouldRespond404WhenUpdatingProjectWhichDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(createProjectDto())
                                         .when()
                                         .put(SECURE_PATH + "/workspace/" + workspace.getId() + "/project/fake");

        assertEquals(response.getStatusCode(), 404);
        assertEquals(unwrapError(response), "Workspace '" + workspace.getId() + "' doesn't contain project with path '/fake'");
        verify(wsManager, never()).updateWorkspace(any(), any());
    }

    @Test
    public void shouldDeleteProject() throws Exception {
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
        final ProjectConfig firstProject = workspace.getConfig().getProjects().iterator().next();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/" + workspace.getId()
                                                 + "/project" + firstProject.getPath());

        assertEquals(response.getStatusCode(), 204);
        verify(wsManager).updateWorkspace(any(), any());
    }

    @Test
    public void testWorkspaceLinks() throws Exception {
        // given
        final WorkspaceImpl workspace = createWorkspace(createConfigDto());
        EnvironmentImpl environment = workspace.getConfig()
                                               .getEnvironments()
                                               .get(workspace.getConfig().getDefaultEnv());
        assertNotNull(environment);

        final WorkspaceRuntimeImpl runtime = new WorkspaceRuntimeImpl(workspace.getConfig().getDefaultEnv());
        MachineConfigImpl devMachineConfig = MachineConfigImpl.builder()
                                                              .setDev(true)
                                                              .setEnvVariables(emptyMap())
                                                              .setServers(emptyList())
                                                              .setLimits(new LimitsImpl(1024))
                                                              .setSource(new MachineSourceImpl("type").setContent("content"))
                                                              .setName(environment.getMachines()
                                                                                  .keySet()
                                                                                  .iterator()
                                                                                  .next())
                                                              .setType("type")
                                                              .build();
        runtime.setDevMachine(new MachineImpl(devMachineConfig,
                                              "machine123",
                                              workspace.getId(),
                                              workspace.getConfig().getDefaultEnv(),
                                              USER_ID,
                                              MachineStatus.RUNNING,
                                              new MachineRuntimeInfoImpl(emptyMap(),
                                                                         emptyMap(),
                                                                         singletonMap("8080/https", new ServerImpl("wsagent",
                                                                                                                   "8080",
                                                                                                                   "https",
                                                                                                                   "path1",
                                                                                                                   "url")))));
        runtime.getMachines().add(runtime.getDevMachine());
        workspace.setStatus(RUNNING);
        workspace.setRuntime(runtime);
        when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

        // when
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/workspace/" + workspace.getId());

        // then
        assertEquals(response.getStatusCode(), 200);
        final WorkspaceDto workspaceDto = unwrapDto(response, WorkspaceDto.class);
        final Set<String> actualRels = workspaceDto.getLinks()
                                                   .stream()
                                                   .map(Link::getRel)
                                                   .collect(toSet());
        final Set<String> expectedRels = new HashSet<>(asList(LINK_REL_START_WORKSPACE,
                                                              LINK_REL_REMOVE_WORKSPACE,
                                                              GET_ALL_USER_WORKSPACES,
                                                              LINK_REL_GET_SNAPSHOT,
                                                              LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL,
                                                              LINK_REL_IDE_URL,
                                                              LINK_REL_SELF,
                                                              LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL,
                                                              LINK_REL_ENVIRONMENT_STATUS_CHANNEL));
        assertTrue(actualRels.equals(expectedRels), format("Links difference: '%s'. \n" +
                                                           "Returned links: '%s', \n" +
                                                           "Expected links: '%s'.",
                                                           Sets.symmetricDifference(actualRels, expectedRels),
                                                           actualRels.toString(),
                                                           expectedRels.toString()));
        assertNotNull(workspaceDto.getRuntime().getLink(LINK_REL_STOP_WORKSPACE), "Runtime doesn't contain stop link");
        assertNotNull(workspaceDto.getRuntime().getLink(WSAGENT_REFERENCE), "Runtime doesn't contain wsagent link");
        assertNotNull(workspaceDto.getRuntime().getLink(WSAGENT_WEBSOCKET_REFERENCE), "Runtime doesn't contain wsagent.websocket link");
    }

    @Test
    public void shouldReturnSnapshotsOnGetSnapshot() throws Exception {
        // given
        String workspaceId = "testWsId1";
        SnapshotImpl.SnapshotBuilder snapshotBuilder = SnapshotImpl.builder()
                                                                   .setCreationDate(System.currentTimeMillis())
                                                                   .setDescription("description")
                                                                   .setDev(true)
                                                                   .setEnvName("envName")
                                                                   .setId("snap1")
                                                                   .setMachineName("machine1")
                                                                   .setMachineSource(new MachineSourceImpl("type")
                                                                                             .setContent("content"))
                                                                   .setNamespace("namespace")
                                                                   .setType("type")
                                                                   .setWorkspaceId(workspaceId);
        SnapshotImpl snapshot1 = snapshotBuilder.build();
        SnapshotImpl snapshot2 = snapshotBuilder.setDev(false).build();

        List<SnapshotImpl> originSnapshots = Arrays.asList(snapshot1, snapshot2);
        when(wsManager.getSnapshot(workspaceId)).thenReturn(originSnapshots);

        // when
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/workspace/" + workspaceId + "/snapshot");

        // then
        assertEquals(response.getStatusCode(), 200);
        List<SnapshotDto> snapshotDtos = unwrapDtoList(response, SnapshotDto.class);
        List<SnapshotImpl> newSnapshots = snapshotDtos.stream().map(SnapshotImpl::new).collect(Collectors.toList());
        originSnapshots.forEach(snapshot -> snapshot.setMachineSource(null));
        assertEquals(newSnapshots, originSnapshots);
        verify(wsManager).getSnapshot(workspaceId);
    }

    @Test
    public void shouldReturnEmptyListIfNotSnapshotsFound() throws Exception {
        // given
        String workspaceId = "testWsId1";

        when(wsManager.getSnapshot(workspaceId)).thenReturn(Collections.emptyList());

        // when
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/workspace/" + workspaceId + "/snapshot");

        // then
        assertEquals(response.getStatusCode(), 200);
        List<SnapshotDto> snapshotDtos = unwrapDtoList(response, SnapshotDto.class);
        assertTrue(snapshotDtos.isEmpty());
        verify(wsManager).getSnapshot(workspaceId);
    }

    private static String unwrapError(Response response) {
        return unwrapDto(response, ServiceError.class).getMessage();
    }

    private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }

    private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass)
                         .stream()
                         .collect(toList());
    }

    private static WorkspaceImpl createWorkspace(WorkspaceConfig configDto, WorkspaceStatus status) {
        return WorkspaceImpl.builder()
                            .setConfig(configDto)
                            .generateId()
                            .setNamespace(NAMESPACE)
                            .setStatus(status)
                            .build();
    }

    private static WorkspaceImpl createWorkspace(WorkspaceConfig configDto) {
        return createWorkspace(configDto, WorkspaceStatus.STOPPED);
    }

    private static CommandDto createCommandDto() {
        return DtoConverter.asDto(new CommandImpl("MCI", "mvn clean install", "maven"));
    }

    private static ProjectConfigDto createProjectDto() {
        return newDto(ProjectConfigDto.class).withName("project-name")
                                             .withPath("/project/path")
                                             .withDescription("Test project")
                                             .withMixins(new ArrayList<>(singleton("maven")))
                                             .withSource(newDto(SourceStorageDto.class).withLocation("location")
                                                                                       .withType("type"))
                                             .withAttributes(new HashMap<>());

    }

    private static EnvironmentDto createEnvDto() {
        ExtendedMachineImpl devMachine = new ExtendedMachineImpl(singletonList("ws-agent"), null);

        return DtoConverter.asDto(new EnvironmentImpl(new EnvironmentRecipeImpl("type", "content-type", "content", null),
                                                      singletonMap("dev-machine", devMachine)));
    }

    private static WorkspaceConfigDto createConfigDto() {
        final WorkspaceConfigImpl config = WorkspaceConfigImpl.builder()
                                                              .setName("dev-workspace")
                                                              .setDefaultEnv("dev-env")
                                                              .setEnvironments(new HashMap<>(singletonMap("dev-env",
                                                                                                          new EnvironmentImpl(
                                                                                                                  createEnvDto()))))
                                                              .setCommands(new ArrayList<>(singleton(createCommandDto())))
                                                              .setProjects(new ArrayList<>(singleton(createProjectDto())))
                                                              .build();
        return DtoConverter.asDto(config);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {

        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(new SubjectImpl(NAMESPACE, USER_ID, "token", false));
        }
    }
}
