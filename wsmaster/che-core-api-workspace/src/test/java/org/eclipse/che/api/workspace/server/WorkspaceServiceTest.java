/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.RUNNING;
import static org.eclipse.che.api.workspace.server.DtoConverter.asDto;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_FACTORY_DEFAULT_EDITOR_PROPERTY;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_FACTORY_DEFAULT_PLUGINS_PROPERTY;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_AUTO_START;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_STORAGE_AVAILABLE_TYPES;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_STORAGE_PREFERRED_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES;
import static org.eclipse.che.api.workspace.shared.Constants.SUPPORTED_RECIPE_TYPES;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.restassured.response.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.workspace.server.devfile.DevfileEntityProvider;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.devfile.DevfileVersionDetector;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeDto;
import org.eclipse.che.api.workspace.shared.dto.ServerDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.MetadataDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.ProjectDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.SourceDto;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link WorkspaceService}.
 *
 * @author Yevhenii Voevodin
 * @author Sergii Leshchenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class WorkspaceServiceTest {

  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  private static final String NAMESPACE = "user";
  private static final String USER_ID = "user123";
  private static final String API_ENDPOINT = "http://localhost:8080/api";
  private static final String CHE_WORKSPACE_PLUGIN_REGISTRY_URL = "http://localhost:9898/plugins/";
  private static final String CHE_WORKSPACE_PLUGIN_REGISTRY_INTERNAL_URL =
      "http://plugin-registry.che.svc.cluster.local/v3";
  private static final String CHE_WORKSPACE_DEVFILE_REGISTRY_URL =
      "http://localhost:9898/devfiles/";
  private static final String CHE_WORKSPACE_DEVFILE_REGISTRY_INTERNAL_URL =
      "http://plugin-registry.che.svc.cluster.local";
  private static final boolean CHE_WORKSPACES_DEFAULT_PERSIST_VOLUMES = false;
  private static final Long LOG_LIMIT_BYTES = 64L;

  private static final Account TEST_ACCOUNT = new AccountImpl("anyId", NAMESPACE, "test");

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  private final String availableStorageTypes = "persistent,ephemeral,async";
  private final String preferredStorageType = "persistent";
  private final String defaultEditor = "theia";
  private final String defaultPlugins = "machine-exec";

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private CheJsonProvider jsonProvider = new CheJsonProvider(Collections.emptySet());

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private DevfileEntityProvider devfileEntityProvider =
      new DevfileEntityProvider(
          new DevfileParser(
              new DevfileSchemaValidator(new DevfileSchemaProvider(), new DevfileVersionDetector()),
              new DevfileIntegrityValidator(Collections.emptyMap())));

  @Mock private WorkspaceManager wsManager;
  @Mock private MachineTokenProvider machineTokenProvider;
  @Mock private WorkspaceLinksGenerator linksGenerator;
  @Mock private URLFetcher urlFetcher;

  private WorkspaceService service;

  @BeforeMethod
  public void setup() {
    service =
        new WorkspaceService(
            API_ENDPOINT,
            true,
            wsManager,
            machineTokenProvider,
            linksGenerator,
            CHE_WORKSPACE_PLUGIN_REGISTRY_URL,
            CHE_WORKSPACE_PLUGIN_REGISTRY_INTERNAL_URL,
            CHE_WORKSPACE_DEVFILE_REGISTRY_URL,
            CHE_WORKSPACE_DEVFILE_REGISTRY_INTERNAL_URL,
            urlFetcher,
            LOG_LIMIT_BYTES,
            availableStorageTypes,
            preferredStorageType,
            defaultEditor,
            defaultPlugins);
  }

  @Test
  public void shouldCreateWorkspaceFromDevfile() throws Exception {
    final DevfileDto devfileDto = createDevfileDto();
    final WorkspaceImpl workspace = createWorkspace(devfileDto);

    when(wsManager.createWorkspace(any(Devfile.class), anyString(), any(), any()))
        .thenReturn(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(devfileDto)
            .when()
            .post(
                SECURE_PATH
                    + "/workspace/devfile"
                    + "?namespace=test"
                    + "&attribute=factoryId:factory123"
                    + "&attribute=custom:custom:value");

    assertEquals(response.getStatusCode(), 201);
    assertEquals(
        new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class), TEST_ACCOUNT), workspace);
    verify(wsManager)
        .createWorkspace(
            any(Devfile.class),
            eq("test"),
            eq(
                ImmutableMap.of(
                    "factoryId", "factory123",
                    "custom", "custom:value")),
            any());
  }

  @Test
  public void shouldCreateWorkspaceFromDevfileInNamespace() throws Exception {
    final DevfileDto devfileDto = createDevfileDto();
    final WorkspaceImpl workspace = createWorkspace(devfileDto);

    when(wsManager.createWorkspace(any(Devfile.class), anyString(), any(), any()))
        .thenReturn(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(devfileDto)
            .when()
            .post(
                SECURE_PATH
                    + "/workspace/devfile"
                    + "?namespace=test"
                    + "&attribute=factoryId:factory123"
                    + "&attribute=custom:custom:value"
                    + "&infrastructure-namespace=my-namespace");

    assertEquals(response.getStatusCode(), 201);
    assertEquals(
        new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class), TEST_ACCOUNT), workspace);
    verify(wsManager)
        .createWorkspace(
            any(Devfile.class),
            eq("test"),
            eq(
                ImmutableMap.of(
                    "factoryId",
                    "factory123",
                    "custom",
                    "custom:value",
                    "infrastructureNamespace",
                    "my-namespace")),
            any());
  }

  @Test
  public void shouldReturnBadRequestOnInvalidDevfile() throws Exception {
    final DevfileDto devfileDto = createDevfileDto();
    final WorkspaceImpl workspace = createWorkspace(devfileDto);

    when(wsManager.createWorkspace(any(Devfile.class), anyString(), any(), any()))
        .thenThrow(new ValidationException("boom"));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(devfileDto)
            .when()
            .post(
                SECURE_PATH
                    + "/workspace/devfile"
                    + "?namespace=test"
                    + "&attribute=factoryId:factory123"
                    + "&attribute=custom:custom:value");

    assertEquals(response.getStatusCode(), 400);
    String error = unwrapError(response);
    assertEquals(error, "boom");
  }

  @Test
  public void createShouldReturn400WhenAttributesAreNotValid() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(createConfigDto())
            .when()
            .post(SECURE_PATH + "/workspace/devfile?attribute=factoryId=factoryId123");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(
        unwrapError(response),
        "Attribute 'factoryId=factoryId123' is not valid, "
            + "it should contain name and value separated "
            + "with colon. For example: attributeName:attributeValue");
  }

  @Test
  public void createShouldReturn400WhenConfigIsNotSent() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/workspace/devfile");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(unwrapError(response), "Devfile required");
  }

  @Test(dataProvider = "validWorkspaceKeys")
  public void shouldGetWorkspaceByKey(String key) throws Exception {
    final WorkspaceImpl workspace = createWorkspace(createConfigDto());
    when(wsManager.getWorkspace(key)).thenReturn(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/workspace/" + key);

    assertEquals(response.getStatusCode(), 200);
    assertEquals(
        new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class), TEST_ACCOUNT), workspace);
    verify(machineTokenProvider, never()).getToken(workspace.getId());
  }

  @DataProvider
  public Object[][] validWorkspaceKeys() {
    return new Object[][] {{"workspaceId"}, {"namespace:name"}, {":name"}};
  }

  @Test
  public void shouldGetWorkspaceWithExternalServersByDefault() throws Exception {
    // given
    WorkspaceImpl workspace = createWorkspace(createConfigDto());
    String externalServerKey = "server2";
    ServerImpl externalServer = createExternalServer();
    Map<String, Server> servers =
        ImmutableMap.of("server1", createInternalServer(), externalServerKey, externalServer);
    Map<String, Machine> machines =
        singletonMap("machine1", new MachineImpl(singletonMap("key", "value"), servers, RUNNING));
    workspace.setRuntime(new RuntimeImpl("activeEnv", machines, "user123"));
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
    Map<String, MachineDto> expected =
        singletonMap(
            "machine1",
            newDto(MachineDto.class)
                .withAttributes(singletonMap("key", "value"))
                .withStatus(RUNNING)
                .withServers(
                    singletonMap(
                        externalServerKey,
                        newDto(ServerDto.class)
                            .withUrl(externalServer.getUrl())
                            .withStatus(externalServer.getStatus())
                            .withAttributes(externalServer.getAttributes()))));

    // when
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/workspace/" + workspace.getId());

    // then
    assertEquals(response.getStatusCode(), 200);
    RuntimeDto retrievedRuntime = unwrapDto(response, WorkspaceDto.class).getRuntime();
    assertNotNull(retrievedRuntime);
    assertEquals(expected, retrievedRuntime.getMachines());
  }

  @Test
  public void shouldTreatServerWithInternalServerAttributeNotEqualToTrueExternal()
      throws Exception {
    // given
    WorkspaceImpl workspace = createWorkspace(createConfigDto());
    String externalServerKey = "server2";
    ServerImpl externalServer =
        createInternalServer()
            .withAttributes(singletonMap(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, ""));
    Map<String, Server> servers =
        ImmutableMap.of("server1", createInternalServer(), externalServerKey, externalServer);
    Map<String, Machine> machines =
        singletonMap("machine1", new MachineImpl(singletonMap("key", "value"), servers, RUNNING));
    workspace.setRuntime(new RuntimeImpl("activeEnv", machines, "user123"));
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
    Map<String, MachineDto> expected =
        singletonMap(
            "machine1",
            newDto(MachineDto.class)
                .withAttributes(singletonMap("key", "value"))
                .withStatus(RUNNING)
                .withServers(
                    singletonMap(
                        externalServerKey,
                        newDto(ServerDto.class)
                            .withUrl(externalServer.getUrl())
                            .withStatus(externalServer.getStatus())
                            .withAttributes(externalServer.getAttributes()))));

    // when
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/workspace/" + workspace.getId());

    // then
    assertEquals(response.getStatusCode(), 200);
    RuntimeDto retrievedRuntime = unwrapDto(response, WorkspaceDto.class).getRuntime();
    assertNotNull(retrievedRuntime);
    assertEquals(expected, retrievedRuntime.getMachines());
  }

  @Test
  public void shouldGetWorkspaceWithInternalServers() throws Exception {
    // given
    WorkspaceImpl workspace = createWorkspace(createConfigDto());
    String externalServerKey = "server2";
    String internalServerKey = "server1";
    ServerImpl externalServer = createExternalServer();
    ServerImpl internalServer = createInternalServer();
    Map<String, Server> servers =
        ImmutableMap.of(
            internalServerKey, createInternalServer(), externalServerKey, externalServer);
    Map<String, Machine> machines =
        singletonMap("machine1", new MachineImpl(singletonMap("key", "value"), servers, RUNNING));
    workspace.setRuntime(new RuntimeImpl("activeEnv", machines, "user123"));
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

    Map<String, MachineDto> expected =
        singletonMap(
            "machine1",
            newDto(MachineDto.class)
                .withAttributes(singletonMap("key", "value"))
                .withStatus(RUNNING)
                .withServers(
                    ImmutableMap.of(
                        externalServerKey,
                        newDto(ServerDto.class)
                            .withUrl(externalServer.getUrl())
                            .withStatus(externalServer.getStatus())
                            .withAttributes(externalServer.getAttributes()),
                        internalServerKey,
                        newDto(ServerDto.class)
                            .withUrl(createInternalServer().getUrl())
                            .withStatus(internalServer.getStatus())
                            .withAttributes(internalServer.getAttributes()))));

    // when
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .queryParameter("includeInternalServers", Boolean.TRUE.toString())
            .when()
            .get(SECURE_PATH + "/workspace/" + workspace.getId());

    // then
    assertEquals(response.getStatusCode(), 200);
    RuntimeDto retrievedRuntime = unwrapDto(response, WorkspaceDto.class).getRuntime();
    assertNotNull(retrievedRuntime);
    assertEquals(expected, retrievedRuntime.getMachines());
  }

  @Test
  public void shouldGetWorkspaceWithInternalServersIfCorrespondingQueryParamHasEmptyValue()
      throws Exception {
    // given
    WorkspaceImpl workspace = createWorkspace(createConfigDto());
    String externalServerKey = "server2";
    String internalServerKey = "server1";
    ServerImpl externalServer = createExternalServer();
    ServerImpl internalServer = createInternalServer();
    Map<String, Server> servers =
        ImmutableMap.of(
            internalServerKey, createInternalServer(), externalServerKey, externalServer);
    Map<String, Machine> machines =
        singletonMap("machine1", new MachineImpl(singletonMap("key", "value"), servers, RUNNING));
    workspace.setRuntime(new RuntimeImpl("activeEnv", machines, "user123"));
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

    Map<String, MachineDto> expected =
        singletonMap(
            "machine1",
            newDto(MachineDto.class)
                .withAttributes(singletonMap("key", "value"))
                .withStatus(RUNNING)
                .withServers(
                    ImmutableMap.of(
                        externalServerKey,
                        newDto(ServerDto.class)
                            .withUrl(externalServer.getUrl())
                            .withStatus(externalServer.getStatus())
                            .withAttributes(externalServer.getAttributes()),
                        internalServerKey,
                        newDto(ServerDto.class)
                            .withUrl(createInternalServer().getUrl())
                            .withStatus(internalServer.getStatus())
                            .withAttributes(internalServer.getAttributes()))));

    // when
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .queryParameter("includeInternalServers", "")
            .when()
            .get(SECURE_PATH + "/workspace/" + workspace.getId());

    // then
    assertEquals(response.getStatusCode(), 200);
    RuntimeDto retrievedRuntime = unwrapDto(response, WorkspaceDto.class).getRuntime();
    assertNotNull(retrievedRuntime);
    assertEquals(expected, retrievedRuntime.getMachines());
  }

  @Test
  public void shouldGetWorkspaceWithInternalServersIfCorrespondingQueryParamHasNoValue()
      throws Exception {
    // given
    WorkspaceImpl workspace = createWorkspace(createConfigDto());
    String externalServerKey = "server2";
    String internalServerKey = "server1";
    ServerImpl externalServer = createExternalServer();
    ServerImpl internalServer = createInternalServer();
    Map<String, Server> servers =
        ImmutableMap.of(
            internalServerKey, createInternalServer(), externalServerKey, externalServer);
    Map<String, Machine> machines =
        singletonMap("machine1", new MachineImpl(singletonMap("key", "value"), servers, RUNNING));
    workspace.setRuntime(new RuntimeImpl("activeEnv", machines, "user123"));
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

    Map<String, MachineDto> expected =
        singletonMap(
            "machine1",
            newDto(MachineDto.class)
                .withAttributes(singletonMap("key", "value"))
                .withStatus(RUNNING)
                .withServers(
                    ImmutableMap.of(
                        externalServerKey,
                        newDto(ServerDto.class)
                            .withUrl(externalServer.getUrl())
                            .withStatus(externalServer.getStatus())
                            .withAttributes(externalServer.getAttributes()),
                        internalServerKey,
                        newDto(ServerDto.class)
                            .withUrl(createInternalServer().getUrl())
                            .withStatus(internalServer.getStatus())
                            .withAttributes(internalServer.getAttributes()))));

    // when
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .queryParameter("includeInternalServers")
            .when()
            .get(SECURE_PATH + "/workspace/" + workspace.getId());

    // then
    assertEquals(response.getStatusCode(), 200);
    RuntimeDto retrievedRuntime = unwrapDto(response, WorkspaceDto.class).getRuntime();
    assertNotNull(retrievedRuntime);
    assertEquals(expected, retrievedRuntime.getMachines());
  }

  @Test
  public void shouldReturnWorkspaceWithTokenIfRuntimeExists() throws Exception {
    final WorkspaceImpl workspace = createWorkspace(createConfigDto());
    workspace.setRuntime(new RuntimeImpl("activeEnv", emptyMap(), "user123"));
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
    when(machineTokenProvider.getToken(anyString())).thenReturn("superToken");

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/workspace/" + workspace.getId());

    assertEquals(response.getStatusCode(), 200);
    WorkspaceDto retrievedWorkspace = unwrapDto(response, WorkspaceDto.class);
    assertEquals(retrievedWorkspace.getRuntime().getMachineToken(), "superToken");
    verify(machineTokenProvider).getToken(workspace.getId());
  }

  @Test(dataProvider = "invalidWorkspaceKeys")
  public void getWorkspaceByKeyShouldReturn400WhenKeyIsInvalid(String workspaceKey)
      throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/workspace/" + workspaceKey);

    assertEquals(response.getStatusCode(), 400);
  }

  @DataProvider
  public Object[][] invalidWorkspaceKeys() {
    return new Object[][] {{"first:second:third"}, {"namespace:"}};
  }

  @Test
  public void shouldGetWorkspaces() throws Exception {
    final WorkspaceImpl workspace1 = createWorkspace(createConfigDto());
    final WorkspaceImpl workspace2 = createWorkspace(createConfigDto(), STARTING);
    when(wsManager.getWorkspaces(eq(USER_ID), eq(false), anyInt(), anyLong()))
        .thenReturn(new Page<>(asList(workspace1, workspace2), 0, 2, 2));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/workspace");

    assertEquals(response.getStatusCode(), 200);
    assertNotNull(response.getHeader("Link"));
    assertEquals(
        unwrapDtoList(response, WorkspaceDto.class)
            .stream()
            .map(ws -> new WorkspaceImpl(ws, TEST_ACCOUNT))
            .collect(toList()),
        asList(workspace1, workspace2));
  }

  @Test
  public void shouldGetWorkspacesByNamespace() throws Exception {
    final WorkspaceImpl workspace1 = createWorkspace(createConfigDto());
    final WorkspaceImpl workspace2 = createWorkspace(createConfigDto(), STARTING);
    when(wsManager.getByNamespace(eq(NAMESPACE), eq(false), anyInt(), anyLong()))
        .thenReturn(new Page<>(asList(workspace1, workspace2), 0, 2, 2));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/workspace/namespace/" + NAMESPACE);

    assertEquals(response.getStatusCode(), 200);
    assertEquals(
        unwrapDtoList(response, WorkspaceDto.class)
            .stream()
            .map(ws -> new WorkspaceImpl(ws, TEST_ACCOUNT))
            .collect(toList()),
        asList(workspace1, workspace2));
  }

  @Test
  public void shouldGetWorkspacesByStatus() throws Exception {
    final WorkspaceImpl workspace1 = createWorkspace(createConfigDto());
    final WorkspaceImpl workspace2 = createWorkspace(createConfigDto(), STARTING);
    when(wsManager.getWorkspaces(eq(USER_ID), eq(false), anyInt(), anyLong()))
        .thenReturn(new Page<>(asList(workspace1, workspace2), 0, 2, 2));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/workspace?status=starting");

    assertEquals(response.getStatusCode(), 200);
    assertEquals(
        unwrapDtoList(response, WorkspaceDto.class)
            .stream()
            .map(ws -> new WorkspaceImpl(ws, TEST_ACCOUNT))
            .collect(toList()),
        singletonList(workspace2));
  }

  @Test
  public void shouldUpdateTheWorkspace() throws Exception {
    final WorkspaceImpl workspace = createWorkspace(createConfigDto());
    when(wsManager.updateWorkspace(any(), any())).thenReturn(workspace);
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
    final WorkspaceDto workspaceDto = asDto(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(workspaceDto)
            .when()
            .put(SECURE_PATH + "/workspace/" + workspace.getId());

    assertEquals(response.getStatusCode(), 200);
    assertEquals(
        new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class), TEST_ACCOUNT), workspace);
  }

  @Test
  public void shouldUpdateWorkspaceWithRuntime() throws Exception {
    final WorkspaceImpl workspace = createWorkspace(createConfigDto());
    MachineImpl machine = new MachineImpl(emptyMap(), emptyMap(), MachineStatus.STARTING);
    RuntimeImpl runtime = new RuntimeImpl("myenv", singletonMap("machine1", machine), "owner");
    workspace.setRuntime(runtime);
    when(wsManager.updateWorkspace(any(), any())).thenReturn(workspace);
    // when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);
    final WorkspaceDto workspaceDto = asDto(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(workspaceDto)
            .when()
            .put(SECURE_PATH + "/workspace/" + workspace.getId());

    assertEquals(response.getStatusCode(), 200);
    assertEquals(unwrapDto(response, WorkspaceDto.class).getRuntime(), asDto(runtime));
  }

  @Test
  public void shouldNotUpdateTheWorkspaceWithConfigAndDevfileAtTheSameTime() throws Exception {
    final WorkspaceDto workspaceDto =
        newDto(WorkspaceDto.class)
            .withId("workspace123")
            .withConfig(newDto(WorkspaceConfigDto.class))
            .withDevfile(newDto(DevfileDto.class));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(workspaceDto)
            .when()
            .put(SECURE_PATH + "/workspace/" + workspaceDto.getId());

    assertEquals(response.getStatusCode(), 400);
    assertEquals(
        unwrapError(response),
        "Required non-null workspace configuration or devfile update but not both");
  }

  @Test
  public void shouldNotUpdateTheWorkspaceWithoutConfigAndDevfile() throws Exception {
    final WorkspaceDto workspaceDto = newDto(WorkspaceDto.class).withId("workspace123");

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(workspaceDto)
            .when()
            .put(SECURE_PATH + "/workspace/" + workspaceDto.getId());

    assertEquals(response.getStatusCode(), 400);
    assertEquals(
        unwrapError(response),
        "Required non-null workspace configuration or devfile update but not both");
  }

  @Test
  public void shouldDeleteWorkspace() throws Exception {
    final WorkspaceImpl workspace = createWorkspace(createConfigDto());

    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/workspace/" + workspace.getId());

    assertEquals(response.getStatusCode(), 204);
    verify(wsManager).removeWorkspace(workspace.getId());
  }

  @Test
  public void shouldStartWorkspace() throws Exception {
    final WorkspaceImpl workspace = createWorkspace(createConfigDto());
    when(wsManager.startWorkspace(any(), any(), any())).thenReturn(workspace);
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .post(
                SECURE_PATH
                    + "/workspace/"
                    + workspace.getId()
                    + "/runtime"
                    + "?environment="
                    + workspace.getConfig().getDefaultEnv());

    assertEquals(response.getStatusCode(), 200);
    assertEquals(
        new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class), TEST_ACCOUNT), workspace);
    verify(wsManager)
        .startWorkspace(workspace.getId(), workspace.getConfig().getDefaultEnv(), emptyMap());
  }

  @Test
  public void shouldStartWorkspaceWithStartupDebug() throws Exception {
    final WorkspaceImpl workspace = createWorkspace(createConfigDto());
    when(wsManager.startWorkspace(any(), any(), any())).thenReturn(workspace);
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .post(
                SECURE_PATH
                    + "/workspace/"
                    + workspace.getId()
                    + "/runtime"
                    + "?environment="
                    + workspace.getConfig().getDefaultEnv()
                    + "&"
                    + DEBUG_WORKSPACE_START
                    + "=true");

    assertEquals(response.getStatusCode(), 200);
    assertEquals(
        new WorkspaceImpl(unwrapDto(response, WorkspaceDto.class), TEST_ACCOUNT), workspace);
    verify(wsManager)
        .startWorkspace(
            workspace.getId(),
            workspace.getConfig().getDefaultEnv(),
            ImmutableMap.of(
                DEBUG_WORKSPACE_START,
                Boolean.TRUE.toString(),
                DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES,
                "64"));
  }

  @Test
  public void shouldStopWorkspace() throws Exception {
    final WorkspaceImpl workspace = createWorkspace(createConfigDto());
    when(wsManager.getWorkspace(workspace.getId())).thenReturn(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/workspace/" + workspace.getId() + "/runtime");

    assertEquals(response.getStatusCode(), 204);
    verify(wsManager).stopWorkspace(workspace.getId(), emptyMap());
  }

  @Test
  public void shouldBeAbleToGetSettings() {
    when(wsManager.getSupportedRecipes()).thenReturn(ImmutableSet.of("dockerimage", "dockerfile"));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/workspace/settings");

    assertEquals(response.getStatusCode(), 200);
    final Map<String, String> settings =
        new Gson().fromJson(response.print(), new TypeToken<Map<String, String>>() {}.getType());
    assertEquals(
        settings,
        new ImmutableMap.Builder<>()
            .put(SUPPORTED_RECIPE_TYPES, "dockerimage,dockerfile")
            .put(CHE_WORKSPACE_AUTO_START, "true")
            .put("cheWorkspacePluginRegistryUrl", CHE_WORKSPACE_PLUGIN_REGISTRY_URL)
            .put(
                "cheWorkspacePluginRegistryInternalUrl", CHE_WORKSPACE_PLUGIN_REGISTRY_INTERNAL_URL)
            .put("cheWorkspaceDevfileRegistryUrl", CHE_WORKSPACE_DEVFILE_REGISTRY_URL)
            .put(
                "cheWorkspaceDevfileRegistryInternalUrl",
                CHE_WORKSPACE_DEVFILE_REGISTRY_INTERNAL_URL)
            .put(CHE_WORKSPACE_STORAGE_AVAILABLE_TYPES, availableStorageTypes)
            .put(CHE_WORKSPACE_STORAGE_PREFERRED_TYPE, preferredStorageType)
            .put(CHE_FACTORY_DEFAULT_EDITOR_PROPERTY, defaultEditor)
            .put(CHE_FACTORY_DEFAULT_PLUGINS_PROPERTY, defaultPlugins)
            .build());
  }

  private static String unwrapError(Response response) {
    return unwrapDto(response, ServiceError.class).getMessage();
  }

  private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass);
  }

  private static WorkspaceImpl createWorkspace(WorkspaceConfig configDto, WorkspaceStatus status) {
    return WorkspaceImpl.builder()
        .setConfig(configDto)
        .generateId()
        .setAccount(TEST_ACCOUNT)
        .setStatus(status)
        .build();
  }

  private WorkspaceImpl createWorkspace(DevfileDto devfileDto) {
    return WorkspaceImpl.builder()
        .generateId()
        .setDevfile(devfileDto)
        .setAccount(TEST_ACCOUNT)
        .setStatus(STOPPED)
        .build();
  }

  private DevfileDto createDevfileDto() {
    return newDto(DevfileDto.class)
        .withApiVersion("0.0.1")
        .withMetadata(newDto(MetadataDto.class).withName("ws"))
        .withProjects(
            singletonList(
                newDto(ProjectDto.class)
                    .withName("project")
                    .withSource(
                        newDto(SourceDto.class)
                            .withLocation("https://github.com/eclipse/che.git"))));
  }

  private static WorkspaceImpl createWorkspace(WorkspaceConfig configDto) {
    return createWorkspace(configDto, WorkspaceStatus.STOPPED);
  }

  private static CommandDto createCommandDto() {
    return asDto(new CommandImpl("MCI", "mvn clean install", "maven"));
  }

  private static ProjectConfigDto createProjectDto() {
    return newDto(ProjectConfigDto.class)
        .withName("project-name")
        .withPath("/project/path")
        .withDescription("Test project")
        .withMixins(new ArrayList<>(singleton("maven")))
        .withSource(newDto(SourceStorageDto.class).withLocation("location").withType("type"))
        .withAttributes(new HashMap<>());
  }

  private static EnvironmentDto createEnvDto() {
    MachineConfigImpl devMachine =
        new MachineConfigImpl(
            null,
            singletonMap("CHE_ENV", "value"),
            singletonMap(MEMORY_LIMIT_ATTRIBUTE, "10000"),
            emptyMap());

    return asDto(
        new EnvironmentImpl(
            new RecipeImpl("type", "content-type", "content", null),
            singletonMap("dev-machine", devMachine)));
  }

  private static WorkspaceConfigDto createConfigDto() {
    final WorkspaceConfigImpl config =
        WorkspaceConfigImpl.builder()
            .setName("dev-workspace")
            .setDefaultEnv("dev-env")
            .setEnvironments(singletonMap("dev-env", new EnvironmentImpl(createEnvDto())))
            .setCommands(singletonList(createCommandDto()))
            .setProjects(singletonList(createProjectDto()))
            .build();
    return asDto(config);
  }

  private ServerImpl createInternalServer() {
    return new ServerImpl()
        .withStatus(ServerStatus.UNKNOWN)
        .withUrl("http://localhost:7070")
        .withAttributes(
            singletonMap(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, Boolean.TRUE.toString()));
  }

  private ServerImpl createExternalServer() {
    return new ServerImpl()
        .withStatus(ServerStatus.UNKNOWN)
        .withUrl("http://localhost:7070")
        .withAttributes(
            singletonMap(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, Boolean.FALSE.toString()));
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent()
          .setSubject(new SubjectImpl(NAMESPACE, USER_ID, "token", false));
    }
  }
}
