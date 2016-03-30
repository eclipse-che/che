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

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.permission.PermissionManager;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.shared.DTO;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.impl.provider.JsonEntityProvider;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link WorkspaceService}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class WorkspaceServiceTest {

    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER  = new ApiExceptionMapper();
    private static final String             USER_ID = "user123";
    private static final LinkedList<String> ROLES   = new LinkedList<>(singleton("user"));
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER  = new EnvironmentFilter();
    @SuppressWarnings("unused")
    private static final DtoBodyReader      READER  = new DtoBodyReader();

    @Mock
    private WorkspaceManager   manager;
    @Mock
    private MachineManager     machineManager;
    @Mock
    private WorkspaceValidator validator;
    @Mock
    private PermissionManager  permissionManager;
    @InjectMocks
    private WorkspaceService   service;

    @Test
    public void shouldCreateWorkspace() throws Exception {
        final WorkspaceConfigDto configDto = createConfigDto();
        final WorkspaceImpl workspace = createWorkspace(configDto);
        when(manager.createWorkspace(any(), any(), any(), any())).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(configDto.toString())
                                         .when()
                                         .post(SECURE_PATH + "/workspace" +
                                               "?attribute=stackId:stack123" +
                                               "&attribute=factoryId:factory123" +
                                               "&attribute=custom:custom:value");

        assertEquals(response.getStatusCode(), 201);
        assertEquals(new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class)), workspace);
        verify(validator).validateConfig(any());
        verify(validator).validateAttributes(any());
        verify(manager).createWorkspace(anyObject(),
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
        when(manager.getWorkspace(workspace.getId())).thenReturn(workspace);

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
        when(manager.getWorkspaces(USER_ID)).thenReturn(asList(workspace1, workspace2));

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
        when(manager.getWorkspaces(USER_ID)).thenReturn(asList(workspace1, workspace2));

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
        when(manager.updateWorkspace(workspace.getId(), workspace)).thenReturn(workspace);
        final WorkspaceDto workspaceDto = DtoConverter.asDto(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(workspaceDto)
                                         .when()
                                         .put(SECURE_PATH + "/workspace/" + workspace.getId());

        assertEquals(response.getStatusCode(), 200);
        assertEquals(unwrapDto(response, WorkspaceDto.class), workspace);
        verify(validator).validateWorkspace(workspace);
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
                            .setNamespace(USER_ID)
                            .setStatus(status)
                            .build();
    }

    private static WorkspaceImpl createWorkspace(WorkspaceConfig configDto) {
        return createWorkspace(configDto, WorkspaceStatus.STOPPED);
    }

    private static WorkspaceConfigDto createConfigDto() {
        final MachineConfigImpl devMachine = MachineConfigImpl.builder()
                                                              .setDev(true)
                                                              .setName("dev-machine")
                                                              .setType("docker")
                                                              .setSource(new MachineSourceImpl("location", "recipe"))
                                                              .setServers(asList(new ServerConfImpl("ref1",
                                                                                                    "8080",
                                                                                                    "https",
                                                                                                    "path1"),
                                                                                 new ServerConfImpl("ref2",
                                                                                                    "8081",
                                                                                                    "https",
                                                                                                    "path2")))
                                                              .setEnvVariables(singletonMap("key1", "value1"))
                                                              .build();
        final WorkspaceConfigImpl config = WorkspaceConfigImpl.builder()
                                                              .setName("dev-workspace")
                                                              .setDefaultEnv("dev-env")
                                                              .setEnvironments(singletonList(
                                                                      new EnvironmentImpl("dev-env", null, singletonList(devMachine))))
                                                              .build();
        return DtoConverter.asDto(config);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {

        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setUser(new UserImpl("user", USER_ID, "token", ROLES, false));
        }
    }

    @Provider
    public static class DtoBodyReader extends JsonEntityProvider {
        @Override
        public Object readFrom(Class type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap httpHeaders,
                               InputStream entityStream) throws IOException {
            if (type.isAnnotationPresent(DTO.class)) {
                return DtoFactory.getInstance().createDtoFromJson(entityStream, type);
            }
            return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
        }
    }
}