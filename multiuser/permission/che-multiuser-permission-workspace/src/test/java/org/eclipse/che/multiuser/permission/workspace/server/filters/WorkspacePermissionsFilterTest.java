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
package org.eclipse.che.multiuser.permission.workspace.server.filters;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import java.lang.reflect.Method;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.api.permission.server.account.AccountOperation;
import org.eclipse.che.multiuser.api.permission.server.account.AccountPermissionsChecker;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
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
 * Tests for {@link WorkspacePermissionsFilter}.
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class WorkspacePermissionsFilterTest {
  private static final String USERNAME = "userok";
  private static final String TEST_ACCOUNT_TYPE = "test";

  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private static Subject subject;

  @Mock private WorkspaceManager workspaceManager;

  @Mock private SuperPrivilegesChecker superPrivilegesChecker;

  private WorkspacePermissionsFilter permissionsFilter;

  @Mock private AccountManager accountManager;

  @Mock private AccountImpl account;

  @Mock private WorkspaceService workspaceService;

  @Mock private AccountPermissionsChecker accountPermissionsChecker;

  @Mock private WorkspaceImpl workspace;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(subject.getUserName()).thenReturn(USERNAME);
    lenient().when(workspaceManager.getWorkspace(any())).thenReturn(workspace);
    lenient().when(workspace.getNamespace()).thenReturn("namespace");
    lenient().when(workspace.getId()).thenReturn("workspace123");

    lenient().when(accountManager.getByName(any())).thenReturn(account);
    lenient().when(account.getType()).thenReturn(TEST_ACCOUNT_TYPE);

    permissionsFilter =
        spy(
            new WorkspacePermissionsFilter(
                workspaceManager,
                accountManager,
                ImmutableSet.of(accountPermissionsChecker),
                superPrivilegesChecker));

    lenient()
        .doThrow(new ForbiddenException(""))
        .when(permissionsFilter)
        .checkAccountPermissions(anyString(), any());
  }

  @Test
  public void shouldCheckAccountPermissionsAccessOnWorkspaceCreationFromDevfile() throws Exception {
    doNothing().when(permissionsFilter).checkAccountPermissions(anyString(), any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(DtoFactory.newDto(DevfileDto.class))
            .when()
            .post(SECURE_PATH + "/workspace/devfile?namespace=userok");

    assertEquals(response.getStatusCode(), 204);
    verify(workspaceService).create(any(DevfileDto.class), any(), any(), eq("userok"), any());
    verify(permissionsFilter).checkAccountPermissions("userok", AccountOperation.CREATE_WORKSPACE);
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldCheckAccountPermissionsOnFetchingWorkspacesByNamespace() throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(false);
    doNothing().when(permissionsFilter).checkAccountPermissions(anyString(), any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/workspace/namespace/userok");

    assertEquals(response.getStatusCode(), 200);
    verify(superPrivilegesChecker).hasSuperPrivileges();
    verify(workspaceService).getByNamespace(any(), eq("userok"));
    verify(permissionsFilter).checkAccountPermissions("userok", AccountOperation.MANAGE_WORKSPACES);
    verifyZeroInteractions(subject);
  }

  @Test
  public void
      shouldNotCheckAccountPermissionsIfUserHasSuperPrivilegesOnFetchingWorkspacesByNamespace()
          throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(true);
    doNothing().when(permissionsFilter).checkAccountPermissions(anyString(), any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/workspace/namespace/userok");

    assertEquals(response.getStatusCode(), 200);
    verify(superPrivilegesChecker).hasSuperPrivileges();
    verify(workspaceService).getByNamespace(any(), eq("userok"));
    verify(permissionsFilter, never())
        .checkAccountPermissions("userok", AccountOperation.MANAGE_WORKSPACES);
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldNotCheckPermissionsOnGettingSettings() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/workspace/settings");

    assertEquals(response.getStatusCode(), 200);
    verify(workspaceService).getSettings();
    verify(permissionsFilter, never()).checkAccountPermissions(anyString(), any());
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldNotCheckPermissionsPermissionsOnWorkspacesGetting() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/workspace");

    assertEquals(response.getStatusCode(), 204);
    verify(workspaceService).getWorkspaces(any(), anyInt(), nullable(String.class));
    verify(permissionsFilter, never()).checkAccountPermissions(anyString(), any());
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldCheckUserPermissionsOnWorkspaceStopping() throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(false);
    when(subject.hasPermission("workspace", "workspace123", "run")).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .pathParam("id", "workspace123")
            .contentType("application/json")
            .when()
            .delete(SECURE_PATH + "/workspace/{id}/runtime");

    assertEquals(response.getStatusCode(), 204);
    verify(superPrivilegesChecker).hasSuperPrivileges();
    verify(workspaceService).stop(eq("workspace123"));
    verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("run"));
  }

  @Test
  public void
      shouldNotCheckPermissionsOnWorkspaceDomainIfUserHasSuperPrivilegesOnWorkspaceStopping()
          throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .pathParam("id", "workspace123")
            .contentType("application/json")
            .when()
            .delete(SECURE_PATH + "/workspace/{id}/runtime");

    assertEquals(response.getStatusCode(), 204);
    verify(superPrivilegesChecker).hasSuperPrivileges();
    verify(workspaceService).stop(eq("workspace123"));
    verify(subject, never()).hasPermission(eq("workspace"), eq("workspace123"), eq("run"));
  }

  @Test
  public void shouldCheckPermissionsOnWorkspaceStarting() throws Exception {
    when(subject.hasPermission("workspace", "workspace123", "run")).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .pathParam("id", "workspace123")
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/workspace/{id}/runtime");

    assertEquals(response.getStatusCode(), 204);
    verify(workspaceService).startById(eq("workspace123"), nullable(String.class), eq(false));
    verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("run"));
  }

  @Test
  public void shouldCheckUserPermissionsOnGetWorkspaceByKey() throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(false);
    when(subject.hasPermission("workspace", "workspace123", "read")).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .pathParam("key", "workspace123")
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/workspace/{key}");

    assertEquals(response.getStatusCode(), 204);
    verify(superPrivilegesChecker).hasSuperPrivileges();
    verify(workspaceService).getByKey(eq("workspace123"), eq("false"));
    verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("read"));
  }

  @Test
  public void
      shouldNotCheckPermissionsOnWorkspaceDomainIfUserHasSuperPrivilegesOnGetWorkspaceByKey()
          throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .pathParam("key", "workspace123")
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/workspace/{key}");

    assertEquals(response.getStatusCode(), 204);
    verify(superPrivilegesChecker).hasSuperPrivileges();
    verify(workspaceService).getByKey(eq("workspace123"), eq("false"));
    verify(subject, never()).hasPermission(eq("workspace"), eq("workspace123"), eq("read"));
  }

  @Test
  public void shouldCheckPermissionsOnGetWorkspaceByUserNameAndWorkspaceName() throws Exception {
    when(subject.hasPermission("workspace", "workspace123", "read")).thenReturn(true);
    User storedUser = mock(User.class);
    when(storedUser.getId()).thenReturn("user123");

    WorkspaceImpl workspace = mock(WorkspaceImpl.class);
    when(workspace.getId()).thenReturn("workspace123");
    when(workspaceManager.getWorkspace("myWorkspace", "userok")).thenReturn(workspace);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .pathParam("key", "userok:myWorkspace")
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/workspace/{key}");

    assertEquals(response.getStatusCode(), 204);
    verify(workspaceService).getByKey(eq("userok:myWorkspace"), eq("false"));
    verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("read"));
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
    when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);
    doThrow(new ForbiddenException(""))
        .when(permissionsFilter)
        .checkAccountPermissions(anyString(), any());

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
        "The user does not have permission to " + action + " workspace with id 'workspace123'");

    verifyZeroInteractions(workspaceService);
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldNotCheckWorkspacePermissionsWhenWorkspaceBelongToHisPersonalAccount(
      String path, String method, String action) throws Exception {
    doNothing().when(permissionsFilter).checkAccountPermissions(anyString(), any());
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(false);
    when(workspace.getNamespace()).thenReturn(USERNAME);

    Response response =
        request(
            given()
                .auth()
                .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                .contentType("application/json")
                .when(),
            SECURE_PATH + path,
            method);
    // Successful 2xx
    assertEquals(response.getStatusCode() / 100, 2);
  }

  @Test
  public void shouldNotThrowExceptionWhenNamespaceIsNullOnNamespaceAccessChecking()
      throws Exception {
    doCallRealMethod().when(permissionsFilter).checkAccountPermissions(anyString(), any());

    permissionsFilter.checkAccountPermissions(null, AccountOperation.MANAGE_WORKSPACES);

    verify(accountPermissionsChecker, never()).checkPermissions(anyString(), any());
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void
      shouldThrowForbiddenExceptionWhenPermissionsCheckerForCorrespondingAccountTypeThrowsForbiddenException()
          throws Exception {
    doCallRealMethod().when(permissionsFilter).checkAccountPermissions(anyString(), any());
    doThrow(new ForbiddenException(""))
        .when(accountPermissionsChecker)
        .checkPermissions(anyString(), any());

    permissionsFilter.checkAccountPermissions("account1", AccountOperation.MANAGE_WORKSPACES);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void shouldThrowForbiddenExceptionWhenThereIsNoPermissionsCheckerForSpecifiedAccount()
      throws Exception {
    doCallRealMethod().when(permissionsFilter).checkAccountPermissions(anyString(), any());
    when(account.getType()).thenReturn("unknown");

    permissionsFilter.checkAccountPermissions("account1", AccountOperation.MANAGE_WORKSPACES);
  }

  @DataProvider(name = "coveredPaths")
  public Object[][] pathsProvider() {
    return new Object[][] {
      {"/workspace/workspace123", "get", WorkspaceDomain.READ},
      {"/workspace/workspace123/runtime", "post", WorkspaceDomain.RUN},
      {"/workspace/workspace123/runtime", "delete", WorkspaceDomain.RUN},
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
