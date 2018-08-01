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
package org.eclipse.che.multiuser.permission.workspace.activity;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link ActivityPermissionsFilter}.
 *
 * @author Max Shaposhnik
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class ActivityPermissionsFilterTest {

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @SuppressWarnings("unused")
  @InjectMocks
  ActivityPermissionsFilter permissionsFilter;

  @Mock private static Subject subject;

  @Mock WorkspaceActivityService service;

  @Test
  public void shouldCheckPermissionsOnGettingMachineById() throws Exception {

    when(subject.hasPermission(
            eq(WorkspaceDomain.DOMAIN_ID), eq("workspace123"), eq(WorkspaceDomain.USE)))
        .thenReturn(true);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .put(SECURE_PATH + "/activity/workspace123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).active(eq("workspace123"));
    verify(subject).checkPermission(WorkspaceDomain.DOMAIN_ID, "workspace123", WorkspaceDomain.USE);
  }

  @Test
  public void shouldThrowExceptionWhenUpdatingNotOwnedWorkspace() throws Exception {

    when(subject.hasPermission(
            eq(WorkspaceDomain.DOMAIN_ID), eq("workspace123"), eq(WorkspaceDomain.USE)))
        .thenReturn(false);
    doThrow(
            new ForbiddenException(
                "The user does not have permission to "
                    + WorkspaceDomain.USE
                    + " workspace with id 'workspace123'"))
        .when(subject)
        .checkPermission(anyString(), anyString(), anyString());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .put(SECURE_PATH + "/activity/workspace123");

    assertEquals(response.getStatusCode(), 403);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void shouldThrowExceptionWhenCallingUnlistedMethod() throws Exception {

    GenericResourceMethod genericResourceMethod = Mockito.mock(GenericResourceMethod.class);
    when(genericResourceMethod.getMethod())
        .thenReturn(
            this.getClass().getDeclaredMethod("shouldThrowExceptionWhenCallingUnlistedMethod"));
    Object[] argument = new Object[0];
    permissionsFilter.filter(genericResourceMethod, argument);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
