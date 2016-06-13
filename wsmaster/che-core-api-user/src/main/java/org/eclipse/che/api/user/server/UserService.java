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
package org.eclipse.che.api.user.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.status;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_CREATE_USER;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_CURRENT_USER;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_USER_BY_EMAIL;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_USER_BY_ID;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_REMOVE_USER_BY_ID;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_UPDATE_PASSWORD;
import static org.eclipse.che.api.user.server.DtoConverter.toDescriptor;
import static org.eclipse.che.api.user.server.LinksInjector.injectLinks;

/**
 * Provides REST API for user management
 *
 * @author Eugene Voevodin
 * @author Anton Korneta
 */
@Api(value = "/user", description = "User manager")
@Path("/user")
public class UserService extends Service {
    public static final String USER_SELF_CREATION_ALLOWED = "user.self.creation.allowed";

    private final UserManager       userManager;
    private final TokenValidator    tokenValidator;
    private final UserNameValidator userNameValidator;
    private final boolean           userSelfCreationAllowed;

    @Inject
    public UserService(UserManager userManager,
                       TokenValidator tokenValidator,
                       UserNameValidator userNameValidator,
                       @Named(USER_SELF_CREATION_ALLOWED) boolean userSelfCreationAllowed) {
        this.userManager = userManager;
        this.tokenValidator = tokenValidator;
        this.userNameValidator = userNameValidator;
        this.userSelfCreationAllowed = userSelfCreationAllowed;
    }

