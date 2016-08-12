/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.rest;


import org.everrest.assured.EverrestJetty;
import org.hamcrest.Matchers;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.jayway.restassured.RestAssured.expect;

@Listeners(value = {EverrestJetty.class})
public class RuntimeExceptionMapperTest {
    @Path("/runtime-exception")
    public static class RuntimeExceptionService {
        @GET
        @Path("/re-empty-msg")
        public String reWithEmptyMessage() {
            throw new NullPointerException();
        }

        @GET
        @Path("/re-with-msg")
        public String reWithMessage() {
            throw new NullPointerException("Message");
        }

    }

    RuntimeExceptionService service;

    RuntimeExceptionMapper mapper;

    @Test
    public void shouldHandleREWithEmptyMessage() {
        expect()
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body(Matchers.isEmptyOrNullString())
                .when().get("/runtime-exception/re-empty-msg");
    }

    @Test
    public void shouldHandleREWithMessage() {
        expect()
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Matchers.equalTo("{\"message\":\"Message\"}"))
                .when().get("/runtime-exception/re-with-msg");
    }

}
