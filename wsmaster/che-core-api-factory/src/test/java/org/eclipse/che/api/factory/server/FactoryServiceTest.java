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
package org.eclipse.che.api.factory.server;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.eclipse.che.api.factory.server.DtoConverter.asDto;
import static org.eclipse.che.api.factory.server.FactoryService.VALIDATE_QUERY_PARAMETER;
import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.factory.server.FactoryService.FactoryParametersResolverHolder;
import org.eclipse.che.api.factory.server.builder.FactoryBuilder;
import org.eclipse.che.api.factory.server.impl.SourceStorageParametersValidator;
import org.eclipse.che.api.factory.server.model.impl.AuthorImpl;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl.WorkspaceConfigImplBuilder;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl.WorkspaceImplBuilder;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link FactoryService}.
 *
 * @author Anton Korneta
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class FactoryServiceTest {

  private static final String SERVICE_PATH = "/factory";
  private static final String FACTORY_ID = "correctFactoryId";
  private static final String FACTORY_NAME = "factory";
  private static final String USER_ID = "userId";
  private static final String USER_EMAIL = "email";
  private static final String WORKSPACE_NAME = "workspace";
  private static final String PROJECT_SOURCE_TYPE = "git";
  private static final String PROJECT_SOURCE_LOCATION =
      "https://github.com/codenvy/platform-api.git";

  private static final DtoFactory DTO = DtoFactory.getInstance();

  @Mock private FactoryManager factoryManager;
  @Mock private FactoryCreateValidator createValidator;
  @Mock private FactoryAcceptValidator acceptValidator;
  @Mock private PreferenceManager preferenceManager;
  @Mock private UserManager userManager;
  @Mock private FactoryEditValidator editValidator;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private DefaultFactoryParameterResolver defaultFactoryParameterResolver;

  @InjectMocks private FactoryParametersResolverHolder factoryParametersResolverHolder;

  private FactoryBuilder factoryBuilderSpy;

  private User user;

  private FactoryService service;

  @SuppressWarnings("unused")
  private ApiExceptionMapper apiExceptionMapper;

  @SuppressWarnings("unused")
  private EnvironmentFilter environmentFilter;

  @BeforeMethod
  public void setUp() throws Exception {
    factoryBuilderSpy = spy(new FactoryBuilder(new SourceStorageParametersValidator()));
    lenient().doNothing().when(factoryBuilderSpy).checkValid(any(FactoryDto.class));
    lenient().doNothing().when(factoryBuilderSpy).checkValid(any(FactoryDto.class), anyBoolean());
    user = new UserImpl(USER_ID, USER_EMAIL, ADMIN_USER_NAME);
    lenient().when(userManager.getById(anyString())).thenReturn(user);
    lenient()
        .when(preferenceManager.find(USER_ID))
        .thenReturn(ImmutableMap.of("preference", "value"));
    service =
        new FactoryService(
            factoryManager,
            userManager,
            preferenceManager,
            createValidator,
            acceptValidator,
            editValidator,
            factoryBuilderSpy,
            workspaceManager,
            factoryParametersResolverHolder);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext context = EnvironmentContext.getCurrent();
      context.setSubject(new SubjectImpl(ADMIN_USER_NAME, USER_ID, ADMIN_USER_PASSWORD, false));
    }
  }

  @Test
  public void shouldSaveFactory() throws Exception {
    final Factory factory = createFactory();
    final FactoryDto factoryDto = asDto(factory, user);
    when(factoryManager.saveFactory(any(FactoryDto.class))).thenReturn(factory);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(ContentType.JSON)
            .body(factoryDto)
            .expect()
            //                                         .statusCode(200)
            .post(SERVICE_PATH);
    assertEquals(getFromResponse(response, FactoryDto.class).withLinks(emptyList()), factoryDto);
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenFactoryConfigurationNotProvided() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(ContentType.JSON)
            .expect()
            .statusCode(400)
            .post(SERVICE_PATH);
    final String errMessage = getFromResponse(response, ServiceError.class).getMessage();
    assertEquals(errMessage, "Factory configuration required");
  }

  @Test
  public void shouldReturnFactoryByIdentifierWithoutValidation() throws Exception {
    final Factory factory = createFactory();
    final FactoryDto factoryDto = asDto(factory, user);
    when(factoryManager.getById(FACTORY_ID)).thenReturn(factory);

    final Response response =
        given()
            .when()
            //                                         .expect()
            //                                         .statusCode(200)
            .get(SERVICE_PATH + "/" + FACTORY_ID);

    assertEquals(getFromResponse(response, FactoryDto.class).withLinks(emptyList()), factoryDto);
  }

  @Test
  public void shouldReturnFactoryByIdentifierWithValidation() throws Exception {
    final Factory factory = createFactory();
    final FactoryDto factoryDto = asDto(factory, user);
    when(factoryManager.getById(FACTORY_ID)).thenReturn(factory);
    doNothing().when(acceptValidator).validateOnAccept(any(FactoryDto.class));

    final Response response =
        given()
            .when()
            .expect()
            .statusCode(200)
            .get(SERVICE_PATH + "/" + FACTORY_ID + "?validate=true");

    assertEquals(getFromResponse(response, FactoryDto.class).withLinks(emptyList()), factoryDto);
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenFactoryIsNotExist() throws Exception {
    final String errMessage = format("Factory with id %s is not found", FACTORY_ID);
    doThrow(new NotFoundException(errMessage)).when(factoryManager).getById(anyString());

    final Response response =
        given().expect().statusCode(404).when().get(SERVICE_PATH + "/" + FACTORY_ID);

    assertEquals(getFromResponse(response, ServiceError.class).getMessage(), errMessage);
  }

  @Test
  public void shouldReturnFactoryListByNameAttribute() throws Exception {
    final FactoryImpl factory = createFactory();
    doReturn(new Page<>(ImmutableList.of(factory), 0, 1, 1))
        .when(factoryManager)
        .getByAttribute(1, 0, ImmutableList.of(Pair.of("name", factory.getName())));
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .when()
            .expect()
            .statusCode(200)
            .get(SERVICE_PATH + "/find?maxItems=1&skipCount=0&name=" + factory.getName());

    final List<FactoryDto> res = unwrapDtoList(response, FactoryDto.class);
    assertEquals(res.size(), 1);
    assertEquals(res.get(0).withLinks(emptyList()), asDto(factory, user));
  }

  @Test
  public void shouldFailToReturnFactoryListByWrongAttribute() {

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .when()
            .expect()
            .statusCode(400)
            .get(SERVICE_PATH + "/find?maxItems=1&skipCount=0&strange=edrer");
  }

  @Test
  public void shouldReturnFactoryListByCreatorAttribute() throws Exception {
    final FactoryImpl factory1 = createNamedFactory("factory1");
    final FactoryImpl factory2 = createNamedFactory("factory2");
    doReturn(new Page<>(ImmutableList.of(factory1, factory2), 0, 2, 2))
        .when(factoryManager)
        .getByAttribute(2, 0, ImmutableList.of(Pair.of("creator.userId", user.getName())));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .when()
            .expect()
            .statusCode(200)
            .get(SERVICE_PATH + "/find?maxItems=2&skipCount=0&creator.userId=" + user.getName());

    final Set<FactoryDto> res =
        unwrapDtoList(response, FactoryDto.class)
            .stream()
            .map(f -> f.withLinks(emptyList()))
            .collect(toSet());
    assertEquals(res.size(), 2);
    assertTrue(res.containsAll(ImmutableList.of(asDto(factory1, user), asDto(factory2, user))));
  }

  @Test
  public void shouldThrowBadRequestWhenGettingFactoryByEmptyAttributeList() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .when()
            .expect()
            .get(SERVICE_PATH + "/find?maxItems=1&skipCount=0");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(
        DTO.createDtoFromJson(response.getBody().asString(), ServiceError.class).getMessage(),
        "Query must contain at least one attribute");
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenUpdatingNonExistingFactory() throws Exception {
    final Factory factory =
        createFactoryWithStorage(
            FACTORY_NAME, "git", "https://github.com/codenvy/platform-api.git");
    doThrow(new NotFoundException(format("Factory with id %s is not found.", FACTORY_ID)))
        .when(factoryManager)
        .getById(anyString());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .body(JsonHelper.toJson(factory))
            .when()
            .put(SERVICE_PATH + "/" + FACTORY_ID);

    assertEquals(response.getStatusCode(), 404);
    assertEquals(
        DTO.createDtoFromJson(response.getBody().asString(), ServiceError.class).getMessage(),
        format("Factory with id %s is not found.", FACTORY_ID));
  }

  @Test
  public void shouldNotBeAbleToUpdateANullFactory() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .when()
            .put(SERVICE_PATH + "/" + FACTORY_ID);

    assertEquals(response.getStatusCode(), 400);
    assertEquals(
        DTO.createDtoFromJson(response.getBody().asString(), ServiceError.class).getMessage(),
        "Factory configuration required");
  }

  @Test
  public void shouldRemoveFactoryByGivenIdentifier() throws Exception {
    final Factory factory = createFactory();
    when(factoryManager.getById(FACTORY_ID)).thenReturn(factory);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .param("id", FACTORY_ID)
        .expect()
        .statusCode(204)
        .when()
        .delete(SERVICE_PATH + "/" + FACTORY_ID);

    verify(factoryManager).removeFactory(FACTORY_ID);
  }

  @Test
  public void shouldNotThrowAnyExceptionWhenRemovingNonExistingFactory() throws Exception {
    doNothing().when(factoryManager).removeFactory(anyString());

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .param("id", FACTORY_ID)
            .when()
            .delete(SERVICE_PATH + "/" + FACTORY_ID);

    assertEquals(response.getStatusCode(), 204);
  }

  @Test
  public void shouldGenerateFactoryJsonIncludeGivenProjects() throws Exception {
    // given
    final String wsId = "workspace123234";
    WorkspaceImplBuilder ws = WorkspaceImpl.builder();
    WorkspaceConfigImplBuilder wsConfig = WorkspaceConfigImpl.builder();
    ws.setId(wsId);
    wsConfig.setProjects(
        Arrays.asList(
            newDto(ProjectConfigDto.class)
                .withPath("/proj1")
                .withSource(
                    newDto(SourceStorageDto.class).withType("git").withLocation("location")),
            newDto(ProjectConfigDto.class)
                .withPath("/proj2")
                .withSource(
                    newDto(SourceStorageDto.class).withType("git").withLocation("location"))));
    wsConfig.setName("wsname");
    wsConfig.setEnvironments(singletonMap("env1", newDto(EnvironmentDto.class)));
    wsConfig.setDefaultEnv("env1");
    ws.setStatus(WorkspaceStatus.RUNNING);
    wsConfig.setCommands(
        singletonList(
            newDto(CommandDto.class)
                .withName("MCI")
                .withType("mvn")
                .withCommandLine("clean install")));
    ws.setConfig(wsConfig.build());
    WorkspaceImpl usersWorkspace = ws.build();
    when(workspaceManager.getWorkspace(eq(wsId))).thenReturn(usersWorkspace);

    // when
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get("/private" + SERVICE_PATH + "/workspace/" + wsId);

    // then
    assertEquals(response.getStatusCode(), 200);
    FactoryDto result = DTO.createDtoFromJson(response.getBody().asString(), FactoryDto.class);
    assertEquals(result.getWorkspace().getProjects().size(), 2);
  }

  @Test
  public void shouldNotGenerateFactoryIfNoProjectsWithSourceStorage() throws Exception {
    // given
    final String wsId = "workspace123234";
    WorkspaceImplBuilder ws = WorkspaceImpl.builder();
    WorkspaceConfigImplBuilder wsConfig = WorkspaceConfigImpl.builder();
    ws.setId(wsId);
    wsConfig.setProjects(
        Arrays.asList(
            newDto(ProjectConfigDto.class).withPath("/proj1"),
            newDto(ProjectConfigDto.class).withPath("/proj2")));
    wsConfig.setName("wsname");
    wsConfig.setEnvironments(singletonMap("env1", newDto(EnvironmentDto.class)));
    wsConfig.setDefaultEnv("env1");
    ws.setStatus(WorkspaceStatus.RUNNING);
    wsConfig.setCommands(
        singletonList(
            newDto(CommandDto.class)
                .withName("MCI")
                .withType("mvn")
                .withCommandLine("clean install")));
    ws.setConfig(wsConfig.build());

    WorkspaceImpl usersWorkspace = ws.build();
    when(workspaceManager.getWorkspace(eq(wsId))).thenReturn(usersWorkspace);

    // when
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SERVICE_PATH + "/workspace/" + wsId);

    // then
    assertEquals(response.getStatusCode(), BAD_REQUEST.getStatusCode());
  }

  /** Checks that the user can remove an existing factory */
  @Test
  public void shouldBeAbleToRemoveFactory() throws Exception {
    final Factory factory = createFactory();
    when(factoryManager.getById(FACTORY_ID)).thenReturn(factory);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .param("id", FACTORY_ID)
            .when()
            .delete(SERVICE_PATH + "/" + FACTORY_ID);

    assertEquals(response.getStatusCode(), 204);

    // check there was a call on the remove operation with expected ID
    verify(factoryManager).removeFactory(FACTORY_ID);
  }

  @Test
  public void shouldNotThrowExceptionWhenRemoveNoExistingFactory() throws Exception {
    doNothing().when(factoryManager).removeFactory(FACTORY_ID);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .param("id", FACTORY_ID)
            .when()
            .delete(SERVICE_PATH + "/" + FACTORY_ID);

    assertEquals(response.getStatusCode(), 204);
  }

  @Test
  public void checkValidateResolver() throws Exception {
    final FactoryParametersResolverHolder dummyHolder = spy(factoryParametersResolverHolder);
    doReturn(defaultFactoryParameterResolver)
        .when(dummyHolder)
        .getFactoryParametersResolver(anyMap());
    // service instance with dummy holder
    service =
        new FactoryService(
            factoryManager,
            userManager,
            preferenceManager,
            createValidator,
            acceptValidator,
            editValidator,
            factoryBuilderSpy,
            workspaceManager,
            dummyHolder);

    // invalid factory
    final String invalidFactoryMessage = "invalid factory";
    doThrow(new BadRequestException(invalidFactoryMessage))
        .when(acceptValidator)
        .validateOnAccept(any());

    // create factory
    final FactoryDto expectFactory =
        newDto(FactoryDto.class).withV(CURRENT_VERSION).withName("matchingResolverFactory");

    // accept resolver
    when(defaultFactoryParameterResolver.createFactory(anyMap())).thenReturn(expectFactory);

    // when
    final Map<String, String> map = new HashMap<>();
    final Response response =
        given()
            .contentType(ContentType.JSON)
            .when()
            .body(map)
            .queryParam(VALIDATE_QUERY_PARAMETER, valueOf(true))
            .post(SERVICE_PATH + "/resolver");

    // then check we have a bad request
    assertEquals(response.getStatusCode(), BAD_REQUEST.getStatusCode());
    assertTrue(response.getBody().asString().contains(invalidFactoryMessage));

    // check we call resolvers
    dummyHolder.getFactoryParametersResolver(anyMap());
    verify(defaultFactoryParameterResolver).createFactory(anyMap());

    // check we call validator
    verify(acceptValidator).validateOnAccept(any());
  }

  @Test
  public void shouldThrowBadRequestWhenNoURLParameterGiven() throws Exception {
    // when
    final Map<String, String> map = new HashMap<>();
    final Response response =
        given()
            .contentType(ContentType.JSON)
            .when()
            .body(map)
            .queryParam(VALIDATE_QUERY_PARAMETER, valueOf(true))
            .post(SERVICE_PATH + "/resolver");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(
        DTO.createDtoFromJson(response.getBody().asString(), ServiceError.class).getMessage(),
        "Cannot build factory with any of the provided parameters. Please check parameters correctness, and resend query.");
  }

  private FactoryImpl createFactory() {
    return createNamedFactory(FACTORY_NAME);
  }

  private FactoryImpl createNamedFactory(String name) {
    return createFactoryWithStorage(name, PROJECT_SOURCE_TYPE, PROJECT_SOURCE_LOCATION);
  }

  private FactoryImpl createFactoryWithStorage(String name, String type, String location) {
    return FactoryImpl.builder()
        .setId(FACTORY_ID)
        .setVersion("4.0")
        .setWorkspace(createWorkspaceConfig(type, location))
        .setCreator(new AuthorImpl(USER_ID, 12L))
        .setName(name)
        .build();
  }

  private static WorkspaceConfig createWorkspaceConfig(String type, String location) {
    return WorkspaceConfigImpl.builder()
        .setName(WORKSPACE_NAME)
        .setEnvironments(singletonMap("env1", new EnvironmentImpl(createEnvDto())))
        .setProjects(createProjects(type, location))
        .build();
  }

  private static EnvironmentDto createEnvDto() {
    final RecipeImpl environmentRecipe = new RecipeImpl();
    environmentRecipe.setType("type");
    environmentRecipe.setContent("content");
    environmentRecipe.setContentType("compose");
    environmentRecipe.setLocation("location");
    final EnvironmentImpl env = new EnvironmentImpl();
    final MachineConfigImpl extendedMachine = new MachineConfigImpl();
    extendedMachine.setInstallers(singletonList("agent"));
    extendedMachine.setAttributes(singletonMap("att1", "value"));
    extendedMachine.setServers(
        singletonMap(
            "agent", new ServerConfigImpl("5555", "https", "path", singletonMap("key", "value"))));
    env.setRecipe(environmentRecipe);
    env.setMachines(singletonMap("machine1", extendedMachine));
    return org.eclipse.che.api.workspace.server.DtoConverter.asDto(env);
  }

  private static List<ProjectConfig> createProjects(String type, String location) {
    final ProjectConfigImpl projectConfig = new ProjectConfigImpl();
    projectConfig.setSource(new SourceStorageImpl(type, location, null));
    return ImmutableList.of(projectConfig);
  }

  private static <T> T getFromResponse(Response response, Class<T> clazz) throws Exception {
    return DTO.createDtoFromJson(response.getBody().asInputStream(), clazz);
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass)
      throws IOException {
    return new ArrayList<>(
        DtoFactory.getInstance().createListDtoFromJson(response.body().asInputStream(), dtoClass));
  }
}
