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
package org.eclipse.che.wsagent.server.appstate;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link AppStateService}.
 *
 * @author Roman Nikitenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class AppStateServiceTest {

  private static final String USER_ID = "userId";
  private static final String APP_STATE =
      "{\"projectExplorer\":{\"revealPath\":[\"/spring\"],\"showHiddenFiles\":false}}";
  private static final String EXCEPTION_MESSAGE = "User ID should be defined";

  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  private static final Subject SUBJECT = new SubjectImpl("user", USER_ID, "token", false);

  @Captor private ArgumentCaptor<String> stateCaptor;

  @Mock(answer = Answers.RETURNS_MOCKS)
  private AppStateManager appStateManager;

  @InjectMocks private AppStateService appStateService;

  @Test
  public void shouldGetAppState() throws Exception {
    when(appStateManager.loadAppState(USER_ID)).thenReturn(APP_STATE);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .queryParam("userId", USER_ID)
            .get(SECURE_PATH + "/app/state");

    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getBody().print(), APP_STATE);
  }

  @Test
  public void shouldThrowBadRequestAtGettingAppState() throws Exception {
    when(appStateManager.loadAppState("")).thenThrow(new ValidationException(EXCEPTION_MESSAGE));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .queryParam("userId", "")
            .get(SECURE_PATH + "/app/state");

    assertEquals(response.getStatusCode(), 400);
  }

  @Test
  public void shouldSaveAppState() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .queryParam("userId", USER_ID)
            .body(APP_STATE)
            .post(SECURE_PATH + "/app/state/update");

    verify(appStateManager).saveState(eq(USER_ID), stateCaptor.capture());

    assertEquals(stateCaptor.getValue(), APP_STATE);
    assertEquals(response.getStatusCode(), 204);
  }

  @Test
  public void shouldThrowBadRequestAtSavingAppState() throws Exception {
    doThrow(new ValidationException(EXCEPTION_MESSAGE))
        .when(appStateManager)
        .saveState("", APP_STATE);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .queryParam("userId", "")
            .body(APP_STATE)
            .post(SECURE_PATH + "/app/state/update");

    assertEquals(response.getStatusCode(), 400);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(SUBJECT);
    }
  }
}
