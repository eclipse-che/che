/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.agent.server.filters;

import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.testng.annotations.Test;

import static com.jayway.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_STACK_BY_ID;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_REMOVE_STACK;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Garagatyi
 */
public class AddExecAgentInStackFilterTest {
    @Test
    public void newStackShouldBeCreatedForUser() throws ConflictException, ServerException {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType(APPLICATION_JSON)
                                         .body(stackDto)
                                         .when()
                                         .post(SECURE_PATH + "/stack");

        assertEquals(response.getStatusCode(), 201);

        verify(stackDao).create(any(StackImpl.class));

        final StackDto stackDtoDescriptor = unwrapDto(response, StackDto.class);

        assertEquals(stackDtoDescriptor.getName(), stackDto.getName());
        assertEquals(stackDtoDescriptor.getCreator(), USER_ID);
        assertEquals(stackDtoDescriptor.getDescription(), stackDto.getDescription());
        assertEquals(stackDtoDescriptor.getTags(), stackDto.getTags());

        assertEquals(stackDtoDescriptor.getComponents(), stackDto.getComponents());

        assertEquals(stackDtoDescriptor.getSource(), stackDto.getSource());

        assertEquals(stackDtoDescriptor.getScope(), stackDto.getScope());

        assertEquals(stackDtoDescriptor.getLinks().size(), 2);
        assertEquals(stackDtoDescriptor.getLinks().get(0).getRel(), LINK_REL_REMOVE_STACK);
        assertEquals(stackDtoDescriptor.getLinks().get(1).getRel(), LINK_REL_GET_STACK_BY_ID);
    }
}
