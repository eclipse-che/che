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
package org.eclipse.che.multiuser.api.permission.server.filter;

import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.Collections;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.InstanceParameterValidator;
import org.eclipse.che.multiuser.api.permission.server.PermissionsService;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.api.permission.server.filter.check.DomainsPermissionsCheckers;
import org.eclipse.che.multiuser.api.permission.server.filter.check.SetPermissionsChecker;
import org.eclipse.che.multiuser.api.permission.shared.dto.PermissionsDto;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link SetPermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class, EverrestJetty.class})
public class SetPermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private static Subject subject;

  @Mock private PermissionsService permissionsService;

  @Mock private SuperPrivilegesChecker superPrivilegesChecker;

  @Mock private InstanceParameterValidator instanceValidator;

  @Mock private DomainsPermissionsCheckers domainsPermissionsCheckers;

  @InjectMocks private SetPermissionsFilter permissionsFilter;

  @BeforeMethod
  public void setUp() {
    when(subject.getUserId()).thenReturn("user123");
  }

  @Test
  public void shouldRespond400IfBodyIsNull() throws Exception {
    when(subject.hasPermission("test", "test123", SET_PERMISSIONS)).thenReturn(false);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/permissions");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(unwrapError(response), "Permissions descriptor required");
    verifyZeroInteractions(permissionsService);
  }

  @Test
  public void shouldRespond400IfDomainIdIsEmpty() throws Exception {
    when(subject.hasPermission("test", "test123", SET_PERMISSIONS)).thenReturn(false);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(
                DtoFactory.newDto(PermissionsDto.class)
                    .withDomainId("")
                    .withInstanceId("test123")
                    .withUserId("user123")
                    .withActions(Collections.singletonList("read")))
            .when()
            .post(SECURE_PATH + "/permissions");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(unwrapError(response), "Domain required");
    verifyZeroInteractions(permissionsService);
  }

  @Test
  public void shouldRespond400IfInstanceIsNotValid() throws Exception {
    when(subject.hasPermission("test", "test123", SET_PERMISSIONS)).thenReturn(false);
    doThrow(new BadRequestException("instance is not valid"))
        .when(instanceValidator)
        .validate(any(), any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(
                DtoFactory.newDto(PermissionsDto.class)
                    .withDomainId("test")
                    .withInstanceId("test123")
                    .withUserId("user123")
                    .withActions(Collections.singletonList("read")))
            .when()
            .post(SECURE_PATH + "/permissions");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(unwrapError(response), "instance is not valid");
    verifyZeroInteractions(permissionsService);
    verify(instanceValidator).validate("test", "test123");
  }

  @Test
  public void shouldRespond403IfUserDoesNotHaveAnyPermissionsForInstance() throws Exception {
    when(subject.hasPermission("test", "test123", SET_PERMISSIONS)).thenReturn(false);
    when(subject.hasPermission("test", "test123", SET_PERMISSIONS)).thenReturn(true);
    final SetPermissionsChecker setPermissionsChecker = mock(SetPermissionsChecker.class);
    when(domainsPermissionsCheckers.getSetChecker("test")).thenReturn(setPermissionsChecker);
    doThrow(new ForbiddenException("ex")).when(setPermissionsChecker).check(any(Permissions.class));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(
                DtoFactory.newDto(PermissionsDto.class)
                    .withDomainId("test")
                    .withInstanceId("test123")
                    .withUserId("user123")
                    .withActions(Collections.singletonList("read")))
            .when()
            .post(SECURE_PATH + "/permissions");

    assertEquals(response.getStatusCode(), 403);
    verifyZeroInteractions(permissionsService);
    verify(instanceValidator).validate("test", "test123");
    verify(setPermissionsChecker, times(1)).check(any(Permissions.class));
  }

  @Test
  public void shouldDoChainIfUserHasAnyPermissionsForInstance() throws Exception {
    when(subject.hasPermission("test", "test123", SET_PERMISSIONS)).thenReturn(true);
    final SetPermissionsChecker setPermissionsChecker = mock(SetPermissionsChecker.class);
    when(domainsPermissionsCheckers.getSetChecker("test")).thenReturn(setPermissionsChecker);
    doNothing().when(setPermissionsChecker).check(any(Permissions.class));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(
                DtoFactory.newDto(PermissionsDto.class)
                    .withDomainId("test")
                    .withInstanceId("test123")
                    .withUserId("user123")
                    .withActions(Collections.singletonList("read")))
            .when()
            .post(SECURE_PATH + "/permissions");

    assertEquals(response.getStatusCode(), 204);
    verify(permissionsService).storePermissions(any());
    verify(instanceValidator).validate("test", "test123");
  }

  @Test
  public void shouldDoChainIfUserDoesNotHavePermissionToSetPermissionsButHasSuperPrivileges()
      throws Exception {
    when(superPrivilegesChecker.isPrivilegedToManagePermissions(anyString())).thenReturn(true);
    when(subject.hasPermission("test", "test123", SET_PERMISSIONS)).thenReturn(false);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(
                DtoFactory.newDto(PermissionsDto.class)
                    .withDomainId("test")
                    .withInstanceId("test123")
                    .withUserId("user123")
                    .withActions(Collections.singletonList("read")))
            .when()
            .post(SECURE_PATH + "/permissions");

    assertEquals(response.getStatusCode(), 204);
    verify(permissionsService).storePermissions(any());
    verify(superPrivilegesChecker).isPrivilegedToManagePermissions("test");
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
