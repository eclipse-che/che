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
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Mockito.verify;

import com.jayway.restassured.response.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.impl.EnvironmentContext;
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

  private DummyService dummyService = new DummyService();

  @BeforeMethod
  public void setUp() {
    filter = new ApiResponceMetricFilter();
    filter.apiResponseCounter = apiResponseCounter;
  }

  @Test(dataProvider = "serverErrors")
  public void shouldIncrementCounterOnServerErrorResponces(int status) {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .put(SECURE_PATH +  "/service/test");

    verify(apiResponseCounter).incrementServerErrorResponceCounter();
  }

  @Path("/service")
  public static class DummyService {

    @GET
    @Path("/test")
    public void testMethod() {
      System.out.println("222     ");
    }

    @GET
    @Path("/error")
    @Produces(APPLICATION_JSON)
    public void serverErrorMethod() {
      throw new RuntimeException("test");
    }
  }

  @DataProvider(name = "serverErrors")
  public Object[][] serverErrors() {
    return new Object[][] {{500}, {501}, {502}, {503}, {504}};
  }

}
