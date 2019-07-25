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
package org.eclipse.che.api.workspace.server.devfile;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.*;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.everrest.assured.EverrestJetty;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class DevfileServiceTest {

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private ApiExceptionMapper exceptionMapper;

  private DevfileSchemaProvider schemaProvider = new DevfileSchemaProvider();

  private static final Subject SUBJECT = new SubjectImpl("user", "user123", "token", false);

  @SuppressWarnings("unused")
  private DevfileService devFileService;

  @BeforeMethod
  public void initService() {
    this.devFileService = new DevfileService(schemaProvider);
  }

  @Test
  public void shouldRetrieveSchema() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile");

    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getBody().asString(), schemaProvider.getSchemaContent());
  }
}
