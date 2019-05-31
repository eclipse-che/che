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
package org.eclipse.che.multiuser.permissions.devfile;

import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain.READ;
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
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.devfile.DevfileService;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.permission.devfile.DevfilePermissionsFilter;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class DevfilePermissionsFilterTest {

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private static Subject subject;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private DevfileService service;

  @SuppressWarnings("unused")
  @InjectMocks
  private DevfilePermissionsFilter permissionsFilter;

  @Test
  public void shouldCheckPermissionsOnExportingWorkspaceById() throws Exception {
    final String wsId = "workspace123";
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile/" + wsId);

    assertEquals(response.getStatusCode(), 204);
    verify(subject).checkPermission(DOMAIN_ID, wsId, READ);
    verify(service).createFromWorkspace((eq(wsId)));
  }

  @Test
  public void shouldCheckPermissionsOnExportingWorkspaceByKey() throws Exception {
    final String key = "namespace/ws_name";
    final String wsId = "workspace123";
    WorkspaceImpl workspace = new WorkspaceImpl();
    workspace.setId(wsId);
    when(workspaceManager.getWorkspace(eq(key))).thenReturn(workspace);
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile/" + key);

    assertEquals(response.getStatusCode(), 204);
    verify(subject).checkPermission(DOMAIN_ID, wsId, READ);
    verify(service).createFromWorkspace((eq(key)));
  }

  @Test
  public void shouldReturnForbiddenWhenUserDoesHavePermissionsToExportWorkspaceToDevfile()
      throws Exception {
    doThrow(new ForbiddenException("User in not authorized"))
        .when(subject)
        .checkPermission(anyString(), anyString(), anyString());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile/workspace123");

    assertEquals(response.getStatusCode(), 403);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
