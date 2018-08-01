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
package org.eclipse.che.multiuser.keycloak.server;

import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.multiuser.keycloak.server.UnavailableResourceInMultiUserFilter.ERROR_RESPONSE_JSON_MESSAGE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import com.jayway.restassured.response.Response;
import org.everrest.assured.EverrestJetty;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class UnavailableResourceInMultiUserFilterTest {
  @SuppressWarnings("unused")
  private static final UnavailableResourceInMultiUserFilter FILTER =
      new UnavailableResourceInMultiUserFilter();

  @Test(dataProvider = "allowedRequests")
  public void shouldAllowGetRequests(String url) {
    final Response response = given().when().get(url);

    assertNotEquals(response.getStatusCode(), 403);
    assertNotEquals(response.getBody().print().trim(), ERROR_RESPONSE_JSON_MESSAGE);
  }

  @Test
  public void shouldReturnForbiddenResponseForUserDeletion() {

    final Response response = given().when().delete("/user/123");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(response.getBody().print().trim(), ERROR_RESPONSE_JSON_MESSAGE);
  }

  @Test
  public void shouldReturnForbiddenResponseForUserPasswordUpdate() {

    final Response response = given().when().post("/user/password");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(response.getBody().print().trim(), ERROR_RESPONSE_JSON_MESSAGE);
  }

  @Test
  public void shouldReturnForbiddenResponseForCurrentUserProfileUpdate() {

    final Response response = given().when().post("/profile/attributes");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(response.getBody().print().trim(), ERROR_RESPONSE_JSON_MESSAGE);
  }

  @Test
  public void shouldReturnForbiddenResponseFortUserProfileUpdate() {

    final Response response = given().when().post("/profile/profile123/attributes");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(response.getBody().print().trim(), ERROR_RESPONSE_JSON_MESSAGE);
  }

  @Test
  public void shouldReturnForbiddenResponseForCurrentUserProfileDelete() {

    final Response response = given().when().delete("/profile/attributes");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(response.getBody().print().trim(), ERROR_RESPONSE_JSON_MESSAGE);
  }

  @DataProvider(name = "allowedRequests")
  public Object[][] allowedRequests() {
    return new Object[][] {
      {"/user"},
      {"/user/"},
      {"/user/user123"},
      {"/user/find"},
      {"/user/settings"},
      {"/profile"},
      {"/profile/profile123"}
    };
  }
}
