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
package org.eclipse.che.multiuser.organization.api.permissions;

import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain.DELETE;
import static org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain.DOMAIN_ID;
import static org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain.MANAGE_SUBORGANIZATIONS;
import static org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain.UPDATE;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.api.OrganizationService;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link
 * org.eclipse.che.multiuser.organization.api.permissions.OrganizationPermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class OrganizationPermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  private static final String USER_ID = "user123";

  @Mock private static Subject subject;

  @Mock private OrganizationService service;

  @Mock private OrganizationManager manager;

  @Mock private SuperPrivilegesChecker superPrivilegesChecker;

  @InjectMocks private OrganizationPermissionsFilter permissionsFilter;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(subject.getUserId()).thenReturn(USER_ID);

    when(manager.getById(anyString()))
        .thenReturn(new OrganizationImpl("organization123", "test", null));
  }

  @Test
  public void shouldTestThatAllPublicMethodsAreCoveredByPermissionsFilter() throws Exception {
    // given
    final List<String> collect =
        Stream.of(OrganizationService.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toList());

    // then
    assertEquals(collect.size(), 7);
    assertTrue(collect.contains(OrganizationPermissionsFilter.CREATE_METHOD));
    assertTrue(collect.contains(OrganizationPermissionsFilter.UPDATE_METHOD));
    assertTrue(collect.contains(OrganizationPermissionsFilter.REMOVE_METHOD));
    assertTrue(collect.contains(OrganizationPermissionsFilter.GET_BY_PARENT_METHOD));
    assertTrue(collect.contains(OrganizationPermissionsFilter.GET_ORGANIZATIONS_METHOD));
    assertTrue(collect.contains(OrganizationPermissionsFilter.GET_BY_ID_METHOD));
    assertTrue(collect.contains(OrganizationPermissionsFilter.FIND_METHOD));
  }

  @Test
  public void shouldNotCheckPermissionsOnGettingOrganizationById() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .when()
        .get(SECURE_PATH + "/organization/organization123");

    verify(service).getById("organization123");
    verifyNoMoreInteractions(subject);
  }

  @Test
  public void shouldNotCheckPermissionsOnGettingOrganizationByName() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .when()
        .get(SECURE_PATH + "/organization/find?name=test");

    verify(service).find("test");
    verifyNoMoreInteractions(subject);
  }

  @Test
  public void shouldNotCheckPermissionsOnOrganizationsFetchingIfUserIdIsNotSpecified()
      throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/organization");

    verify(service).getOrganizations(eq(null), anyInt(), anyInt());
    verify(subject, never()).hasPermission(anyString(), anyString(), anyString());
  }

  @Test
  public void shouldNotCheckPermissionsOnOrganizationsFetchingIfUserSpecifiesHisOwnId()
      throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/organization?user=" + USER_ID);

    verify(service).getOrganizations(eq(USER_ID), anyInt(), anyInt());
    verify(subject, never()).hasPermission(anyString(), anyString(), anyString());
  }

  @Test
  public void shouldCheckSuperPrivilegesOnOrganizationsFetchingIfUserSpecifiesForeignId()
      throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(true);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/organization?user=user321");

    verify(service).getOrganizations(eq("user321"), anyInt(), anyInt());
    verify(superPrivilegesChecker).hasSuperPrivileges();
  }

  @Test
  public void
      shouldThrowForbiddenExceptionOnOrganizationsFetchingIfUserSpecifiesForeignIdAndDoesNotHaveSuperPrivileges()
          throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(false);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .expect()
            .statusCode(403)
            .when()
            .get(SECURE_PATH + "/organization?user=user321");

    assertEquals(unwrapError(response), "The user is able to specify only his own id");
    verify(superPrivilegesChecker).hasSuperPrivileges();
    verifyZeroInteractions(service);
  }

  @Test
  public void shouldCheckPermissionsOnOrganizationUpdating() throws Exception {
    when(subject.hasPermission(DOMAIN_ID, "organization123", UPDATE)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/organization/organization123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).update(eq("organization123"), any());
    verify(subject).hasPermission(DOMAIN_ID, "organization123", UPDATE);
    verify(superPrivilegesChecker, never()).hasSuperPrivileges();
    verifyNoMoreInteractions(subject);
  }

  @Test
  public void shouldCheckPermissionsOnParentOrgLevelOnChildOrganizationUpdating() throws Exception {
    when(manager.getById(anyString()))
        .thenReturn(new OrganizationImpl("organization123", "test", "parent123"));
    when(subject.hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/organization/organization123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).update(eq("organization123"), any());
    verify(subject).hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS);
    verify(superPrivilegesChecker, never()).hasSuperPrivileges();
    verifyNoMoreInteractions(subject);
  }

  @Test
  public void
      shouldCheckPermissionsOnChildOrganizationUpdatingWhenUserDoesNotHavePermissionsOnParentOrgLevel()
          throws Exception {
    when(manager.getById(anyString()))
        .thenReturn(new OrganizationImpl("organization123", "test", "parent123"));
    doReturn(false).when(subject).hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS);
    doReturn(true).when(subject).hasPermission(DOMAIN_ID, "organization123", UPDATE);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/organization/organization123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).update(eq("organization123"), any());
    verify(subject).hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS);
    verify(subject).hasPermission(DOMAIN_ID, "organization123", UPDATE);
  }

  @Test
  public void shouldCheckPermissionsOnOrganizationRemoving() throws Exception {
    when(subject.hasPermission(DOMAIN_ID, "organization123", DELETE)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .delete(SECURE_PATH + "/organization/organization123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).remove(eq("organization123"));
    verify(subject).hasPermission(DOMAIN_ID, "organization123", DELETE);
    verify(superPrivilegesChecker, never()).hasSuperPrivileges();
    verifyNoMoreInteractions(subject);
  }

  @Test
  public void shouldCheckPermissionsOnParentOrgLevelOnChildOrganizationRemoving() throws Exception {
    when(manager.getById(anyString()))
        .thenReturn(new OrganizationImpl("organization123", "test", "parent123"));
    when(subject.hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .delete(SECURE_PATH + "/organization/organization123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).remove(eq("organization123"));
    verify(subject).hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS);
    verify(superPrivilegesChecker, never()).hasSuperPrivileges();
    verifyNoMoreInteractions(subject);
  }

  @Test
  public void
      shouldCheckPermissionsOnChildOrganizationRemovingWhenUserDoesNotHavePermissionsOnParentOrgLevel()
          throws Exception {
    when(manager.getById(anyString()))
        .thenReturn(new OrganizationImpl("organization123", "test", "parent123"));
    doReturn(false).when(subject).hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS);
    doReturn(true).when(subject).hasPermission(DOMAIN_ID, "organization123", DELETE);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .delete(SECURE_PATH + "/organization/organization123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).remove(eq("organization123"));
    verify(subject).hasPermission(DOMAIN_ID, "parent123", MANAGE_SUBORGANIZATIONS);
    verify(subject).hasPermission(DOMAIN_ID, "organization123", DELETE);
    verify(superPrivilegesChecker, never()).hasSuperPrivileges();
    verifyNoMoreInteractions(subject);
  }

  @Test
  public void shouldNotCheckPermissionsOnRootOrganizationCreation() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .body(DtoFactory.newDto(OrganizationDto.class).withParent(null))
            .post(SECURE_PATH + "/organization");

    assertEquals(response.getStatusCode(), 204);
    verify(service).create(any());
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldCheckPermissionsOnChildOrganizationCreation() throws Exception {
    when(subject.hasPermission(DOMAIN_ID, "parent-org", MANAGE_SUBORGANIZATIONS)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .body(DtoFactory.newDto(OrganizationDto.class).withParent("parent-org"))
            .post(SECURE_PATH + "/organization");

    assertEquals(response.getStatusCode(), 204);
    verify(service).create(any());
    verify(subject).hasPermission(DOMAIN_ID, "parent-org", MANAGE_SUBORGANIZATIONS);
  }

  @Test
  public void
      shouldThrowForbiddenExceptionOnChildOrganizationCreationIfUserDoesNotHaveCorrespondingPermission()
          throws Exception {
    when(subject.hasPermission(DOMAIN_ID, "parent-org", MANAGE_SUBORGANIZATIONS)).thenReturn(false);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .body(DtoFactory.newDto(OrganizationDto.class).withParent("parent-org"))
            .post(SECURE_PATH + "/organization");

    assertEquals(response.getStatusCode(), 403);
    verifyZeroInteractions(service);
    verify(subject).hasPermission(DOMAIN_ID, "parent-org", MANAGE_SUBORGANIZATIONS);
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp =
          "The user does not have permission to perform this operation")
  public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
    final GenericResourceMethod mock = mock(GenericResourceMethod.class);
    Method injectLinks = OrganizationService.class.getMethod("getServiceDescriptor");
    when(mock.getMethod()).thenReturn(injectLinks);

    permissionsFilter.filter(mock, new Object[] {});
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldThrowForbiddenExceptionWhenUserDoesNotHavePermissionsForPerformOperation(
      String path, String method, String action) throws Exception {
    when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);

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
        unwrapError(response),
        "The user does not have permission to "
            + action
            + " organization with id 'organization123'");

    verifyZeroInteractions(service);
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldThrowNotFoundWhenUserRequestsNonExistedOrganization(
      String path, String method, String ignored) throws Exception {
    when(manager.getById(anyString()))
        .thenThrow(new NotFoundException("Organization was not found"));

    Response response =
        request(
            given()
                .auth()
                .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                .contentType("application/json")
                .when(),
            SECURE_PATH + path,
            method);

    assertEquals(response.getStatusCode(), 404);
    assertEquals(unwrapError(response), "Organization was not found");

    verifyZeroInteractions(service);
  }

  @DataProvider(name = "coveredPaths")
  public Object[][] pathsProvider() {
    return new Object[][] {
      {"/organization/organization123", "post", UPDATE},
      {"/organization/organization123", "delete", DELETE},
      {"/organization/organization123/organizations", "get", MANAGE_SUBORGANIZATIONS}
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
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
