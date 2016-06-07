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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.api.user.shared.dto.UserInRoleDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;

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
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_CURRENT_USER;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_CURRENT_USER_PASSWORD;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_USER;
import static org.eclipse.che.api.user.server.DtoConverter.asDto;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * User REST API.
 *
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
@Path("/user")
@Api(value = "/user", description = "User REST API")
public class UserService extends Service {
    @VisibleForTesting
    static final String USER_SELF_CREATION_ALLOWED = "user.self.creation.allowed";

    private final UserManager       userManager;
    private final TokenValidator    tokenValidator;
    private final UserLinksInjector linksInjector;
    private final boolean           userSelfCreationAllowed;

    @Context
    private SecurityContext context;

    @Inject
    public UserService(UserManager userManager,
                       TokenValidator tokenValidator,
                       UserLinksInjector linksInjector,
                       @Named(USER_SELF_CREATION_ALLOWED) boolean userSelfCreationAllowed) {
        this.userManager = userManager;
        this.linksInjector = linksInjector;
        this.tokenValidator = tokenValidator;
        this.userSelfCreationAllowed = userSelfCreationAllowed;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_USER)
    @ApiOperation(value = "Create a new user", response = UserDto.class)
    @ApiResponses({@ApiResponse(code = 201, message = "User successfully created, response contains created entity"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 401, message = "Missed token parameter"),
                   @ApiResponse(code = 500, message = "Couldn't create user due to internal server error")})
    public Response create(@ApiParam("New user")
                           UserDto userDto,
                           @ApiParam("Authentication token")
                           @QueryParam("token")
                           String token,
                           @ApiParam("User type")
                           @QueryParam("temporary")
                           @DefaultValue("false")
                           Boolean isTemporary,
                           @Context
                           SecurityContext context) throws BadRequestException,
                                                           UnauthorizedException,
                                                           ConflictException,
                                                           ServerException {
        final User createdUser;
        if (token == null) {
            checkUser(userDto);
            createdUser = userManager.create(userDto, isTemporary);
        } else {
            if (!userSelfCreationAllowed) {
                throw new BadRequestException("User self creation is not allowed");
            }
            createdUser = userManager.create(fromToken(token), isTemporary);
        }
        return Response.status(CREATED)
                       .entity(linksInjector.injectLinks(asDto(createdUser), getServiceContext()))
                       .build();
    }

