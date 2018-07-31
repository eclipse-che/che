/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.user;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.logger.LoggerService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.permission.logger.LoggerServicePermissionsFilter;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.permission.logger.LoggerServicePermissionsFilter}
 *
 * @author Florent Benoit
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class LoggerServicePermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  LoggerServicePermissionsFilter permissionsFilter;

  @Mock private static Subject subject;

  @Mock LoggerService service;

  @BeforeMethod
  public void setUp() {
    permissionsFilter = new LoggerServicePermissionsFilter();
  }

  @Test
  public void shouldCheckPermissionsOnGetLoggers() throws Exception {

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/logger");

    assertEquals(response.getStatusCode(), 200);
    verify(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }

  @Test
  public void shouldThrowExceptionWhenUserDoesNotHavePermissionsToGetLoggers() throws Exception {
    doThrow(new ForbiddenException("User is not authorized"))
        .when(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/logger");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(unwrapError(response), "User is not authorized");
    verify(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
    verifyZeroInteractions(service);
  }

  @Test
  public void shouldCheckPermissionsOnGetLogger() throws Exception {

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/logger/FOO");

    assertEquals(response.getStatusCode(), 204);
    verify(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }

  @Test
  public void shouldThrowExceptionWhenUserDoesNotHavePermissionsToGetLogger() throws Exception {
    doThrow(new ForbiddenException("User is not authorized"))
        .when(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/logger/FOO");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(unwrapError(response), "User is not authorized");
    verify(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
    verifyZeroInteractions(service);
  }

  @Test
  public void shouldCheckPermissionsOnUpdateLogger() throws Exception {

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .put(SECURE_PATH + "/logger/FOO");

    assertEquals(response.getStatusCode(), 204);
    verify(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }

  @Test
  public void shouldThrowExceptionWhenUserDoesNotHavePermissionsToUpdateLogger() throws Exception {
    doThrow(new ForbiddenException("User is not authorized"))
        .when(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .put(SECURE_PATH + "/logger/FOO");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(unwrapError(response), "User is not authorized");
    verify(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
    verifyZeroInteractions(service);
  }

  @Test
  public void shouldCheckPermissionsOnCreateLogger() throws Exception {

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/logger/FOO");

    assertEquals(response.getStatusCode(), 204);
    verify(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }

  @Test
  public void shouldThrowExceptionWhenUserDoesNotHavePermissionsToCreateLogger() throws Exception {
    doThrow(new ForbiddenException("User is not authorized"))
        .when(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/logger/FOO");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(unwrapError(response), "User is not authorized");
    verify(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
    verifyZeroInteractions(service);
  }

  private static String unwrapError(Response response) {
    return unwrapDto(response, ServiceError.class).getMessage();
  }

  private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
