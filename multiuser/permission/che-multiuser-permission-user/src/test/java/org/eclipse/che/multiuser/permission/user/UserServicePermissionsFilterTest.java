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
package org.eclipse.che.multiuser.permission.user;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.jayway.restassured.response.Response;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
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
 * Tests for {@link UserServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class UserServicePermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  public static final String TOKEN = "token123";
  public static final String USER_ID = "userok";

  @Mock UserManager userManager;

  UserServicePermissionsFilter permissionsFilter;

  @Mock private static Subject subject;

  @Mock UserService service;

  @BeforeMethod
  public void setUp() throws ServerException {
    permissionsFilter = new UserServicePermissionsFilter(true);
    lenient().when(subject.getUserId()).thenReturn(USER_ID);
  }

  @Test
  public void shouldNotCheckPermissionsOnUserCreationFromToken() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/user?token=" + TOKEN);

    assertEquals(response.getStatusCode(), 204);
    verify(service).create(eq(null), eq(TOKEN), anyBoolean());
    verifyZeroInteractions(subject);
  }

  @Test
  public void shouldCheckPermissionsOnUserCreationFromEntity() throws Exception {
    final UserDto userToCreate =
        DtoFactory.newDto(UserDto.class)
            .withId("user123")
            .withEmail("test @test.com")
            .withPassword("***");

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(userToCreate)
            .when()
            .post(SECURE_PATH + "/user");

    assertEquals(response.getStatusCode(), 204);
    verify(service).create(any(), eq(null), anyBoolean());
    verify(subject)
        .checkPermission(
            SystemDomain.DOMAIN_ID, null, UserServicePermissionsFilter.MANAGE_USERS_ACTION);
  }

  @Test
  public void shouldCheckPermissionsOnUserRemoving() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .delete(SECURE_PATH + "/user/user123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).remove(eq("user123"));
    verify(subject)
        .checkPermission(
            SystemDomain.DOMAIN_ID, null, UserServicePermissionsFilter.MANAGE_USERS_ACTION);
  }

  @Test
  public void shouldNotCheckPermissionsOnUserSelfRemoving() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .delete(SECURE_PATH + "/user/" + USER_ID);

    assertEquals(response.getStatusCode(), 204);
    verify(service).remove(eq(USER_ID));
    verify(subject, never())
        .checkPermission(
            SystemDomain.DOMAIN_ID, null, UserServicePermissionsFilter.MANAGE_USERS_ACTION);
  }

  @Test(dataProvider = "publicMethods")
  public void shouldNotCheckPermissionsForPublicMethods(String methodName) throws Exception {
    final Method method =
        Stream.of(UserService.class.getMethods())
            .filter(userServiceMethod -> userServiceMethod.getName().equals(methodName))
            .findAny()
            .orElseGet(null);
    assertNotNull(method);

    final GenericResourceMethod mock = mock(GenericResourceMethod.class);
    when(mock.getMethod()).thenReturn(method);

    permissionsFilter.filter(mock, new Object[] {});

    verifyNoMoreInteractions(subject);
  }

  @DataProvider(name = "publicMethods")
  private Object[][] pathsProvider() {
    return new Object[][] {
      {"getCurrent"}, {"updatePassword"}, {"getById"}, {"find"}, {"getSettings"}
    };
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
