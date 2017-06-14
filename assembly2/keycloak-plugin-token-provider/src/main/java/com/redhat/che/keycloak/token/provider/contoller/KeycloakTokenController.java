/*******************************************************************************
 * Copyright (c) 2017 Red Hat inc.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package com.redhat.che.keycloak.token.provider.contoller;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;

import com.redhat.che.keycloak.token.provider.exception.KeycloakException;
import com.redhat.che.keycloak.token.provider.service.KeycloakTokenProvider;
import com.redhat.che.keycloak.token.provider.validator.KeycloakTokenValidator;

@Path("/token")
@Singleton
public class KeycloakTokenController {

    @Inject
    private KeycloakTokenProvider tokenProvider;

    @Inject
    private KeycloakTokenValidator validator;

    @GET
    @Path("/github")
    public Response getGitHubToken(@HeaderParam(HttpHeaders.AUTHORIZATION) String keycloakToken)
            throws ForbiddenException, NotFoundException, ConflictException, BadRequestException, ServerException,
            UnauthorizedException, IOException {
        String token = null;
        try {
            validator.validate(keycloakToken);
            token = tokenProvider.obtainGitHubToken(keycloakToken);
        } catch (KeycloakException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.ok(token).build();
    }

    @GET
    @Path("/oso")
    public Response getOpenShiftToken(@HeaderParam(HttpHeaders.AUTHORIZATION) String keycloakToken)
            throws ForbiddenException, NotFoundException, ConflictException, BadRequestException, ServerException,
            UnauthorizedException, IOException {
        String token = null;
        try {
            validator.validate(keycloakToken);
            token = tokenProvider.obtainOsoToken(keycloakToken);
        } catch (KeycloakException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.ok(token).build();
    }

}
