/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;
import java.util.Map;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link PreferencesService}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class PreferencesServiceTest {

  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  private static final Subject SUBJECT = new SubjectImpl("user", "user123", "token", false);

  @Mock(answer = Answers.RETURNS_MOCKS)
  private PreferenceManager preferenceManager;

  @InjectMocks private PreferencesService preferencesService;

  @Test
  public void shouldFindPreferences() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/preferences");

    assertEquals(response.getStatusCode(), 200);
    verify(preferenceManager).find(SUBJECT.getUserId());
  }

  @Test
  public void shouldFindPreferencesAndFilter() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/preferences?filter=.*github.*");

    assertEquals(response.getStatusCode(), 200);
    verify(preferenceManager).find(SUBJECT.getUserId(), ".*github.*");
  }

  @Test
  public void shouldSavePreferences() throws Exception {
    final Map<String, String> preferences =
        ImmutableMap.of(
            "pref1", "value1",
            "pref2", "value2",
            "pref3", "value3");
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(preferences)
            .when()
            .post(SECURE_PATH + "/preferences");

    assertEquals(response.getStatusCode(), 204);
    verify(preferenceManager).save(SUBJECT.getUserId(), preferences);
  }

  @Test
  public void shouldNotSavePreferencesWhenNothingSent() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/preferences");

    assertEquals(response.getStatusCode(), 400);
  }

  @Test
  public void shouldUpdatePreferences() throws Exception {
    final Map<String, String> preferences =
        ImmutableMap.of(
            "pref1", "value1",
            "pref2", "value2",
            "pref3", "value3");
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(preferences)
            .when()
            .put(SECURE_PATH + "/preferences");

    assertEquals(response.getStatusCode(), 200);
    verify(preferenceManager).update(SUBJECT.getUserId(), preferences);
  }

  @Test
  public void shouldNotUpdatePreferencesWhenNothingSent() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .put(SECURE_PATH + "/preferences");

    assertEquals(response.getStatusCode(), 400);
  }

  @Test
  public void shouldRemoveSpecifiedPreferences() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(asList("pref1", "pref2"))
            .when()
            .delete(SECURE_PATH + "/preferences");

    assertEquals(response.getStatusCode(), 204);
    verify(preferenceManager).remove(SUBJECT.getUserId(), asList("pref1", "pref2"));
  }

  @Test
  public void shouldRemoveAllPreferencesIfNothingSent() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .delete(SECURE_PATH + "/preferences");

    assertEquals(response.getStatusCode(), 204);
    verify(preferenceManager).remove(SUBJECT.getUserId());
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(SUBJECT);
    }
  }
}
