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
package org.eclipse.che.multiuser.resource.api.license;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
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
import org.eclipse.che.multiuser.api.permission.server.account.AccountOperation;
import org.eclipse.che.multiuser.api.permission.server.account.AccountPermissionsChecker;
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
 * Tests for {@link org.eclipse.che.multiuser.resource.api.license.LicenseServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class AccountLicenseServicePermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private AccountManager accountManager;

  @Mock private Account account;

  @Mock private AccountLicenseService service;

  @Mock private static Subject subject;

  @Mock private AccountPermissionsChecker checker;

  private LicenseServicePermissionsFilter filter;

  @BeforeMethod
  public void setUp() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);

    when(account.getType()).thenReturn("test");
    when(checker.getAccountType()).thenReturn("test");

    filter = new LicenseServicePermissionsFilter(accountManager, ImmutableSet.of(checker));
  }

  @Test
  public void shouldTestThatAllPublicMethodsAreCoveredByPermissionsFilter() throws Exception {
    // given
    final List<String> collect =
        Stream.of(AccountLicenseService.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toList());

    // then
    assertEquals(collect.size(), 1);
    assertTrue(collect.contains(LicenseServicePermissionsFilter.GET_LICENSE_METHOD));
  }

  @Test
  public void shouldCheckPermissionsOnGettingLicense() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/license/account/account123");

    verify(checker).checkPermissions("account123", AccountOperation.SEE_RESOURCE_INFORMATION);
    verify(service).getLicense("account123");
  }

  @Test(dataProvider = "coveredPaths")
  public void shouldDenyRequestWhenUserDoesNotHasPermissionsToSeeLicense(String path)
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
  public void shouldDenyRequestThereIsNotPermissionCheckerWhenUserDoesNotHasPermissionsToSeeLicense(
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
    return new Object[][] {{"/license/account/account123"}};
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
