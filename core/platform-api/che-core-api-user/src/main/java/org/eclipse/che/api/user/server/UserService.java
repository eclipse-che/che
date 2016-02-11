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

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.user.shared.dto.NewUser;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.api.user.shared.dto.UserInRoleDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.status;
import static org.eclipse.che.api.user.server.Constants.ID_LENGTH;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_CREATE_USER;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_CURRENT_USER;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_USER_BY_EMAIL;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_USER_BY_ID;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_INROLE;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_REMOVE_USER_BY_ID;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_UPDATE_PASSWORD;
import static org.eclipse.che.api.user.server.Constants.PASSWORD_LENGTH;
import static org.eclipse.che.api.user.server.DtoConverter.toDescriptor;
import static org.eclipse.che.api.user.server.LinksInjector.injectLinks;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Provides REST API for user management
 *
 * @author Eugene Voevodin
 */
@Api(value = "/user", description = "User manager")
@Path("/user")
public class UserService extends Service {
    @VisibleForTesting
    static final String USER_SELF_CREATION_ALLOWED = "user.self.creation.allowed";

    private final UserDao        userDao;
    private final UserProfileDao profileDao;
    private final PreferenceDao  preferenceDao;
    private final TokenValidator tokenValidator;
    private final boolean        userSelfCreationAllowed;

