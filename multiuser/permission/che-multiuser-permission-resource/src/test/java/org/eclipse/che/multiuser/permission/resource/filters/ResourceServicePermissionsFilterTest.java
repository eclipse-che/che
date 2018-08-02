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
package org.eclipse.che.multiuser.permission.resource.filters;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.jayway.restassured.response.Response;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.api.permission.server.account.AccountOperation;
import org.eclipse.che.multiuser.api.permission.server.account.AccountPermissionsChecker;
import org.eclipse.che.multiuser.resource.api.free.FreeResourcesLimitService;
import org.eclipse.che.multiuser.resource.api.usage.ResourceService;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link ResourceServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class, EverrestJetty.class})
public class ResourceServicePermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private AccountManager accountManager;

  @Mock private Account account;

  @Mock private ResourceService service;

  @Mock private FreeResourcesLimitService freeResourcesLimitService;

  @Mock private Subject subject;

  @Mock private AccountPermissionsChecker checker;

  private ResourceServicePermissionsFilter filter;

  @BeforeMethod
  public void setUp() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);

    when(account.getType()).thenReturn("test");
    when(checker.getAccountType()).thenReturn("test");

    filter = new ResourceServicePermissionsFilter(accountManager, ImmutableSet.of(checker));
  }

  @Test
  public void shouldTestThatAllPublicMethodsAreCoveredByPermissionsFilter() throws Exception {
    // given
    final List<String> collect =
        Stream.of(ResourceService.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toList());

    // then
    assertEquals(collect.size(), 4);
    assertTrue(collect.contains(ResourceServicePermissionsFilter.GET_TOTAL_RESOURCES_METHOD));
    assertTrue(collect.contains(ResourceServicePermissionsFilter.GET_AVAILABLE_RESOURCES_METHOD));
    assertTrue(collect.contains(ResourceServicePermissionsFilter.GET_USED_RESOURCES_METHOD));
    assertTrue(collect.contains(ResourceServicePermissionsFilter.GET_RESOURCES_DETAILS_METHOD));
  }

  @Test
  public void shouldNotProceedFreeResourcesPath() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/resource/free/account123");

    verify(freeResourcesLimitService).getFreeResourcesLimit(nullable(String.class));
  }

  @Test
  public void shouldCheckPermissionsOnGettingTotalResources() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(200)
        .when()
        .get(SECURE_PATH + "/resource/account123");

    verify(checker).checkPermissions("account123", AccountOperation.SEE_RESOURCE_INFORMATION);
    verify(service).getTotalResources("account123");
  }

  @Test
  public void shouldCheckPermissionsOnGettingAvailableResources() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(200)
        .when()
        .get(SECURE_PATH + "/resource/account123/available");

    verify(checker).checkPermissions("account123", AccountOperation.SEE_RESOURCE_INFORMATION);
    verify(service).getAvailableResources("account123");
  }

  @Test
  public void shouldCheckPermissionsOnGettingUsedResources() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(200)
        .when()
        .get(SECURE_PATH + "/resource/account123/used");

    verify(checker).checkPermissions("account123", AccountOperation.SEE_RESOURCE_INFORMATION);
    verify(service).getUsedResources("account123");
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldDenyRequestWhenUserDoesNotHasPermissionsToSeeResources(String path)
      throws Exception {
    doThrow(new ForbiddenException("Forbidden"))
        .when(checker)
        .checkPermissions(nullable(String.class), any());

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(403)
        .when()
        .get(SECURE_PATH + path);

    verify(checker).checkPermissions("account123", AccountOperation.SEE_RESOURCE_INFORMATION);
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldNotCheckPermissionsOnAccountLevelWhenUserHasManageCodenvyPermission(String path)
      throws Exception {
    when(subject.hasPermission(any(), any(), any())).thenReturn(true);

    final Response response =
        given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).when().get(SECURE_PATH + path);

    assertEquals(response.getStatusCode() / 100, 2);
    verify(subject).hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
    verify(checker, never())
        .checkPermissions("account123", AccountOperation.SEE_RESOURCE_INFORMATION);
  }

  @Test(dataProvider = "coveredPaths")
  public void
      shouldDenyRequestThereIsNotPermissionCheckerWhenUserDoesNotHasPermissionsToSeeResources(
          String path) throws Exception {
    when(account.getType()).thenReturn("unknown");

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(403)
        .when()
        .get(SECURE_PATH + path);
  }

  @Test
  public void testChecksPermissionsOnGettingResourcesDetails() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/resource/account123/details");

    verify(checker).checkPermissions("account123", AccountOperation.SEE_RESOURCE_INFORMATION);
    verify(service).getResourceDetails("account123");
  }

  @Test(dataProvider = "coveredPaths")
  public void testDeniesRequestWhenUserDoesNotHasPermissionsToSeeResourcesDetails(String path)
      throws Exception {
    doThrow(new ForbiddenException("Forbidden")).when(checker).checkPermissions(anyString(), any());

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(403)
        .when()
        .get(SECURE_PATH + path);

    verify(checker).checkPermissions("account123", AccountOperation.SEE_RESOURCE_INFORMATION);
  }

  @Test(dataProvider = "coveredPaths")
  public void testDeniesRequestWhenUserDoesNotHasPermissionsToSeeResourceDetails(String path)
      throws Exception {
    when(account.getType()).thenReturn("unknown");

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(403)
        .when()
        .get(SECURE_PATH + path);
  }

  @DataProvider(name = "coveredPaths")
  public Object[][] pathsProvider() {
    return new Object[][] {
      {"/resource/account123"},
      {"/resource/account123/available"},
      {"/resource/account123/used"},
      {"/resource/account123/details"}
    };
  }

  @Filter
  public class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