    @GET
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_CURRENT_USER)
    @RolesAllowed({"user", "temp_user"})
    @ApiOperation("Get logged in user")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains currently logged in user entity"),
                   @ApiResponse(code = 500, message = "Couldn't get user due to internal server error")})
    public UserDto getCurrent() throws NotFoundException, ServerException {
        final User user = userManager.getById(userId());
        return linksInjector.injectLinks(asDto(user), getServiceContext());
    }

    @POST
    @Path("/password")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @GenerateLink(rel = LINK_REL_CURRENT_USER_PASSWORD)
    @RolesAllowed("user")
    @ApiOperation(value = "Update password of logged in user",
                  notes = "Password must contain at least 8 characters, " +
                          "passport must contain letters and digits")
    @ApiResponses({@ApiResponse(code = 204, message = "Password successfully updated"),
                   @ApiResponse(code = 400, message = "Incoming password is invalid value." +
                                                      "Valid password must contain at least 8 character " +
                                                      "which are letters and digits"),
                   @ApiResponse(code = 500, message = "Couldn't update password due to internal server error")})
    public void updatePassword(@ApiParam(value = "New password", required = true)
                               @FormParam("password")
                               String password) throws NotFoundException,
                                                       BadRequestException,
                                                       ServerException,
                                                       ConflictException {
        checkPassword(password);

        final UserImpl user = new UserImpl(userManager.getById(userId()));
        user.setPassword(password);
        userManager.update(user);
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_USER)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    @ApiOperation("Get user by identifier")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested user entity"),
                   @ApiResponse(code = 404, message = "User with requested identifier not found"),
                   @ApiResponse(code = 500, message = "Impossible to get user due to internal server error")})
    public UserDto getById(@ApiParam("User identifier")
                           @PathParam("id")
                           String id) throws NotFoundException, ServerException {
        final User user = userManager.getById(id);
        return linksInjector.injectLinks(asDto(user), getServiceContext());
    }

    @GET
    @Path("/find")
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_USER)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    @ApiOperation("Get user by email or name")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested user entity"),
                   @ApiResponse(code = 404, message = "User with requested email/name not found"),
                   @ApiResponse(code = 500, message = "Impossible to get user due to internal server error")})
    public UserDto find(@ApiParam("User email, if it is set then name shouldn't be")
                        @QueryParam("email")
                        String email,
                        @ApiParam("User name, if is is set then email shouldn't be")
                        @QueryParam("name")
                        String name) throws NotFoundException,
                                            ServerException,
                                            BadRequestException {
        if (email == null && name == null) {
            throw new BadRequestException("Missed user's email or name");
        }
        if (email != null && name != null) {
            throw new BadRequestException("Expected either user's email or name, while both values received");
        }
        final User user = name == null ? userManager.getByEmail(email) : userManager.getByName(name);
        return linksInjector.injectLinks(asDto(user), getServiceContext());
    }

    @DELETE
    @Path("/{id}")
    @GenerateLink(rel = LINK_REL_USER)
    @RolesAllowed("system/admin")
    @ApiOperation("Delete user")
    @ApiResponses({@ApiResponse(code = 204, message = "User successfully removed"),
                   @ApiResponse(code = 409, message = "Couldn't remove user due to conflict(e.g. it has related entities)"),
                   @ApiResponse(code = 500, message = "Couldn't remove user due to internal server error")})
    public void remove(@ApiParam("User identifier")
                       @PathParam("id")
                       String id) throws ServerException, ConflictException {
        userManager.remove(id);
    }

    @GET
    @Path("/settings")
    @Produces(APPLICATION_JSON)
    public Map<String, String> getSettings() {
        return ImmutableMap.of(USER_SELF_CREATION_ALLOWED, Boolean.toString(userSelfCreationAllowed));
    }

    // TODO this method should be removed by CODENVY-480
    @GET
    @Path("/inrole")
    @GenerateLink(rel = "current_user.role")
    @RolesAllowed({"temp_user", "user", "system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    @Beta
    @ApiOperation(value = "Check role for the authenticated user",
                  notes = "Check if user has a role in given scope (default is system) and with an optional scope id. " +
                          "Roles allowed: user, system/admin, system/manager.",
                  response = UserInRoleDescriptor.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 403, message = "Unable to check for the given scope"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public UserInRoleDescriptor inRole(@Required @Description("role inside a scope")
                                       @QueryParam("role")
                                       String role,
                                       @DefaultValue("system")
                                       @Description("scope of the role (like system, workspace)")
                                       @QueryParam("scope")
                                       String scope,
                                       @DefaultValue("")
                                       @Description("id used by the scope, like workspaceId for workspace scope")
                                       @QueryParam("scopeId")
                                       String scopeId,
                                       @Context
                                       SecurityContext context) throws NotFoundException,
                                                                       ForbiddenException {
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

    private User fromToken(String token) throws UnauthorizedException, ConflictException {
        final String email = tokenValidator.validateToken(token);
        final int atIdx = email.indexOf('@');
        // Getting all the characters before '@' e.g. user@eclipse.org -> user
        final String name = atIdx == -1 ? email : email.substring(0, atIdx);
        return newDto(UserDto.class).withEmail(email).withName(name);
    }

    private static void checkUser(User user) throws BadRequestException {
        if (user == null) {
            throw new BadRequestException("User required");
        }
        if (isNullOrEmpty(user.getName())) {
            throw new BadRequestException("User name required");
        }
        if (isNullOrEmpty(user.getEmail())) {
            throw new BadRequestException("User email required");
        }
        if (user.getPassword() != null) {
            checkPassword(user.getPassword());
        }
    }

    private static void checkPassword(String password) throws BadRequestException {
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

    private static String userId() {
        return EnvironmentContext.getCurrent().getSubject().getUserId();
    }
}
