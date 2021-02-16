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
package org.eclipse.che.multiuser.permission.devfile.server.filters;

import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.api.workspace.server.devfile.Constants.CURRENT_API_VERSION;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.Collections;
import java.util.HashSet;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.devfile.server.DevfileService;
import org.eclipse.che.api.devfile.server.UserDevfileManager;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;
import org.eclipse.che.api.workspace.server.devfile.DevfileEntityProvider;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.devfile.DevfileVersionDetector;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.permission.devfile.server.TestObjectGenerator;
import org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link UserDevfilePermissionsFilter}. */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class UserDevfilePermissionsFilterTest {
  private static final String USERNAME = "userok";

  ApiExceptionMapper mapper;

  CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());
  private DevfileEntityProvider devfileEntityProvider =
      new DevfileEntityProvider(
          new DevfileParser(
              new DevfileSchemaValidator(new DevfileSchemaProvider(), new DevfileVersionDetector()),
              new DevfileIntegrityValidator(Collections.emptyMap())));

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private static Subject subject;

  @Mock private UserDevfileManager userDevfileManager;

  private UserDevfilePermissionsFilter permissionsFilter;

  @Mock private DevfileService devfileService;
  private UserDevfileDto userDevfileDto = TestObjectGenerator.createUserDevfileDto();
  private UserDevfileImpl userDevfile =
      new UserDevfileImpl(userDevfileDto, TestObjectGenerator.TEST_ACCOUNT);
  //
  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(subject.getUserName()).thenReturn(USERNAME);
    lenient().when(userDevfileManager.getById(any())).thenReturn(userDevfile);

    permissionsFilter = spy(new UserDevfilePermissionsFilter(userDevfileManager));

    lenient()
        .doThrow(new ForbiddenException(""))
        .when(subject)
        .checkPermission(anyString(), anyString(), anyString());
  }

  @Test
  public void shouldNotCheckAnyPermissionOnDevfileCreate() {
    // given
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(userDevfileDto)
            .when()
            .post(SECURE_PATH + "/devfile/");
    // then
    assertEquals(response.getStatusCode(), 204);
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldNotCheckAnyPermissionOnDevfileSearch()
      throws BadRequestException, ForbiddenException, NotFoundException, ServerException {
    // given
    Mockito.when(devfileService.getUserDevfiles(any(), any(), any()))
        .thenReturn(javax.ws.rs.core.Response.ok().build());
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile/search");
    // then
    assertEquals(response.getStatusCode(), 200);
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldNotCheckAnyPermissionOnDevfileSchema()
      throws NotFoundException, ServerException {
    // given
    Mockito.when(devfileService.getSchema(CURRENT_API_VERSION))
        .thenReturn(javax.ws.rs.core.Response.ok().build());
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile");
    // then
    assertEquals(response.getStatusCode(), 200);
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldCheckReadPermissionsOnFetchingUserDevfileById() throws Exception {
    // given
    Mockito.when(devfileService.getById(eq(userDevfileDto.getId()))).thenReturn(userDevfileDto);
    doNothing()
        .when(subject)
        .checkPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.READ));

    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/devfile/" + userDevfileDto.getId());
    // then
    assertEquals(response.getStatusCode(), 200);
    verify(devfileService).getById(eq(userDevfileDto.getId()));
    verify(permissionsFilter)
        .doCheckPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.READ));
  }

  @Test
  public void shouldBeAbleToFailOnCheckPermissionDevfileReadByID() throws ForbiddenException {
    // given
    doThrow(new ForbiddenException("forbidden"))
        .when(subject)
        .checkPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.READ));
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile/" + userDevfileDto.getId());
    // then
    assertEquals(response.getStatusCode(), 403);
    verify(permissionsFilter)
        .doCheckPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.READ));
  }

  @Test
  public void shouldChecksPermissionDevfileUpdate() throws ForbiddenException {
    // given
    doNothing()
        .when(subject)
        .checkPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.UPDATE));
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(userDevfileDto)
            .when()
            .put(SECURE_PATH + "/devfile/" + userDevfileDto.getId());
    // then
    assertEquals(response.getStatusCode(), 204);
    verify(permissionsFilter)
        .doCheckPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.UPDATE));
  }

  @Test
  public void shouldBeAbleToFailOnCheckPermissionDevfileUpdate() throws ForbiddenException {
    // given
    doThrow(new ForbiddenException("forbidden"))
        .when(subject)
        .checkPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.UPDATE));
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(userDevfileDto)
            .when()
            .put(SECURE_PATH + "/devfile/" + userDevfileDto.getId());
    // then
    assertEquals(response.getStatusCode(), 403);
    verify(permissionsFilter)
        .doCheckPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.UPDATE));
  }

  @Test
  public void shouldChecksPermissionDevfileDelete() throws ForbiddenException {
    // given
    doNothing()
        .when(subject)
        .checkPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.DELETE));
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/devfile/" + userDevfileDto.getId());
    // then
    assertEquals(response.getStatusCode(), 204);
    verify(permissionsFilter)
        .doCheckPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.DELETE));
  }

  @Test
  public void shouldBeAbleToFailOnCheckPermissionDevfileDelete() throws ForbiddenException {
    // given
    doThrow(new ForbiddenException("forbidden"))
        .when(subject)
        .checkPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.DELETE));
    // when
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/devfile/" + userDevfileDto.getId());

    // then
    assertEquals(response.getStatusCode(), 403);
    verify(permissionsFilter)
        .doCheckPermission(
            eq(UserDevfileDomain.DOMAIN_ID),
            eq(userDevfileDto.getId()),
            eq(UserDevfileDomain.DELETE));
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
