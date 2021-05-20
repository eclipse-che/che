/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link ApiResponseMetricFilter} functionality
 *
 * @author Mykhailo Kuznietsov
 */
@Listeners({
  MockitoTestNGListener.class,
  EverrestJetty.class,
})
public class ApiResponseMetricFilterTest {

  @Mock private ApiResponseCounter apiResponseCounter;

  private ApiResponseMetricFilter filter;

  @BeforeMethod
  public void setUp() {
    filter = new ApiResponseMetricFilter();
    filter.setApiResponseCounter(apiResponseCounter);
  }

  @Test
  public void shouldHandleStatusOnHttpRequest() {
    // requesting a non existing resource, so 404 is expected
    int status = 404;

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .get(SECURE_PATH + "/service")
        .then()
        .statusCode(status);

    verify(apiResponseCounter).handleStatus(eq(status));
  }
}
