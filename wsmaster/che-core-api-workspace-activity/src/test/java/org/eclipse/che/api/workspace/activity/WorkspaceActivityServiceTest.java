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
package org.eclipse.che.api.workspace.activity;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.net.URI;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
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
 * Tests for {@link WorkspaceActivityService}
 *
 * @author Mihail Kuznyetsov
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class WorkspaceActivityServiceTest {

  @SuppressWarnings("unused") // used by EverrestJetty
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @SuppressWarnings("unused") // used by EverrestJetty
  private final ApiExceptionMapper exceptionMapper = new ApiExceptionMapper();

  private static final String SERVICE_PATH = "/activity";
  private static final String USER_ID = "user123";
  private static final String WORKSPACE_ID = "workspace123";
  private static final Subject TEST_USER = new SubjectImpl("name", USER_ID, "token", false);
  @Mock private WorkspaceActivityManager workspaceActivityManager;

  @Mock private WorkspaceManager workspaceManager;

  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private WorkspaceActivityService workspaceActivityService;

  @BeforeMethod
  public void setUp() {
    workspaceActivityService =
        new WorkspaceActivityService(workspaceActivityManager, workspaceManager);
  }

  @Test
  public void shouldUpdateWorkspaceActivityOfRunningWorkspace()
      throws NotFoundException, ServerException {
    // given
    final WorkspaceImpl workspace = createWorkspace(USER_ID, WorkspaceStatus.RUNNING);
    when(workspaceManager.getWorkspace(WORKSPACE_ID)).thenReturn(workspace);

    // when
    Response response = given().when().put(SERVICE_PATH + '/' + WORKSPACE_ID);

    // then
    assertEquals(response.getStatusCode(), 204);
    verify(workspaceActivityManager).update(eq(WORKSPACE_ID), anyLong());
  }

  @Test(dataProvider = "wsStatus")
  public void shouldNotUpdateWorkspaceActivityOfStartingWorkspace(WorkspaceStatus status)
      throws NotFoundException, ServerException {
    // given
    final WorkspaceImpl workspace = createWorkspace(USER_ID, status);
    when(workspaceManager.getWorkspace(WORKSPACE_ID)).thenReturn(workspace);
    // when
    Response response = given().when().put(SERVICE_PATH + '/' + WORKSPACE_ID);

    assertEquals(response.getStatusCode(), 204);
    verifyZeroInteractions(workspaceActivityManager);
  }

  @Test
  public void shouldRequireStatusParameterForActivityQueries() {
    Response response = given().when().get(URI.create(SERVICE_PATH));

    assertEquals(response.getStatusCode(), 400);
    verifyZeroInteractions(workspaceActivityManager);
  }

  @Test
  public void shouldBeAbleToQueryWithoutTimeConstraints() throws ServerException {
    Page<String> emptyPage = new Page<>(emptyList(), 0, 1, 0);
    when(workspaceActivityManager.findWorkspacesInStatus(any(), anyLong(), anyInt(), anyLong()))
        .thenReturn(emptyPage);

    Response response = given().when().get(URI.create(SERVICE_PATH + "?status=RUNNING"));

    assertEquals(response.getStatusCode(), 200);
    verify(workspaceActivityManager, times(1))
        .findWorkspacesInStatus(
            eq(WorkspaceStatus.RUNNING), anyLong(), eq(Pages.DEFAULT_PAGE_SIZE), eq(0L));
  }

  @Test
  public void shouldIgnoredMinDurationWhenThresholdSpecified() throws Exception {
    when(workspaceActivityManager.findWorkspacesInStatus(
            eq(WorkspaceStatus.STOPPED), anyLong(), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList("ws-1"), 0, 1, 1));

    Response response =
        given()
            .when()
            .get(URI.create(SERVICE_PATH + "?status=STOPPED&threshold=15&minDuration=55"));

    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getBody().print(), "[\"ws-1\"]");
    verify(workspaceActivityManager, times(1))
        .findWorkspacesInStatus(
            eq(WorkspaceStatus.STOPPED), eq(15L), eq(Pages.DEFAULT_PAGE_SIZE), eq(0L));
  }

  @DataProvider(name = "wsStatus")
  public Object[][] getWorkspaceStatus() {
    return new Object[][] {
      {WorkspaceStatus.STARTING}, {WorkspaceStatus.STOPPED}, {WorkspaceStatus.STOPPING}
    };
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {

    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(TEST_USER);
    }
  }

  private WorkspaceImpl createWorkspace(String namespace, WorkspaceStatus status) {
    final WorkspaceConfigImpl config =
        WorkspaceConfigImpl.builder().setName("dev-workspace").setDefaultEnv("dev-env").build();

    return WorkspaceImpl.builder()
        .setConfig(config)
        .generateId()
        .setAccount(new AccountImpl("accountId", namespace, "test"))
        .setStatus(status)
        .build();
  }
}
