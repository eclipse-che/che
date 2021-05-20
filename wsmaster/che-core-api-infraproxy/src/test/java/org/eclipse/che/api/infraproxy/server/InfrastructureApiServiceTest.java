/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.infraproxy.server;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.everrest.assured.EverrestJetty;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class InfrastructureApiServiceTest {
  @SuppressWarnings("unused") // is declared for use by everrest-assured
  ApiExceptionMapper exceptionMapper = new ApiExceptionMapper();

  @Mock RuntimeInfrastructure infra;
  InfrastructureApiService apiService;

  @BeforeMethod
  public void setup() throws Exception {
    apiService = new InfrastructureApiService("openshift", "openshift-identityProvider", infra);
  }

  @Test
  public void testFailsAuthWhenNotOnOpenShift() throws Exception {
    // given
    apiService = new InfrastructureApiService("not-openshift", "openshift-identityProvider", infra);

    // when
    Response response =
        given()
            .contentType("application/json; charset=utf-8")
            .when()
            .get("/unsupported/k8s/nazdar/");

    // then
    assertEquals(response.getStatusCode(), 403);
  }

  @Test
  public void testFailsAuthWhenNotUsingOpenShiftIdentityProvider() throws Exception {
    // given
    apiService = new InfrastructureApiService("openshift", "not-openshift-identityProvider", infra);

    // when
    Response response =
        given()
            .contentType("application/json; charset=utf-8")
            .when()
            .get("/unsupported/k8s/nazdar/");

    // then
    assertEquals(response.getStatusCode(), 403);
  }

  @Test
  public void testGet() throws Exception {
    // given
    when(infra.sendDirectInfrastructureRequest(any(), any(), any(), eq(null)))
        .thenReturn(
            javax.ws.rs.core.Response.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .build());
    // when
    Response response =
        given()
            .contentType("application/json; charset=utf-8")
            .when()
            .get("/unsupported/k8s/nazdar/");

    // then
    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getContentType(), "application/json;charset=utf-8");
  }

  @Test
  public void testPost() throws Exception {
    // given
    when(infra.sendDirectInfrastructureRequest(any(), any(), any(), any()))
        .thenReturn(
            javax.ws.rs.core.Response.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .build());
    // when
    Response response =
        given()
            .contentType("application/json; charset=utf-8")
            .body("true")
            .when()
            .post("/unsupported/k8s/nazdar/");

    // then
    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getContentType(), "application/json;charset=utf-8");
  }

  @Test
  public void testPut() throws Exception {
    // given
    when(infra.sendDirectInfrastructureRequest(any(), any(), any(), any()))
        .thenReturn(
            javax.ws.rs.core.Response.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .build());
    // when
    Response response =
        given()
            .contentType("application/json; charset=utf-8")
            .body("true")
            .when()
            .put("/unsupported/k8s/nazdar/");

    // then
    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getContentType(), "application/json;charset=utf-8");
  }

  @Test
  public void testHead() throws Exception {
    // given
    when(infra.sendDirectInfrastructureRequest(any(), any(), any(), any()))
        .thenReturn(
            javax.ws.rs.core.Response.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .build());
    // when
    Response response =
        given()
            .contentType("application/json; charset=utf-8")
            .when()
            .head("/unsupported/k8s/nazdar/");

    // then
    assertEquals(response.getStatusCode(), 200);
  }

  @Test
  public void testDelete() throws Exception {
    // given
    when(infra.sendDirectInfrastructureRequest(any(), any(), any(), any()))
        .thenReturn(
            javax.ws.rs.core.Response.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .build());
    // when
    Response response =
        given()
            .contentType("application/json; charset=utf-8")
            .body("true")
            .when()
            .delete("/unsupported/k8s/nazdar/");

    // then
    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getContentType(), "application/json;charset=utf-8");
  }

  @Test
  public void testOptions() throws Exception {
    // given
    when(infra.sendDirectInfrastructureRequest(any(), any(), any(), any()))
        .thenReturn(
            javax.ws.rs.core.Response.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .build());
    // when
    Response response =
        given()
            .contentType("application/json; charset=utf-8")
            .when()
            .options("/unsupported/k8s/nazdar/");

    // then
    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getContentType(), "application/json;charset=utf-8");
  }

  @Test
  public void testPatch() throws Exception {
    // given
    when(infra.sendDirectInfrastructureRequest(any(), any(), any(), any()))
        .thenReturn(
            javax.ws.rs.core.Response.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .build());
    // when
    Response response =
        given()
            .contentType("application/json; charset=utf-8")
            .body("true")
            .when()
            .patch("/unsupported/k8s/nazdar/");

    // then
    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getContentType(), "application/json;charset=utf-8");
  }
}
