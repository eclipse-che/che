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
package org.eclipse.che.core.metrics;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.RedirectConfig.redirectConfig;
import static com.jayway.restassured.config.RestAssuredConfig.newConfig;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class ApiResponceMetricFilterTest {

  @Mock ApiResponseCounter apiResponseCounter;

  @InjectMocks private ApiResponceMetricFilter filter;

  private DummyService dummyService;

  @BeforeMethod
  public void setUp() {
    filter = new ApiResponceMetricFilter();
    filter.apiResponseCounter = apiResponseCounter;
    dummyService = new DummyService();
  }

  @Test(dataProvider = "successes")
  public void shouldIncrementCounterOnSuccessResponses(int status) {
    dummyService.setStatusToReturn(status);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .get(SECURE_PATH + "/service/success")
        .then()
        .statusCode(status);

    verify(apiResponseCounter).incrementSuccessResponseCounter();
    verify(apiResponseCounter, never()).incrementRedirectResonseCounter();
    verify(apiResponseCounter, never()).incrementClientErrorResponseCounter();
    verify(apiResponseCounter, never()).incrementServerErrorResponceCounter();
  }

  @Test(dataProvider = "redirects")
  public void shouldIncrementCounterOnRedirectResponses(int status) {
    dummyService.setStatusToReturn(status);

    given()
        .config(newConfig().redirect(redirectConfig().followRedirects(false)))
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .get(SECURE_PATH + "/service/redirect")
        .then()
        .statusCode(status);

    verify(apiResponseCounter).incrementRedirectResonseCounter();
    verify(apiResponseCounter, never()).incrementSuccessResponseCounter();
    verify(apiResponseCounter, never()).incrementClientErrorResponseCounter();
    verify(apiResponseCounter, never()).incrementServerErrorResponceCounter();
  }

  @Test(dataProvider = "clientErrors")
  public void shouldIncrementCounterOnClientErrorResponses(int status) {
    dummyService.setStatusToReturn(status);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .get(SECURE_PATH + "/service/clienterror")
        .then()
        .statusCode(status);

    verify(apiResponseCounter).incrementClientErrorResponseCounter();
    verify(apiResponseCounter, never()).incrementSuccessResponseCounter();
    verify(apiResponseCounter, never()).incrementRedirectResonseCounter();
    verify(apiResponseCounter, never()).incrementServerErrorResponceCounter();
  }

  @Test(dataProvider = "serverErrors")
  public void shouldIncrementCounterOnServerErrorResponses(int status) {
    dummyService.setStatusToReturn(status);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .get(SECURE_PATH + "/service/servererror")
        .then()
        .statusCode(status);

    verify(apiResponseCounter).incrementServerErrorResponceCounter();
    verify(apiResponseCounter, never()).incrementSuccessResponseCounter();
    verify(apiResponseCounter, never()).incrementRedirectResonseCounter();
    verify(apiResponseCounter, never()).incrementClientErrorResponseCounter();
  }

  @Path("/service")
  public static class DummyService {

    int statusToReturn;

    public void setStatusToReturn(int statusToReturn) {
      this.statusToReturn = statusToReturn;
    }

    @GET
    @Path("/success")
    public javax.ws.rs.core.Response success() {
      return javax.ws.rs.core.Response.status(statusToReturn).build();
    }

    @GET
    @Path("/redirect")
    public javax.ws.rs.core.Response redirect() {
      return javax.ws.rs.core.Response.temporaryRedirect(URI.create("localhost"))
          .status(statusToReturn)
          .build();
    }

    @GET
    @Path("/clienterror")
    public javax.ws.rs.core.Response clientError() {
      return javax.ws.rs.core.Response.status(statusToReturn).build();
    }

    @GET
    @Path("/servererror")
    public javax.ws.rs.core.Response serverErrorMethod() {
      return javax.ws.rs.core.Response.status(statusToReturn).build();
    }
  }

  @DataProvider(name = "successes")
  public Object[][] success() {
    return new Object[][] {{200}, {201}, {202}, {203}, {204}};
  }

  @DataProvider(name = "redirects")
  public Object[][] redirects() {
    return new Object[][] {{300}, {301}, {302}, {303}, {304}};
  }

  @DataProvider(name = "clientErrors")
  public Object[][] clientErrors() {
    return new Object[][] {{400}, {401}, {402}, {403}, {404}, {405}};
  }

  @DataProvider(name = "serverErrors")
  public Object[][] serverErrors() {
    return new Object[][] {{500}, {501}, {502}, {503}, {504}};
  }
}
