/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
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
import org.eclipse.che.multiuser.resource.api.usage.ResourceUsageService;
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
 * Tests for {@link ResourceUsageServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class, EverrestJetty.class})
public class ResourceUsageServicePermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private AccountManager accountManager;

  @Mock private Account account;

  @Mock private ResourceUsageService service;

  @Mock private FreeResourcesLimitService freeResourcesLimitService;

  @Mock private static Subject subject;

  @Mock private AccountPermissionsChecker checker;

  private ResourceUsageServicePermissionsFilter filter;

  @BeforeMethod
  public void setUp() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);

    when(account.getType()).thenReturn("test");
    when(checker.getAccountType()).thenReturn("test");

    filter = new ResourceUsageServicePermissionsFilter(accountManager, ImmutableSet.of(checker));
  }

  @Test
  public void shouldTestThatAllPublicMethodsAreCoveredByPermissionsFilter() throws Exception {
    // given
    final List<String> collect =
        Stream.of(ResourceUsageService.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toList());

    // then
    assertEquals(collect.size(), 3);
    assertTrue(collect.contains(ResourceUsageServicePermissionsFilter.GET_TOTAL_RESOURCES_METHOD));
    assertTrue(
        collect.contains(ResourceUsageServicePermissionsFilter.GET_AVAILABLE_RESOURCES_METHOD));
    assertTrue(collect.contains(ResourceUsageServicePermissionsFilter.GET_USED_RESOURCES_METHOD));
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
    when(subject.hasPermission(
            nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(true);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(200)
        .when()
        .get(SECURE_PATH + path);

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

  @DataProvider(name = "coveredPaths")
  public Object[][] pathsProvider() {
    return new Object[][] {
      {"/resource/account123"}, {"/resource/account123/available"}, {"/resource/account123/used"},
    };
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
