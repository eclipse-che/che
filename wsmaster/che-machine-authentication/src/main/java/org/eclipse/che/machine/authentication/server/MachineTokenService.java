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
package org.eclipse.che.machine.authentication.server;


import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.machine.authentication.shared.dto.MachineTokenDto;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Machine security token service.
 * Allows user to retrieve token to access to the particular workspace, and,
 * in the reverse case, allows get the user by his token.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Path("/machine/token")
public class MachineTokenService {

    private final MachineTokenRegistry   registry;
    private final HttpJsonRequestFactory requestFactory;
    private final String                 apiEndpoint;

    @Inject
    public MachineTokenService(MachineTokenRegistry machineTokenRegistry,
                               HttpJsonRequestFactory requestFactory,
                               @Named("che.api") String apiEndpoint) {
        this.registry = machineTokenRegistry;
        this.requestFactory = requestFactory;
        this.apiEndpoint = apiEndpoint;
    }

    /**
     * Gets the access token for current user for particular workspace with following rules:
     * <ul>
     *   <li>If workspace is started by this user, token was generated on startup time, this method will just return it.<li/>
     *   <li>If workspace is started by other user, but current user has permissions to use it, token will be generated on demand.<li/>
     * <ul/>
     *
     * @param wsId
     *        id of workspace to generate token for.
     * @return entity of machine token
     * @throws NotFoundException
     *         if no workspace exists with given id
     */
    @GET
    @Path("/{wsId}")
    @Produces(MediaType.APPLICATION_JSON)
    public MachineTokenDto getMachineToken(@PathParam("wsId") String wsId) throws NotFoundException {
        final String userId = EnvironmentContext.getCurrent().getSubject().getUserId();
        return newDto(MachineTokenDto.class).withUserId(userId)
                                            .withWorkspaceId(wsId)
                                            .withMachineToken(registry.getOrCreateToken(userId, wsId));
    }

    /**
     * Finds a user by his machine token.
     *
     * @param token
     *        token to find user by
     * @return user entity
     * @throws ApiException
     *         when token is not found, or there is problem retrieving user via api.
     * @throws IOException
     */
    @GET
    @Path("/user/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDto getUser(@PathParam("token") String token) throws ApiException, IOException {
        final String userId = registry.getUserId(token);
        return requestFactory.fromUrl(apiEndpoint + "/user/" + userId)
                             .useGetMethod()
                             .request()
                             .asDto(UserDto.class);
    }
}
