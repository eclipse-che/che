/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import static java.lang.String.valueOf;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.eclipse.che.api.factory.server.FactoryService.VALIDATE_QUERY_PARAMETER;
import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
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
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.commons.env.EnvironmentContext;
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
    service = new FactoryService(userManager, acceptValidator, factoryParametersResolverHolder);
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

  @Test
  public void checkValidateResolver() throws Exception {
    final FactoryParametersResolverHolder dummyHolder = spy(factoryParametersResolverHolder);
    doReturn(defaultFactoryParameterResolver)
        .when(dummyHolder)
        .getFactoryParametersResolver(anyMap());
    // service instance with dummy holder
    service = new FactoryService(userManager, acceptValidator, dummyHolder);

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
