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
package org.eclipse.che.api.devfile.server;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.workspace.server.devfile.Constants.CURRENT_API_VERSION;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;
import org.eclipse.che.api.workspace.server.devfile.DevfileEntityProvider;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class DevfileServiceTest {

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private ApiExceptionMapper exceptionMapper;

  private DevfileSchemaProvider schemaProvider = new DevfileSchemaProvider();

  private static final Subject SUBJECT = new SubjectImpl("user", "user123", "token", false);

  private static final String CURRENT_USER_ID = "user123";

  private final String USER_DEVFILE_ID = NameGenerator.generate("usrd", 16);

  CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());
  private DevfileEntityProvider devfileEntityProvider =
      new DevfileEntityProvider(
          new DevfileParser(
              new DevfileSchemaValidator(new DevfileSchemaProvider()),
              new DevfileIntegrityValidator(Collections.emptyMap())));
  @Mock UserDevfileDao userDevfileDao;
  @Mock UserDevfileManager userDevfileManager;
  @Mock EventService eventService;
  @Mock DevfileServiceLinksInjector linksInjector;

  DevfileService userDevfileService;

  @Test
  public void shouldRetrieveSchema() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile");

    assertEquals(response.getStatusCode(), 200);
    assertEquals(
        response.getBody().asString(), schemaProvider.getSchemaContent(CURRENT_API_VERSION));
  }

  @BeforeMethod
  public void setup() {
    this.userDevfileService = new DevfileService(schemaProvider, userDevfileManager, linksInjector);
    lenient()
        .when(linksInjector.injectLinks(any(UserDevfileDto.class), any(ServiceContext.class)))
        .thenAnswer((Answer<UserDevfileDto>) invocation -> invocation.getArgument(0));
  }

  @Test
  public void shouldCreateUserDevfile() throws Exception {
    final DevfileDto devfileDto =
        org.eclipse.che.api.workspace.server.DtoConverter.asDto(
            TestObjectGenerator.createDevfile("devfile-name"));
    final UserDevfileImpl userDevfileImpl = new UserDevfileImpl(null, devfileDto);

    when(userDevfileManager.createDevfile(any(Devfile.class))).thenReturn(userDevfileImpl);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(devfileDto)
            .when()
            .post(SECURE_PATH + "/devfile");

    assertEquals(response.getStatusCode(), 201);
    assertEquals(new UserDevfileImpl(unwrapDto(response, UserDevfileDto.class)), userDevfileImpl);
    verify(userDevfileManager).createDevfile(any(Devfile.class));
  }

  @Test
  public void shouldGetUserDevfileById() throws Exception {
    final UserDevfileImpl userDevfile = TestObjectGenerator.createUserDevfile();
    when(userDevfileManager.getById(eq("id-22323"))).thenReturn(userDevfile);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/devfile/id-22323");

    assertEquals(new UserDevfileImpl(unwrapDto(response, UserDevfileDto.class)), userDevfile);
    verify(userDevfileManager).getById(eq("id-22323"));
    verify(linksInjector).injectLinks(any(), any());
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenUserDevfileIsNotExistOnGetById() throws Exception {

    final String errMessage = format("UserDevfile with id %s is not found", USER_DEVFILE_ID);
    doThrow(new NotFoundException(errMessage)).when(userDevfileManager).getById(anyString());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .expect()
            .statusCode(404)
            .when()
            .get(SECURE_PATH + "/devfile/" + USER_DEVFILE_ID);

    assertEquals(unwrapDto(response, ServiceError.class).getMessage(), errMessage);
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenUpdatingNonExistingUserDevfile() throws Exception {
    // given
    final DevfileDto devfileDto =
        org.eclipse.che.api.workspace.server.DtoConverter.asDto(
            TestObjectGenerator.createDevfile("devfile-name"));

    doThrow(new NotFoundException(format("User devfile with id %s is not found.", USER_DEVFILE_ID)))
        .when(userDevfileManager)
        .updateUserDevfile(any(UserDevfile.class));
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .body(JsonHelper.toJson(devfileDto))
            .when()
            .put(SECURE_PATH + "/devfile/" + USER_DEVFILE_ID);
    // then
    assertEquals(response.getStatusCode(), 404);
    assertEquals(
        unwrapDto(response, ServiceError.class).getMessage(),
        format("User devfile with id %s is not found.", USER_DEVFILE_ID));
  }

  @Test
  public void shouldBeAbleToUpdateUserDevfile() throws Exception {
    // given
    final UserDevfileDto devfileDto = TestObjectGenerator.createUserDevfileDto();
    final UserDevfileImpl userDevfileImpl = new UserDevfileImpl(devfileDto);
    when(userDevfileManager.updateUserDevfile(any(UserDevfile.class))).thenReturn(userDevfileImpl);

    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .body(JsonHelper.toJson(devfileDto))
            .when()
            .put(SECURE_PATH + "/devfile/" + devfileDto.getId());
    // then
    assertEquals(response.getStatusCode(), 200);
    assertEquals(new UserDevfileImpl(unwrapDto(response, UserDevfileDto.class)), userDevfileImpl);
    verify(userDevfileManager).updateUserDevfile(devfileDto);
    verify(linksInjector).injectLinks(any(), any());
  }

  @Test
  public void shouldOverrideIdOnUpdateUserDevfile() throws Exception {
    // given
    final UserDevfileDto devfileDto = TestObjectGenerator.createUserDevfileDto();
    final UserDevfileImpl userDevfileImpl = new UserDevfileImpl(devfileDto);

    final String newID = NameGenerator.generate("id", 24);
    final UserDevfileImpl expectedUserDevfileImpl = new UserDevfileImpl(newID, userDevfileImpl);
    final UserDevfileDto expectedDto =
        org.eclipse.che.api.devfile.server.DtoConverter.asDto(expectedUserDevfileImpl);
    when(userDevfileManager.updateUserDevfile(any(UserDevfile.class)))
        .thenReturn(expectedUserDevfileImpl);
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(APPLICATION_JSON)
            .body(JsonHelper.toJson(devfileDto))
            .when()
            .put(SECURE_PATH + "/devfile/" + newID);
    // then
    assertEquals(response.getStatusCode(), 200);
    assertEquals(
        new UserDevfileImpl(unwrapDto(response, UserDevfileDto.class)), expectedUserDevfileImpl);
    verify(userDevfileManager).updateUserDevfile(expectedDto);
    verify(linksInjector).injectLinks(any(), any());
  }

  @Test
  public void shouldRemoveUserDevfileByGivenIdentifier() throws Exception {
    // given
    // when
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(204)
        .when()
        .delete(SECURE_PATH + "/devfile/" + USER_DEVFILE_ID);
    // then
    verify(userDevfileManager).removeUserDevfile(USER_DEVFILE_ID);
  }

  @Test
  public void shouldNotThrowAnyExceptionWhenRemovingNonExistingUserDevfile() throws Exception {
    // given
    Mockito.doNothing().when(userDevfileManager).removeUserDevfile(anyString());
    // when
    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/devfile/" + USER_DEVFILE_ID);
    // then
    assertEquals(response.getStatusCode(), 204);
  }

  @Test
  public void shouldGetUserDevfilesAvailableToUser() throws Exception {
    // given
    final UserDevfileDto devfileDto = TestObjectGenerator.createUserDevfileDto();
    final UserDevfileImpl userDevfileImpl = new UserDevfileImpl(devfileDto);
    doReturn(new Page<>(ImmutableList.of(userDevfileImpl), 0, 1, 1))
        .when(userDevfileManager)
        .getUserDevfiles(anyInt(), anyInt(), anyList(), anyList());

    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/devfile/list");
    // then
    final List<UserDevfileDto> res = unwrapDtoList(response, UserDevfileDto.class);
    assertEquals(res.size(), 1);
    assertEquals(res.get(0).withLinks(emptyList()), devfileDto);
    verify(userDevfileManager).getUserDevfiles(eq(30), eq(0), anyList(), anyList());
  }

  @Test
  public void shouldBeAbleToSetLimitAndOffsetOnUserDevfileSearch() throws Exception {
    // given
    doReturn(new Page<>(Collections.emptyList(), 0, 1, 0))
        .when(userDevfileManager)
        .getUserDevfiles(anyInt(), anyInt(), anyList(), anyList());
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .queryParam("maxItems", 5)
            .queryParam("skipCount", 52)
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/devfile/list");
    // then
    verify(userDevfileManager).getUserDevfiles(eq(5), eq(52), anyList(), anyList());
  }

  @Test
  public void shouldBeAbleToSetFiltertOnUserDevfileSearch() throws Exception {
    // given
    doReturn(new Page<>(Collections.emptyList(), 0, 1, 0))
        .when(userDevfileManager)
        .getUserDevfiles(anyInt(), anyInt(), anyList(), anyList());
    Map<String, String> parameters =
        ImmutableMap.of("id", "sdfsdf5", "devfile.meta.name", "like:%dfdf");
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .queryParameter("id", "sdfsdf5")
            .queryParameter("devfile.meta.name", "like:%dfdf")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/devfile/list");
    // then
    Class<List<Pair<String, String>>> listClass =
        (Class<List<Pair<String, String>>>) (Class) ArrayList.class;
    ArgumentCaptor<List<Pair<String, String>>> filterCaptor = ArgumentCaptor.forClass(listClass);
    verify(userDevfileManager).getUserDevfiles(eq(30), eq(0), filterCaptor.capture(), anyList());
    assertEquals(
        filterCaptor.getValue(),
        ImmutableList.of(new Pair("devfile.meta.name", "like:%dfdf"), new Pair("id", "sdfsdf5")));
  }

  @Test
  public void shouldBeAbleToSetOrderOnUserDevfileSearch() throws Exception {
    // given
    doReturn(new Page<>(Collections.emptyList(), 0, 1, 0))
        .when(userDevfileManager)
        .getUserDevfiles(anyInt(), anyInt(), anyList(), anyList());
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .queryParameter("order", "id:asc,name:desc")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/devfile/list");
    // then
    Class<List<Pair<String, String>>> listClass =
        (Class<List<Pair<String, String>>>) (Class) ArrayList.class;
    ArgumentCaptor<List<Pair<String, String>>> orderCaptor = ArgumentCaptor.forClass(listClass);
    verify(userDevfileManager).getUserDevfiles(eq(30), eq(0), anyList(), orderCaptor.capture());
    assertEquals(
        orderCaptor.getValue(), ImmutableList.of(new Pair("id", "asc"), new Pair("name", "desc")));
  }

  @Test
  public void shouldNotAllowToGetMoreWhen60Items() throws Exception {
    // given
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .queryParam("maxItems", 61)
            .when()
            .expect()
            .statusCode(400)
            .get(SECURE_PATH + "/devfile/list");

    // then
    assertEquals(
        unwrapDto(response, ServiceError.class).getMessage(),
        "Maximum allowed page size is 60 but requested 61");
  }

  private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createDtoFromJson(response.asString(), dtoClass);
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass)
      throws IOException {
    return new ArrayList<>(
        DtoFactory.getInstance().createListDtoFromJson(response.body().asInputStream(), dtoClass));
  }
}