    @Inject
    public UserService(UserDao userDao,
                       UserProfileDao profileDao,
                       PreferenceDao preferenceDao,
                       TokenValidator tokenValidator,
                       @Named(USER_SELF_CREATION_ALLOWED) boolean userSelfCreationAllowed) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.preferenceDao = preferenceDao;
        this.tokenValidator = tokenValidator;
        this.userSelfCreationAllowed = userSelfCreationAllowed;
    }

    /**
     * Creates new user and profile.
     * <p/>
     * When current user is in 'system/admin' role then {@code newUser} parameter
     * will be used for user creation, otherwise method uses {@code token} and {@link #tokenValidator}.
     *
     * @param token
     *         authentication token
     * @param isTemporary
     *         if it is {@code true} creates temporary user
     * @return entity of created user
     * @throws UnauthorizedException
     *         when token is {@code null}
     * @throws ConflictException
     *         when token is not valid
     * @throws ServerException
     *         when some error occurred while persisting user or user profile
     * @see UserDescriptor
     * @see #getCurrent(SecurityContext)
     * @see #updatePassword(String)
     * @see #getById(String, SecurityContext)
     * @see #getByAlias(String, SecurityContext)
     * @see #remove(String)
     */
    @ApiOperation(value = "Create a new user",
                  notes = "Create a new user in the system. There are two ways to create a user: through a regular registration workflow " +
                          "and by system/admin. In the former case, auth token is sent to user's mailbox, while system/admin can create a user directly " +
                          "with predefined name and password",
                  response = UserDescriptor.class)
    @ApiResponses({@ApiResponse(code = 201, message = "Created"),
                   @ApiResponse(code = 401, message = "Missed token parameter"),
                   @ApiResponse(code = 409, message = "Invalid token"),
                   @ApiResponse(code = 403, message = "Invalid or missing request parameters"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/create")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_CREATE_USER)
    public Response create(@ApiParam(value = "New user") NewUser newUser,
                           @ApiParam(value = "Authentication token") @QueryParam("token") String token,
                           @ApiParam(value = "User type") @QueryParam("temporary") @DefaultValue("false") Boolean isTemporary,
                           @Context SecurityContext context) throws ApiException {
        if (!context.isUserInRole("system/admin") && !userSelfCreationAllowed) {
            throw new ForbiddenException("Currently only admins can create accounts. Please contact our Admin Team for further info.");
        }

        final User user = context.isUserInRole("system/admin") ? fromEntity(newUser) : fromToken(token);

        userDao.create(user.withId(generate("user", ID_LENGTH))
                           .withPassword(firstNonNull(user.getPassword(), generate("", PASSWORD_LENGTH))));

        profileDao.create(new Profile(user.getId()));

        final Map<String, String> preferences = new HashMap<>(4);
        preferences.put("temporary", Boolean.toString(isTemporary));
        preferences.put("codenvy:created", Long.toString(System.currentTimeMillis()));
        preferenceDao.setPreferences(user.getId(), preferences);

        return status(CREATED).entity(injectLinks(toDescriptor(user), getServiceContext())).build();
    }

    /**
     * Returns {@link UserDescriptor} of current user
     *
     * @return entity of current user.
     * @throws ServerException
     *         when some error occurred while retrieving current user
     */
    @ApiOperation(value = "Get current user",
                  notes = "Get user currently logged in the system",
                  response = UserDescriptor.class,
                  position = 2)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @GenerateLink(rel = LINK_REL_GET_CURRENT_USER)
    @RolesAllowed({"user", "temp_user"})
    @Produces(APPLICATION_JSON)
    public UserDescriptor getCurrent(@Context SecurityContext context) throws NotFoundException, ServerException {
        final User user = userDao.getById(currentUser().getId());
        return injectLinks(toDescriptor(user), getServiceContext());
    }

    /**
     * Updates current user password.
     *
     * @param password
     *         new user password
     * @throws ForbiddenException
     *         when given password is {@code null}
     * @throws ServerException
     *         when some error occurred while updating profile
     * @see UserDescriptor
     */
    @ApiOperation(value = "Update password",
                  notes = "Update current password")
    @ApiResponses({@ApiResponse(code = 204, message = "OK"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 403, message = "Invalid password"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/password")
    @GenerateLink(rel = LINK_REL_UPDATE_PASSWORD)
    @RolesAllowed("user")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public void updatePassword(@ApiParam(value = "New password", required = true)
                               @FormParam("password")
                               String password) throws NotFoundException, ServerException, ForbiddenException, ConflictException {
        checkPassword(password);

        final User user = userDao.getById(currentUser().getId());
        user.setPassword(password);

        userDao.update(user);
    }

    /**
     * Returns status <b>200</b> and {@link UserDescriptor} built from user with given {@code id}
     * or status <b>404</b> when user with given {@code id} was not found
     *
     * @param id
     *         identifier to search user
     * @return entity of found user
     * @throws NotFoundException
     *         when user with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving user
     * @see UserDescriptor
     * @see #getByAlias(String, SecurityContext)
     */
    @ApiOperation(value = "Get user by ID",
                  notes = "Get user by its ID in the system. Roles allowed: system/admin, system/manager.",
                  response = UserDescriptor.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/{id}")
    @GenerateLink(rel = LINK_REL_GET_USER_BY_ID)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    public UserDescriptor getById(@ApiParam(value = "User ID") @PathParam("id") String id,
                                  @Context SecurityContext context) throws NotFoundException, ServerException {
        final User user = userDao.getById(id);
        return injectLinks(toDescriptor(user), getServiceContext());
    }

    /**
     * Returns status <b>200</b> and {@link UserDescriptor} built from user with given {@code alias}
     * or status <b>404</b> when user with given {@code alias} was not found
     *
     * @param alias
     *         alias to search user
     * @return entity of found user
     * @throws NotFoundException
     *         when user with given alias doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving user
     * @see UserDescriptor
     * @see #getById(String, SecurityContext)
     * @see #remove(String)
     */
    @ApiOperation(value = "Get user by alias",
                  notes = "Get user by alias. Roles allowed: system/admin, system/manager.",
                  response = UserDescriptor.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 403, message = "Missed parameter alias"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/find")
    @GenerateLink(rel = LINK_REL_GET_USER_BY_EMAIL)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    public UserDescriptor getByAlias(@ApiParam(value = "User alias", required = true) @QueryParam("alias") @Required String alias,
                                     @Context SecurityContext context) throws NotFoundException, ServerException, ConflictException {
        if (alias == null) {
            throw new ConflictException("Missed parameter alias");
        }
        final User user = userDao.getByAlias(alias);
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
    @ApiOperation(value = "Delete user",
                  notes = "Delete a user from the system. Roles allowed: system/admin")
    @ApiResponses({@ApiResponse(code = 204, message = "Deleted"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 409, message = "Impossible to remove user"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("/{id}")
    @GenerateLink(rel = LINK_REL_REMOVE_USER_BY_ID)
    @RolesAllowed("system/admin")
    public void remove(@ApiParam(value = "User ID")
                       @PathParam("id") String id) throws NotFoundException, ServerException, ConflictException {
        userDao.remove(id);
    }


    /**
     * Allow to check if current user has a given role or not. status <b>200</b> and {@link UserInRoleDescriptor} is returned by indicating if role is granted or not
     *
     * @param role
     *         role to search (like admin or manager)
     * @param scope
     *         the optional scope like system, workspace, account.(default scope is system)
     * @param scopeId
     *         an optional scopeID used by the scope like the workspace ID if scope is workspace.
     * @return {UserInRoleDescriptor} which indicates if role is granted or not
     * @throws org.eclipse.che.api.core.ForbiddenException
     *         with an uknown scope
     * @throws ServerException
     *         when unable to perform the check
     */
    @ApiOperation(value = "Check role for the authenticated user",
                  notes = "Check if user has a role in given scope (default is system) and with an optional scope id. Roles allowed: user, system/admin, system/manager.",
                  response = UserInRoleDescriptor.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 403, message = "Unable to check for the given scope"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/inrole")
    @GenerateLink(rel = LINK_REL_INROLE)
    @RolesAllowed({"temp_user", "user", "system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    @Beta
    public UserInRoleDescriptor inRole(@Required @Description("role inside a scope") @QueryParam("role") String role,
                                       @DefaultValue("system") @Description("scope of the role (like system, workspace)") @QueryParam("scope") String scope,
                                       @DefaultValue("") @Description("id used by the scope, like workspaceId for workspace scope") @QueryParam("scopeId") String scopeId,
                                       @Context SecurityContext context) throws NotFoundException, ServerException, ForbiddenException {

        // handle scope
        boolean isInRole;
        if ("system".equals(scope)) {
            String roleToCheck;
            if ("user".equals(role) || "temp_user".equals(role)) {
                roleToCheck = role;
            } else {
                roleToCheck = "system/" + role;
            }

            // check role
            isInRole = context.isUserInRole(roleToCheck);
        } else {
            throw new ForbiddenException(String.format("Only system scope is handled for now. Provided scope is %s", scope));
        }

        return newDto(UserInRoleDescriptor.class).withIsInRole(isInRole)
                                                 .withRoleName(role)
                                                 .withScope(scope)
                                                 .withScopeId(scopeId);
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
    @RolesAllowed({"user", "system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get user by name",
                  notes = "Get user by its name in the system. Roles allowed: user, system/admin, system/manager.")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Not Found"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public UserDescriptor getByName(@ApiParam(value = "User email")
                                    @PathParam("name")
                                    String name,
                                    @Context
                                    SecurityContext context) throws NotFoundException, ServerException {
        final User user = userDao.getByName(name);
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

    private User fromEntity(NewUser newUser) throws ForbiddenException {
        if (newUser == null) {
            throw new ForbiddenException("New user required");
        }
        if (isNullOrEmpty(newUser.getName())) {
            throw new ForbiddenException("User name required");
        }
        final User user = new User().withName(newUser.getName());
        if (newUser.getPassword() != null) {
            checkPassword(newUser.getPassword());
            user.setPassword(newUser.getPassword());
        }
        return user;
    }

    private User fromToken(String token) throws UnauthorizedException, ConflictException {
        if (token == null) {
            throw new UnauthorizedException("Missed token parameter");
        }
        return new User().withEmail(tokenValidator.validateToken(token));
    }

    private void checkPassword(String password) throws ForbiddenException {
        if (password == null) {
            throw new ForbiddenException("Password required");
        }
        if (password.length() < 8) {
            throw new ForbiddenException("Password should contain at least 8 characters");
        }
        int numOfLetters = 0;
        int numOfDigits = 0;
        for (char passwordChar : password.toCharArray()) {
            if (Character.isDigit(passwordChar)) {
                numOfDigits++;
            }
            if (Character.isLetter(passwordChar)) {
                numOfLetters++;
            }
        }
        if (numOfDigits == 0 || numOfLetters == 0) {
            throw new ForbiddenException("Password should contain letters and digits");
        }
    }

    private org.eclipse.che.commons.user.User currentUser() {
        return EnvironmentContext.getCurrent().getUser();
    }
}