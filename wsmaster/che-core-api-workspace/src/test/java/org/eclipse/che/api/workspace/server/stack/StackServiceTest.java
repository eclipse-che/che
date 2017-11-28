/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.stack;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_STACK_BY_ID;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_REMOVE_STACK;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackSourceImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.api.workspace.shared.stack.StackComponent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link @StackService}
 *
 * @author Alexander Andrienko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class StackServiceTest {

  private static final String STACK_ID = "java-default";
  private static final String NAME = "Java";
  private static final String DESCRIPTION = "Default Java Stack with JDK 8, Maven and Tomcat.";
  private static final String USER_ID = "che";
  private static final String CREATOR = USER_ID;
  private static final String FOREIGN_CREATOR = "foreign_creator";
  private static final String SCOPE = "general";

  private static final String SOURCE_TYPE = "image";
  private static final String SOURCE_ORIGIN = "codenvy/ubuntu_jdk8";

  private static final String COMPONENT_NAME = "Java";
  private static final String COMPONENT_VERSION = "1.8.0_45";

  private static final String WORKSPACE_CONFIG_NAME = "default";
  private static final String DEF_ENVIRONMENT_NAME = "default";

  private static final String COMMAND_NAME = "newMaven";
  private static final String COMMAND_TYPE = "mvn";
  private static final String COMMAND_LINE = "mvn clean install -f ${current.project.path}";

  private static final String ENVIRONMENT_NAME = "default";

  private static final String ICON_MEDIA_TYPE = "image/svg+xml";

  private static final String SVG_ICON =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
          + "<svg viewBox=\"0 0 200 200\" xmlns=\"http://www.w3.org/2000/svg\">\n"
          + "  <circle cx=\"100\" cy=\"100\" r=\"100\" fill=\"red\"/>\n"
          + "</svg>";

  @SuppressWarnings("unused")
  static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @SuppressWarnings("unused")
  static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  private List<String> tags = asList("java", "maven");
  private StackDto stackDto;
  private StackImpl stackImpl;
  private StackImpl foreignStack;
  private StackSourceImpl stackSourceImpl;
  private List<StackComponent> componentsImpl;
  private StackIcon stackIcon;

  private StackSourceDto stackSourceDto;
  private List<StackComponentDto> componentsDto;

  @Mock StackDao stackDao;

  @Mock UriInfo uriInfo;

  @Mock StackComponentImpl stackComponent;

  @Mock StackValidator validator;

  @InjectMocks StackService service;

  @BeforeMethod
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    byte[] fileContent = STACK_ID.getBytes();
    stackIcon = new StackIcon(ICON_MEDIA_TYPE, "image/svg+xml", fileContent);
    componentsImpl = singletonList(new StackComponentImpl(COMPONENT_NAME, COMPONENT_VERSION));
    stackSourceImpl = new StackSourceImpl(SOURCE_TYPE, SOURCE_ORIGIN);
    CommandImpl command = new CommandImpl(COMMAND_NAME, COMMAND_LINE, COMMAND_TYPE);
    EnvironmentImpl environment = new EnvironmentImpl(null, null, null);

    WorkspaceConfigImpl workspaceConfig =
        WorkspaceConfigImpl.builder()
            .setName(WORKSPACE_CONFIG_NAME)
            .setDefaultEnv(DEF_ENVIRONMENT_NAME)
            .setCommands(singletonList(command))
            .setEnvironments(singletonMap(ENVIRONMENT_NAME, environment))
            .build();

    stackSourceDto = newDto(StackSourceDto.class).withType(SOURCE_TYPE).withOrigin(SOURCE_ORIGIN);
    StackComponentDto stackComponentDto =
        newDto(StackComponentDto.class).withName(COMPONENT_NAME).withVersion(COMPONENT_VERSION);
    componentsDto = singletonList(stackComponentDto);

    stackDto =
        DtoFactory.getInstance()
            .createDto(StackDto.class)
            .withId(STACK_ID)
            .withName(NAME)
            .withDescription(DESCRIPTION)
            .withScope(SCOPE)
            .withCreator(CREATOR)
            .withTags(tags)
            .withSource(stackSourceDto)
            .withComponents(componentsDto);

    stackImpl =
        StackImpl.builder()
            .setId(STACK_ID)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setScope(SCOPE)
            .setCreator(CREATOR)
            .setTags(tags)
            .setSource(stackSourceImpl)
            .setComponents(componentsImpl)
            .setWorkspaceConfig(workspaceConfig)
            .setStackIcon(stackIcon)
            .build();

    foreignStack =
        StackImpl.builder()
            .setId(STACK_ID)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setScope(SCOPE)
            .setCreator(FOREIGN_CREATOR)
            .setTags(tags)
            .setSource(stackSourceImpl)
            .setComponents(componentsImpl)
            .setWorkspaceConfig(workspaceConfig)
            .setStackIcon(stackIcon)
            .build();

    when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

    final Field uriField = service.getClass().getSuperclass().getDeclaredField("uriInfo");
    uriField.setAccessible(true);
    uriField.set(service, uriInfo);
  }

  /** Create stack */
  @Test
  public void newStackShouldBeCreatedForUser() throws ConflictException, ServerException {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .body(stackDto)
            .when()
            .post(SECURE_PATH + "/stack");

    assertEquals(response.getStatusCode(), 201);

    verify(stackDao).create(any(StackImpl.class));

    final StackDto stackDtoDescriptor = unwrapDto(response, StackDto.class);

    assertEquals(stackDtoDescriptor.getName(), stackDto.getName());
    assertEquals(stackDtoDescriptor.getCreator(), USER_ID);
    assertEquals(stackDtoDescriptor.getDescription(), stackDto.getDescription());
    assertEquals(stackDtoDescriptor.getTags(), stackDto.getTags());

    assertEquals(stackDtoDescriptor.getComponents(), stackDto.getComponents());

    assertEquals(stackDtoDescriptor.getSource(), stackDto.getSource());

    assertEquals(stackDtoDescriptor.getScope(), stackDto.getScope());

    assertEquals(stackDtoDescriptor.getLinks().size(), 2);
    assertEquals(stackDtoDescriptor.getLinks().get(0).getRel(), LINK_REL_REMOVE_STACK);
    assertEquals(stackDtoDescriptor.getLinks().get(1).getRel(), LINK_REL_GET_STACK_BY_ID);
  }

  //    @Test
  //    public void shouldThrowBadRequestExceptionOnCreateStackWithEmptyBody() {
  //        final Response response = given().auth()
  //                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
  //                                         .contentType(APPLICATION_JSON)
  //                                         .when()
  //                                         .post(SECURE_PATH + "/stack");
  //
  //        assertEquals(response.getStatusCode(), 400);
  //        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Stack required");
  //    }

  //    @Test
  //    public void shouldThrowBadRequestExceptionOnCreateStackWithEmptyName() {
  //        StackComponentDto stackComponentDto =
  // newDto(StackComponentDto.class).withName("Java").withVersion("1.8.45");
  //        StackSourceDto stackSourceDto =
  // newDto(StackSourceDto.class).withType("image").withOrigin("codenvy/ubuntu_jdk8");
  //        StackDto stackDto = newDto(StackDto.class).withId(USER_ID)
  //                                                  .withDescription("")
  //                                                  .withScope("Simple java stack for generation
  // java projects")
  //                                                  .withTags(asList("java", "maven"))
  //                                                  .withCreator("che")
  //
  // .withComponents(singletonList(stackComponentDto))
  //                                                  .withSource(stackSourceDto);
  //
  //        Response response = given().auth()
  //                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
  //                                   .contentType(APPLICATION_JSON)
  //                                   .body(stackDto)
  //                                   .when()
  //                                   .post(SECURE_PATH + "/stack");
  //
  //        assertEquals(response.getStatusCode(), 400);
  //        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Stack name
  // required");
  //    }

  /** Get stack by id */
  @Test
  public void stackByIdShouldBeReturned() throws NotFoundException, ServerException {
    when(stackDao.getById(STACK_ID)).thenReturn(stackImpl);

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/stack/" + STACK_ID);

    assertEquals(response.getStatusCode(), 200);
    StackDto result = unwrapDto(response, StackDto.class);
    assertEquals(result.getId(), stackImpl.getId());
    assertEquals(result.getName(), stackImpl.getName());
    assertEquals(result.getDescription(), stackImpl.getDescription());
    assertEquals(result.getScope(), stackImpl.getScope());
    assertEquals(result.getTags().get(0), stackImpl.getTags().get(0));
    assertEquals(result.getTags().get(1), stackImpl.getTags().get(1));
    assertEquals(
        result.getComponents().get(0).getName(), stackImpl.getComponents().get(0).getName());
    assertEquals(
        result.getComponents().get(0).getVersion(), stackImpl.getComponents().get(0).getVersion());
    assertEquals(result.getSource().getType(), stackImpl.getSource().getType());
    assertEquals(result.getSource().getOrigin(), stackImpl.getSource().getOrigin());
    assertEquals(result.getCreator(), stackImpl.getCreator());
  }

  @Test
  public void stackShouldBeUpdated() throws NotFoundException, ServerException, ConflictException {
    final String updatedDescription = "some description";
    final String updatedScope = "advanced";
    StackDto updatedStackDto =
        DtoFactory.getInstance()
            .createDto(StackDto.class)
            .withId(STACK_ID)
            .withName(NAME)
            .withDescription(updatedDescription)
            .withScope(updatedScope)
            .withCreator(CREATOR)
            .withTags(tags)
            .withSource(stackSourceDto)
            .withComponents(componentsDto);

    StackImpl updateStack = new StackImpl(stackImpl);
    updateStack.setDescription(updatedDescription);
    updateStack.setScope(updatedScope);

    when(stackDao.getById(STACK_ID)).thenReturn(stackImpl).thenReturn(updateStack);
    when(stackDao.update(any())).thenReturn(updateStack).thenReturn(updateStack);

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .content(updatedStackDto)
            .when()
            .put(SECURE_PATH + "/stack/" + STACK_ID);

    assertEquals(response.getStatusCode(), 200);
    StackDto result = unwrapDto(response, StackDto.class);

    assertEquals(result.getId(), updatedStackDto.getId());
    assertEquals(result.getName(), updatedStackDto.getName());
    assertEquals(result.getDescription(), updatedStackDto.getDescription());
    assertEquals(result.getScope(), updatedStackDto.getScope());
    assertEquals(result.getTags().get(0), updatedStackDto.getTags().get(0));
    assertEquals(result.getTags().get(1), updatedStackDto.getTags().get(1));
    assertEquals(
        result.getComponents().get(0).getName(), updatedStackDto.getComponents().get(0).getName());
    assertEquals(
        result.getComponents().get(0).getVersion(),
        updatedStackDto.getComponents().get(0).getVersion());
    assertEquals(result.getSource().getType(), updatedStackDto.getSource().getType());
    assertEquals(result.getSource().getOrigin(), updatedStackDto.getSource().getOrigin());
    assertEquals(result.getCreator(), updatedStackDto.getCreator());

    verify(stackDao).update(any());
    verify(stackDao).getById(STACK_ID);
  }

  @Test
  public void creatorShouldNotBeUpdated()
      throws ServerException, NotFoundException, ConflictException {
    StackDto updatedStackDto =
        DtoFactory.getInstance()
            .createDto(StackDto.class)
            .withId(STACK_ID)
            .withName(NAME)
            .withDescription(DESCRIPTION)
            .withScope(SCOPE)
            .withCreator("creator changed")
            .withTags(tags)
            .withSource(stackSourceDto)
            .withComponents(componentsDto);

    when(stackDao.getById(anyString())).thenReturn(foreignStack);
    when(stackDao.update(any())).thenReturn(foreignStack);

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .content(updatedStackDto)
            .when()
            .put(SECURE_PATH + "/stack/" + STACK_ID);

    assertEquals(response.getStatusCode(), 200);
    StackDto result = unwrapDto(response, StackDto.class);

    assertEquals(result.getId(), updatedStackDto.getId());
    assertEquals(result.getName(), updatedStackDto.getName());
    assertEquals(result.getDescription(), updatedStackDto.getDescription());
    assertEquals(result.getScope(), updatedStackDto.getScope());
    assertEquals(result.getTags().get(0), updatedStackDto.getTags().get(0));
    assertEquals(result.getTags().get(1), updatedStackDto.getTags().get(1));
    assertEquals(
        result.getComponents().get(0).getName(), updatedStackDto.getComponents().get(0).getName());
    assertEquals(
        result.getComponents().get(0).getVersion(),
        updatedStackDto.getComponents().get(0).getVersion());
    assertEquals(result.getSource().getType(), updatedStackDto.getSource().getType());
    assertEquals(result.getSource().getOrigin(), updatedStackDto.getSource().getOrigin());
    assertEquals(result.getCreator(), FOREIGN_CREATOR);

    verify(stackDao).update(any());
    verify(stackDao).getById(STACK_ID);
  }

  /** Delete stack */
  @Test
  public void stackShouldBeDeleted() throws ServerException, NotFoundException {
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/stack/" + STACK_ID);

    verify(stackDao).remove(eq(STACK_ID));
    assertEquals(response.getStatusCode(), 204);
  }

  /** Search stack by tags */
  @Test
  public void shouldReturnsAllStacksWhenListTagsIsEmpty() throws ServerException {
    StackImpl stack2 = new StackImpl(stackImpl);
    stack2.setTags(singletonList("subversion"));
    List<StackImpl> stacks = asList(stackImpl, stack2);
    when(stackDao.searchStacks(anyString(), nullable(List.class), anyInt(), anyInt()))
        .thenReturn(stacks);

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/stack");

    assertEquals(response.getStatusCode(), 200);
    verify(stackDao).searchStacks(anyString(), nullable(List.class), anyInt(), anyInt());

    List<StackDto> result = unwrapListDto(response, StackDto.class);
    assertEquals(result.size(), 2);
    assertEquals(result.get(0).getName(), stackImpl.getName());
    assertEquals(result.get(1).getName(), stack2.getName());
  }

  @Test
  public void shouldReturnsStackByTagList() throws ServerException {
    StackImpl stack2 = new StackImpl(stackImpl);
    stack2.setTags(singletonList("Subversion"));
    when(stackDao.searchStacks(anyString(), eq(singletonList("Subversion")), anyInt(), anyInt()))
        .thenReturn(singletonList(stack2));

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/stack?tags=Subversion");

    assertEquals(response.getStatusCode(), 200);
    verify(stackDao).searchStacks(anyString(), eq(singletonList("Subversion")), anyInt(), anyInt());

    List<StackDto> result = unwrapListDto(response, StackDto.class);
    assertEquals(result.size(), 1);
    assertEquals(result.get(0).getName(), stack2.getName());
  }

  /** Get icon by stack id */
  @Test
  public void shouldReturnIconByStackId() throws NotFoundException, ServerException {
    when(stackDao.getById(stackImpl.getId())).thenReturn(stackImpl);

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/stack/" + stackImpl.getId() + "/icon");
    assertEquals(response.getStatusCode(), 200);

    verify(stackDao).getById(stackImpl.getId());
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenIconStackWasNotFound()
      throws NotFoundException, ServerException {
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/stack/" + stackImpl.getId() + "/icon");

    assertEquals(response.getStatusCode(), 404);
    String expectedErrorMessage = format("Stack with id '%s' was not found.", STACK_ID);
    assertEquals(unwrapDto(response, ServiceError.class).getMessage(), expectedErrorMessage);
    verify(stackDao).getById(stackImpl.getId());
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenIconWasNotFound()
      throws NotFoundException, ServerException {
    StackImpl test = new StackImpl(stackImpl);
    test.setStackIcon(null);
    when(stackDao.getById(test.getId())).thenReturn(test);

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/stack/" + stackImpl.getId() + "/icon");

    assertEquals(response.getStatusCode(), 404);
    String expectedErrorMessage = format("Image for stack with id '%s' was not found.", STACK_ID);
    assertEquals(unwrapDto(response, ServiceError.class).getMessage(), expectedErrorMessage);
    verify(stackDao).getById(test.getId());
  }

  /** Delete icon by stack id */
  @Test
  public void stackIconShouldBeDeletedForUserOwner()
      throws NotFoundException, ConflictException, ServerException {
    when(stackDao.getById(stackImpl.getId())).thenReturn(stackImpl);

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/stack/" + stackImpl.getId() + "/icon");

    assertEquals(response.getStatusCode(), 204);
    verify(stackDao).getById(stackImpl.getId());
    verify(stackDao).update(stackImpl);
  }

  /** Update stack icon */
  @Test
  public void stackIconShouldBeUploadedForUserOwner()
      throws NotFoundException, ConflictException, ServerException, URISyntaxException {
    when(stackDao.getById(stackImpl.getId())).thenReturn(stackImpl);

    checkUploadIcon(stackImpl);
  }

  @Test
  public void foreignStackIconShouldBeUploadedForUser()
      throws NotFoundException, ConflictException, ServerException {
    when(stackDao.getById(foreignStack.getId())).thenReturn(foreignStack);

    checkUploadIcon(foreignStack);
  }

  private void checkUploadIcon(Stack stack)
      throws NotFoundException, ServerException, ConflictException {
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .multiPart("type-java.svg", SVG_ICON, "image/svg+xml")
            .contentType(MULTIPART_FORM_DATA)
            .post(SECURE_PATH + "/stack/" + stackImpl.getId() + "/icon");

    assertEquals(response.getStatusCode(), 200);
    verify(stackDao).getById(foreignStack.getId());
    verify(stackDao).update(any());
  }

  private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
  }

  private static <T> List<T> unwrapListDto(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(new SubjectImpl("user", USER_ID, "token", false));
    }
  }
}
