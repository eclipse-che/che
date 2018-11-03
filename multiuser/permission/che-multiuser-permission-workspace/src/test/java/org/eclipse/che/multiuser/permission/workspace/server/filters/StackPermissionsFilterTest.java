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
package org.eclipse.che.multiuser.permission.workspace.server.filters;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.eclipse.che.multiuser.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.DELETE;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.DOMAIN_ID;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.READ;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.SEARCH;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.UPDATE;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.stack.StackService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link StackPermissionsFilter}
 *
 * @author Sergii Leschenko
 * @author Mykola Morhun
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class StackPermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @SuppressWarnings("unused")
  @InjectMocks
  StackPermissionsFilter permissionsFilter;

  @Mock private static Subject subject;

  @Mock private StackService service;
  @Mock private PermissionsManager permissionsManager;

  @BeforeMethod
  public void beforeMethod() throws Exception {
    permissionsFilter = spy(new StackPermissionsFilter(permissionsManager));

    lenient().doReturn(false).when(subject).hasPermission("stack", "stack123", SEARCH);
    lenient().doReturn(false).when(subject).hasPermission("system", null, MANAGE_SYSTEM_ACTION);
  }

  @Test
  public void shouldNotCheckPermissionsOnStackCreating() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/stack");

    assertEquals(response.getStatusCode(), 204);
    verify(service).createStack(any());
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldCheckPermissionsOnStackReading() throws Exception {
    when(subject.hasPermission("stack", "stack123", READ)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/stack/stack123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).getStack("stack123");
    verify(subject).hasPermission(eq("stack"), eq("stack123"), eq(READ));
  }

  @Test
  public void shouldCheckPermissionsOnStackUpdating() throws Exception {
    when(subject.hasPermission("stack", "stack123", UPDATE)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .put(SECURE_PATH + "/stack/stack123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).updateStack(any(), eq("stack123"));
    verify(subject).hasPermission(eq("stack"), eq("stack123"), eq(UPDATE));
  }

  @Test
  public void shouldCheckPermissionsOnStackRemoving() throws Exception {
    when(subject.hasPermission("stack", "stack123", DELETE)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .delete(SECURE_PATH + "/stack/stack123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).removeStack(eq("stack123"));
    verify(subject).hasPermission(eq("stack"), eq("stack123"), eq(DELETE));
  }

  @Test
  public void shouldNotCheckPermissionsOnStacksSearching() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/stack");

    assertEquals(response.getStatusCode(), 200);
    verify(service).searchStacks(nullable(List.class), anyInt(), anyInt());
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldCheckPermissionsOnIconReading() throws Exception {
    when(subject.hasPermission("stack", "stack123", READ)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/stack/stack123/icon");

    assertEquals(response.getStatusCode(), 204);
    verify(service).getIcon(eq("stack123"));
    verify(subject).hasPermission(eq("stack"), eq("stack123"), eq(READ));
  }

  @Test
  public void shouldCheckPermissionsOnIconUploading() throws Exception {
    when(subject.hasPermission("stack", "stack123", UPDATE)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("multipart/form-data")
            .multiPart("icon", "content", "image/png")
            .when()
            .post(SECURE_PATH + "/stack/stack123/icon");

    assertEquals(response.getStatusCode(), 204);
    verify(service).uploadIcon(any(), eq("stack123"));
    verify(subject).hasPermission(eq("stack"), eq("stack123"), eq(UPDATE));
  }

  @Test
  public void shouldThrowForbiddenExceptionWhenUserDoesNotHavePermissionsForIconUpdating() {
    when(subject.hasPermission("stack", "stack123", UPDATE)).thenReturn(false);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("multipart/form-data")
            .multiPart("icon", "content", "image/png")
            .when()
            .post(SECURE_PATH + "/stack/stack123/icon");

    assertEquals(response.getStatusCode(), 403);
    Assert.assertEquals(
        unwrapError(response),
        "The user does not have permission to " + UPDATE + " stack with id 'stack123'");
    verifyZeroInteractions(service);
  }

  @Test
  public void shouldCheckPermissionsOnIconRemoving() throws Exception {
    when(subject.hasPermission("stack", "stack123", UPDATE)).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("multipart/form-data")
            .when()
            .delete(SECURE_PATH + "/stack/stack123/icon");

    assertEquals(response.getStatusCode(), 204);
    verify(service).removeIcon(eq("stack123"));
    verify(subject).hasPermission(eq("stack"), eq("stack123"), eq(UPDATE));
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp =
          "The user does not have permission to perform this operation")
  public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
    final GenericResourceMethod mock = mock(GenericResourceMethod.class);
    Method injectLinks = WorkspaceService.class.getMethod("getServiceDescriptor");
    when(mock.getMethod()).thenReturn(injectLinks);

    permissionsFilter.filter(mock, new Object[] {});
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldThrowForbiddenExceptionWhenUserDoesNotHavePermissionsForPerformOperation(
      String path, String method, String action) throws Exception {
    when(subject.hasPermission(
            nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(false);

    Response response =
        request(
            given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when(),
            SECURE_PATH + path,
            method);

    assertEquals(response.getStatusCode(), 403);
    assertEquals(
        unwrapError(response),
        "The user does not have permission to " + action + " stack with id 'stack123'");
    verifyZeroInteractions(service);
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldAllowToAdminPerformAnyActionWithPredefinedStack(
      String path, String method, String action) throws Exception {
    doReturn(false)
        .when(subject)
        .hasPermission(eq(DOMAIN_ID), nullable(String.class), nullable(String.class));
    doReturn(true)
        .when(subject)
        .hasPermission(eq(SystemDomain.DOMAIN_ID), nullable(String.class), nullable(String.class));
    doReturn(true).when(permissionsFilter).isStackPredefined(nullable(String.class));

    Response response =
        request(
            given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when(),
            SECURE_PATH + path,
            method);

    assertEquals(response.getStatusCode() / 100, 2);
    verify(subject).hasPermission(eq(SystemDomain.DOMAIN_ID), eq(null), eq(MANAGE_SYSTEM_ACTION));
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldNotAllowToAdminPerformAnyActionWithNonPredefinedStack(
      String path, String method, String action) throws Exception {
    doReturn(false)
        .when(subject)
        .hasPermission(eq(DOMAIN_ID), nullable(String.class), nullable(String.class));
    doReturn(true)
        .when(subject)
        .hasPermission(eq(SystemDomain.DOMAIN_ID), nullable(String.class), nullable(String.class));
    doReturn(false).when(permissionsFilter).isStackPredefined(nullable(String.class));

    Response response =
        request(
            given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when(),
            SECURE_PATH + path,
            method);

    assertEquals(response.getStatusCode(), 403);
    verify(subject).hasPermission(eq(SystemDomain.DOMAIN_ID), eq(null), eq(MANAGE_SYSTEM_ACTION));
  }

  @DataProvider(name = "coveredPaths")
  public Object[][] pathsProvider() {
    return new Object[][] {
      {"/stack/stack123", "get", READ},
      {"/stack/stack123", "put", UPDATE},
      {"/stack/stack123", "delete", DELETE},
      {"/stack/stack123/icon", "get", READ},
      {"/stack/stack123/icon", "delete", UPDATE}
    };
  }

  @Test
  public void shouldRecognizePredefinedStack() throws Exception {
    final AbstractPermissions stackPermission = mock(AbstractPermissions.class);
    when(stackPermission.getUserId()).thenReturn("*");

    final Page<AbstractPermissions> permissionsPage = mock(Page.class);
    when(permissionsPage.getItems()).thenReturn(Collections.singletonList(stackPermission));

    when(permissionsManager.getByInstance(
            nullable(String.class), nullable(String.class), anyInt(), anyLong()))
        .thenReturn(permissionsPage);

    assertTrue(permissionsFilter.isStackPredefined("stack123"));
  }

  @Test
  public void shouldRecognizeNonPredefinedStack() throws Exception {
    final AbstractPermissions stackPermission = mock(AbstractPermissions.class);
    when(stackPermission.getUserId()).thenReturn("userId");

    final Page<AbstractPermissions> permissionsPage = mock(Page.class);
    when(permissionsPage.getItems()).thenReturn(Collections.singletonList(stackPermission));

    when(permissionsManager.getByInstance(
            nullable(String.class), nullable(String.class), anyInt(), anyLong()))
        .thenReturn(permissionsPage);

    assertFalse(permissionsFilter.isStackPredefined("stack123"));
  }

  @Test
  public void shouldRecognizePredefinedStackWhenAFewPermissionsPagesIsRetrieved() throws Exception {
    final String stackId = "stack123";

    final AbstractPermissions privateStackPermission = mock(AbstractPermissions.class);
    when(privateStackPermission.getUserId()).thenReturn("userId");

    final AbstractPermissions publicStackPermission = mock(AbstractPermissions.class);
    when(publicStackPermission.getUserId()).thenReturn("*");

    final Page<AbstractPermissions> permissionsPage1 = mock(Page.class);
    when(permissionsPage1.getItems())
        .thenReturn(asList(privateStackPermission, privateStackPermission, privateStackPermission));
    when(permissionsPage1.hasNextPage()).thenReturn(true);

    final Page<AbstractPermissions> permissionsPage2 = mock(Page.class);
    when(permissionsPage2.getItems())
        .thenReturn(asList(privateStackPermission, publicStackPermission, privateStackPermission));
    when(permissionsPage2.hasNextPage()).thenReturn(false);

    doReturn(permissionsPage2)
        .when(permissionsFilter)
        .getNextPermissionsPage(stackId, permissionsPage1);
    doReturn(null).when(permissionsFilter).getNextPermissionsPage(stackId, permissionsPage2);
    when(permissionsManager.getByInstance(
            nullable(String.class), nullable(String.class), anyInt(), anyLong()))
        .thenReturn(permissionsPage1);

    assertTrue(permissionsFilter.isStackPredefined(stackId));
  }

  @Test
  public void shouldBeAbleToRetrieveNextPermissionPage() throws Exception {
    final String stackId = "stack123";

    final Page<AbstractPermissions> currentPage = mock(Page.class);
    when(currentPage.hasNextPage()).thenReturn(true);
    when(currentPage.getNextPageRef()).thenReturn(mock(Page.PageRef.class));

    final Page<AbstractPermissions> nextPage = mock(Page.class);
    when(permissionsManager.getByInstance(eq(DOMAIN_ID), eq(stackId), anyInt(), anyLong()))
        .thenReturn(nextPage);

    assertEquals(permissionsFilter.getNextPermissionsPage(stackId, currentPage), nextPage);
  }

  @Test
  public void shouldReturnNullIfNoNextPermissionPage() throws Exception {
    final Page<AbstractPermissions> currentPage = mock(Page.class);
    when(currentPage.hasNextPage()).thenReturn(false);

    assertNull(permissionsFilter.getNextPermissionsPage("stack123", currentPage));
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
