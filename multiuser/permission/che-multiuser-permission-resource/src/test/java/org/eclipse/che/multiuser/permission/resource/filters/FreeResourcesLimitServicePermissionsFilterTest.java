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
package org.eclipse.che.multiuser.permission.resource.filters;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.resource.api.free.FreeResourcesLimitService;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link FreeResourcesLimitServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners({MockitoTestNGListener.class, EverrestJetty.class})
public class FreeResourcesLimitServicePermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private static Subject subject;

  @Mock private FreeResourcesLimitService service;

  private FreeResourcesLimitServicePermissionsFilter filter;

  @BeforeMethod
  public void setUp() throws Exception {
    filter = new FreeResourcesLimitServicePermissionsFilter();

    when(subject.hasPermission(
            nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(true);
  }

  @Test
  public void shouldTestThatAllPublicMethodsAreCoveredByPermissionsFilter() throws Exception {
    // given
    final List<String> collect =
        Stream.of(FreeResourcesLimitService.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toList());

    // then
    assertEquals(collect.size(), 4);
    assertTrue(
        collect.contains(
            FreeResourcesLimitServicePermissionsFilter.STORE_FREE_RESOURCES_LIMIT_METHOD));
    assertTrue(
        collect.contains(
            FreeResourcesLimitServicePermissionsFilter.GET_FREE_RESOURCES_LIMITS_METHOD));
    assertTrue(
        collect.contains(
            FreeResourcesLimitServicePermissionsFilter.GET_FREE_RESOURCES_LIMIT_METHOD));
    assertTrue(
        collect.contains(
            FreeResourcesLimitServicePermissionsFilter.REMOVE_FREE_RESOURCES_LIMIT_METHOD));
  }

  @Test
  public void shouldCheckManageSystemPermissionsOnGettingResourcesLimits() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/resource/free");

    verify(service).getFreeResourcesLimits(anyInt(), anyInt());
    verify(subject).hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }

  @Test
  public void shouldCheckManageSystemPermissionsOnGettingResourcesLimitForSpecifiedAccount()
      throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/resource/free/account123");

    verify(service).getFreeResourcesLimit("account123");
    verify(subject).hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }

  @Test
  public void shouldCheckManageSystemPermissionsOnRemovingResourcesLimitForSpecifiedAccount()
      throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(204)
        .when()
        .delete(SECURE_PATH + "/resource/free/account123");

    verify(service).removeFreeResourcesLimit("account123");
    verify(subject).hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }

  @Test
  public void shouldCheckManageSystemPermissionsOnStoringResourcesLimit() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .expect()
        .statusCode(204)
        .when()
        .post(SECURE_PATH + "/resource/free");

    verify(service).storeFreeResourcesLimit(any());
    verify(subject).hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }

  @Test(
    expectedExceptions = ForbiddenException.class,
    expectedExceptionsMessageRegExp = "The user does not have permission to perform this operation"
  )
  public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
    final GenericResourceMethod mock = mock(GenericResourceMethod.class);
    Method unknownMethod = FreeResourcesLimitService.class.getMethod("getServiceDescriptor");
    when(mock.getMethod()).thenReturn(unknownMethod);

    filter.filter(mock, new Object[] {});
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldReturn403WhenUserDoesNotHaveRequiredPermission(String path, String method)
      throws Exception {
    when(subject.hasPermission(
            nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(false);

    Response response =
        request(
            given()
                .auth()
                .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                .contentType("application/json")
                .when(),
            SECURE_PATH + path,
            method);

    assertEquals(response.getStatusCode(), 403);
    assertEquals(
        unwrapError(response), "The user does not have permission to perform this operation");

    verify(subject).hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
    verifyZeroInteractions(service);
  }

  @DataProvider(name = "coveredPaths")
  public Object[][] pathsProvider() {
    return new Object[][] {
      {"/resource/free", "post"},
      {"/resource/free", "get"},
      {"/resource/free/account123", "get"},
      {"/resource/free/account123", "delete"}
    };
  }

  private Response request(RequestSpecification request, String path, String method) {
    switch (method) {
      case "post":
        return request.post(path);
      case "get":
        return request.get(path);
      case "delete":
        return request.delete(path);
      case "put":
        return request.put(path);
    }
    throw new RuntimeException("Unsupported method");
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
