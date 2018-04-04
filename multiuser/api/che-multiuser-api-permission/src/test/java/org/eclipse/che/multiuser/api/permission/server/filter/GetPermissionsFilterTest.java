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
import static java.util.Collections.singletonList;
import static org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.List;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.InstanceParameterValidator;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.api.permission.server.PermissionsService;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
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
 * Tests for {@link GetPermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class, EverrestJetty.class})
public class GetPermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private static Subject subject;

  @Mock private PermissionsManager permissionsManager;

  @Mock private PermissionsService permissionsService;

  @Mock private InstanceParameterValidator instanceValidator;

  @Mock private SuperPrivilegesChecker superPrivilegesChecker;

  @InjectMocks private GetPermissionsFilter permissionsFilter;

  @BeforeMethod
  public void setUp() {
    when(subject.getUserId()).thenReturn("user123");
  }

  @Test
  public void shouldRespond403IfUserDoesNotHaveAnyPermissionsForInstance() throws Exception {
    when(permissionsManager.get("user123", "test", "test123")).thenThrow(new NotFoundException(""));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/permissions/test/all?instance=test123");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(unwrapError(response), "User is not authorized to perform this operation");
    verifyZeroInteractions(permissionsService);
    verify(instanceValidator).validate("test", "test123");
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
            .when()
            .get(SECURE_PATH + "/permissions/test/all?instance=test123");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(unwrapError(response), "instance is not valid");
    verifyZeroInteractions(permissionsService);
    verify(instanceValidator).validate("test", "test123");
  }

  @Test
  public void shouldDoChainIfUserHasAnyPermissionsForInstance() throws Exception {
    when(permissionsManager.get("user123", "test", "test123"))
        .thenReturn(new TestPermissions("user123", "test", "test123", singletonList("read")));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/permissions/test/all?instance=test123");

    assertEquals(response.getStatusCode(), 204);
    verify(permissionsService).getUsersPermissions(eq("test"), eq("test123"), anyInt(), anyInt());
    verify(instanceValidator).validate("test", "test123");
  }

  @Test
  public void shouldDoChainIfUserDoesNotHaveAnyPermissionsForInstanceButHasSuperPrivileges()
      throws Exception {
    when(superPrivilegesChecker.isPrivilegedToManagePermissions(anyString())).thenReturn(true);
    when(permissionsManager.get("user123", "test", "test123"))
        .thenThrow(new NotFoundException("not found"));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/permissions/test/all?instance=test123");

    assertEquals(response.getStatusCode(), 204);
    verify(permissionsService).getUsersPermissions(eq("test"), eq("test123"), anyInt(), anyInt());
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

  private class TestPermissions extends AbstractPermissions {

    String domainId;
    String instanceId;
    List<String> actions;

    public TestPermissions(
        String userId, String domainId, String instanceId, List<String> allowedActions) {
      super(userId);
      this.domainId = domainId;
      this.instanceId = instanceId;
      this.actions = allowedActions;
    }

    @Override
    public String getInstanceId() {
      return instanceId;
    }

    @Override
    public String getDomainId() {
      return domainId;
    }

    @Override
    public List<String> getActions() {
      return actions;
    }
  }
}