    /**
     * Creates new user and profile.
     *
     * <p>User will be created from {@code token} parameter or from {@code userDescriptor}
     * when {@code token} is null
     *
     * @param token
     *         authentication token
     * @param isTemporary
     *         if it is {@code true} creates temporary user
     * @return entity of created user
     * @throws ForbiddenException
     *         when the user is not the system admin, or self creation is disabled
     * @throws BadRequestException
     *         when {@code userDescriptor} is invalid
     * @throws UnauthorizedException
     *         when token is null
     * @throws ConflictException
     *         when token is not valid
     * @throws ServerException
     *         when some error occurred while persisting user or user profile
     * @see UserDescriptor
     * @see #getCurrent()
     * @see #updatePassword(String)
     * @see #getById(String)
     * @see #getByAlias(String)
     * @see #remove(String)
     */
    @POST
    @Path("/create")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_CREATE_USER)
    @ApiOperation(value = "Create a new user",
                  notes = "Create a new user in the system. There are two ways to create a user: " +
                          "through a regular registration workflow when auth token is sent to user's mailbox" +
                          "and directly with predefined name and password. ",
                  response = UserDescriptor.class)
    @ApiResponses({@ApiResponse(code = 201, message = "Created"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 401, message = "Missed token parameter"),
                   @ApiResponse(code = 403, message = "Invalid or missing request parameters"),
                   @ApiResponse(code = 409, message = "Invalid token"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response create(@ApiParam(value = "New user")
                           UserDescriptor userDescriptor,
                           @ApiParam(value = "Authentication token")
                           @QueryParam("token")
                           String token,
                           @ApiParam(value = "User type")
                           @QueryParam("temporary")
                           @DefaultValue("false")
                           Boolean isTemporary) throws ForbiddenException,
                                                       BadRequestException,
                                                       UnauthorizedException,
                                                       ConflictException,
                                                       ServerException,
                                                       NotFoundException {
        final User user = isNullOrEmpty(token) ? fromEntity(userDescriptor) : fromToken(token);
        if (!userNameValidator.isValidUserName(user.getName())) {
            throw new BadRequestException("Username must contain only letters and digits");
        }
        userManager.create(user, isTemporary);
        return status(CREATED).entity(injectLinks(toDescriptor(user), getServiceContext())).build();
    }

    /**
     * Returns {@link UserDescriptor} of current user.
     *
     * @return entity of current user.
     * @throws NotFoundException
     *         when current user not found
     * @throws ServerException
     *         when some error occurred while retrieving current user
     */
    @GET
    @GenerateLink(rel = LINK_REL_GET_CURRENT_USER)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get current user",
                  notes = "Get user currently logged in the system",
                  response = UserDescriptor.class,
                  position = 2)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public UserDescriptor getCurrent() throws NotFoundException, ServerException {
        final User user = userManager.getById(currentUserId());
        return injectLinks(toDescriptor(user), getServiceContext());
    }

    /**
     * Updates current user password.
     *
     * @param password
     *         new user password
     * @throws NotFoundException
     *         when current user not found
     * @throws BadRequestException
     *         when given password is invalid
     * @throws ServerException
     *         when some error occurred while updating profile
     * @see UserDescriptor
     */
    @POST
    @Path("/password")
    @GenerateLink(rel = LINK_REL_UPDATE_PASSWORD)
    @Consumes(APPLICATION_FORM_URLENCODED)
    @ApiOperation(value = "Update password",
                  notes = "Update current password")
    @ApiResponses({@ApiResponse(code = 204, message = "OK"),
                   @ApiResponse(code = 400, message = "Invalid password"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public void updatePassword(@ApiParam(value = "New password", required = true)
                               @FormParam("password")
                               String password) throws NotFoundException,
                                                       BadRequestException,
                                                       ServerException,
                                                       ConflictException {

        checkPassword(password);

        final User user = userManager.getById(currentUserId());
        user.setPassword(password);
        userManager.update(user);
    }

    /**
     * Returns status <b>200</b> and {@link UserDescriptor} built from user with given {@code id}
     * or status <b>404</b> when user with given {@code id} was not found.
     *
     * @param id
     *         identifier to search user
     * @return entity of found user
     * @throws NotFoundException
     *         when user with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving user
     * @see UserDescriptor
     * @see #getByAlias(String)
     */
    @GET
    @Path("/{id}")
    @GenerateLink(rel = LINK_REL_GET_USER_BY_ID)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get user by ID",
                  notes = "Get user by its ID in the system",
                  response = UserDescriptor.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public UserDescriptor getById(@ApiParam(value = "User ID") @PathParam("id") String id) throws NotFoundException,
                                                                                                  ServerException {
        final User user = userManager.getById(id);
        return injectLinks(toDescriptor(user), getServiceContext());
    }

    /**
     * Returns status <b>200</b> and {@link UserDescriptor} built from user with given {@code alias}
     * or status <b>404</b> when user with given {@code alias} was not found.
     *
     * @param alias
     *         alias to search user
     * @return entity of found user
     * @throws NotFoundException
     *         when user with given alias doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving user
     * @throws BadRequestException
     *         when alias parameter is missing
     * @see UserDescriptor
     * @see #getById(String)
     * @see #remove(String)
     */
    @GET
    @Path("/find")
    @GenerateLink(rel = LINK_REL_GET_USER_BY_EMAIL)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get user by alias",
                  notes = "Get user by alias",
                  response = UserDescriptor.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 400, message = "Missed alias parameter"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public UserDescriptor getByAlias(@ApiParam(value = "User alias", required = true)
                                     @QueryParam("alias")
                                     @Required String alias) throws NotFoundException,
                                                                    ServerException,
                                                                    BadRequestException {
        if (alias == null) {
            throw new BadRequestException("Missed parameter alias");
        }
        final User user = userManager.getByAlias(alias);
        return injectLinks(toDescriptor(user), getServiceContext());
    }

    /**
     * Removes user with given identifier.
     *
     * @param id
     *         identifier to remove user
     * @throws NotFoundException
     *         when user with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while removing user
     * @throws ConflictException
     *         when some error occurred while removing user
     */
    @DELETE
    @Path("/{id}")
    @GenerateLink(rel = LINK_REL_REMOVE_USER_BY_ID)
    @ApiOperation(value = "Delete user",
                  notes = "Delete a user from the system")
    @ApiResponses({@ApiResponse(code = 204, message = "Deleted"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 409, message = "Impossible to remove user"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public void remove(@ApiParam(value = "User ID") @PathParam("id") String id) throws NotFoundException,
                                                                                       ServerException,
                                                                                       ConflictException {
        userManager.remove(id);
    }

    /**
     * Get user by name.
     *
     * @param name
     *         user name
     * @return found user
     * @throws NotFoundException
     *         when user with given name doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving user
     */
    @GET
    @Path("/name/{name}")
    @GenerateLink(rel = "get user by name")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get user by name",
                  notes = "Get user by its name in the system")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public UserDescriptor getByName(@ApiParam(value = "User email")
                                    @PathParam("name")
                                    String name) throws NotFoundException, ServerException {
        final User user = userManager.getByName(name);
        return injectLinks(toDescriptor(user), getServiceContext());
    }

    /**
     * Get setting of user service
     */
    @GET
    @Path("/settings")
    @Produces(APPLICATION_JSON)
    public Map<String, String> getSettings() {
        return ImmutableMap.of(USER_SELF_CREATION_ALLOWED, Boolean.toString(userSelfCreationAllowed));
    }

    private User fromEntity(UserDescriptor userDescriptor) throws BadRequestException {
        if (userDescriptor == null) {
            throw new BadRequestException("User Descriptor required");
        }
        if (isNullOrEmpty(userDescriptor.getName())) {
            throw new BadRequestException("User name required");
        }
        if (isNullOrEmpty(userDescriptor.getEmail())) {
            throw new BadRequestException("User email required");
        }
        final User user = new User().withName(userDescriptor.getName())
                                    .withEmail(userDescriptor.getEmail());
        if (userDescriptor.getPassword() != null) {
            checkPassword(userDescriptor.getPassword());
            user.setPassword(userDescriptor.getPassword());
        }
        return user;
    }

    private User fromToken(String token) throws UnauthorizedException, ConflictException {
        return tokenValidator.validateToken(token);
    }

    /**
     * Checks user password conforms some rules:
     * <ul>
     * <li> Not null
     * <li> Must be at least 8 character length
     * <li> Must contain at least one letter and one digit
     * </ul>
     *
     * @param password
     *         user's password
     * @throws BadRequestException
     *         when password violates any rule
     */
    private void checkPassword(String password) throws BadRequestException {
        if (password == null) {
            throw new BadRequestException("Password required");
        }
        if (password.length() < 8) {
            throw new BadRequestException("Password should contain at least 8 characters");
        }
        int numOfLetters = 0;
        int numOfDigits = 0;
        for (char passwordChar : password.toCharArray()) {
            if (Character.isDigit(passwordChar)) {
                numOfDigits++;
            } else if (Character.isLetter(passwordChar)) {
                numOfLetters++;
            }
        }
        if (numOfDigits == 0 || numOfLetters == 0) {
            throw new BadRequestException("Password should contain letters and digits");
        }
    }

    private String currentUserId() {
        return EnvironmentContext.getCurrent().getSubject().getUserId();
    }
}
