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
package org.eclipse.che.api.auth;

import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import io.swagger.annotations.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * Authenticate user by username and password.
 * <p/>
 * In response user receive "token". This token user can use
 * to identify him in all other request to API, to do that he should pass it as query parameter.
 *
 * @author Sergii Kabashniuk
 * @author Alexander Garagatyi
 */

@Api(value = "/auth",
     description = "Authentication manager")
@Path("/auth")
public class AuthenticationService {

    private final AuthenticationDao dao;

    @Inject
    public AuthenticationService(AuthenticationDao dao) {
        this.dao = dao;
    }

    /**
     * Get token to be able to call secure api methods.
     *
     * @param tokenAccessCookie
     *         - old session-based cookie with token
     * @param credentials
     *         - username and password
     * @return - auth token in JSON, session-based and persistent cookies
     * @throws AuthenticationException
     */
    @ApiOperation(value = "Login",
                  notes = "Login to a Codenvy account. Either auth token or cookie are used",
                  response = Token.class,
                  position = 2)
    @ApiResponses(value = {
                  @ApiResponse(code = 200, message = "OK"),
                  @ApiResponse(code = 400, message = "Authentication error")})
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response authenticate(Credentials credentials,
                                 @ApiParam(value = "Existing auth cookie. It is used to get deleted to a obtain new cookie")
                                 @CookieParam("session-access-key") Cookie tokenAccessCookie,
                                 @Context UriInfo uriInfo)
            throws AuthenticationException {

        return dao.login(credentials, tokenAccessCookie, uriInfo);

    }

    /**
     * Perform logout for the given token.
     *
     * @param token
     *         - authentication token
     * @param tokenAccessCookie
     *         - old session-based cookie with token.
     */
    @ApiOperation(value = "Logout",
                  notes = "Logout from a Codenvy account",
                  position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Authentication error")})
    @POST
    @Path("/logout")
    public Response logout(@ApiParam(value = "Auth token", required = true)
                           @QueryParam("token") String token,
                           @ApiParam(value = "Existing auth cookie. It is used to get deleted to a obtain new cookie")
                           @CookieParam("session-access-key") Cookie tokenAccessCookie,
                           @Context UriInfo uriInfo) {


        return dao.logout(token, tokenAccessCookie, uriInfo);

    }

}
