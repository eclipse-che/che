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
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class ApiResponseMetricFilterTest {

  @Mock ApiResponseCounter apiResponseCounter;

  @InjectMocks private ApiResponseMetricFilter filter;

  @BeforeMethod
  public void setUp() {
    filter = new ApiResponseMetricFilter();
  }

  @Test
  public void shouldIncrementCounterOnSuccessResponses(int status) {

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .get(SECURE_PATH + "/service/success")
        .then()
        .statusCode(status);

    verify(apiResponseCounter).handleStatus(eq(status));
  }
}
