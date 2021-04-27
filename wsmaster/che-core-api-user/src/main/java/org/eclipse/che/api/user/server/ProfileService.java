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
package org.eclipse.che.api.user.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_CURRENT_PROFILE;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_CURRENT_PROFILE_ATTRIBUTES;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.shared.dto.ProfileDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Profile REST API.
 *
 * @author Yevhenii Voevodin
 */
@Api(value = "/profile", description = "Profile REST API")
@Path("/profile")
public class ProfileService extends Service {

  @Inject private ProfileManager profileManager;
  @Inject private UserManager userManager;
  @Inject private ProfileLinksInjector linksInjector;
  @Context private SecurityContext context;

  @GET
  @Produces(APPLICATION_JSON)
  @GenerateLink(rel = LINK_REL_CURRENT_PROFILE)
  @ApiOperation("Get profile of the logged in user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains requested profile entity"),
    @ApiResponse(code = 404, message = "Currently logged in user doesn't have profile"),
    @ApiResponse(code = 500, message = "Couldn't retrieve profile due to internal server error")
  })
  public ProfileDto getCurrent() throws ServerException, NotFoundException {
    final ProfileImpl profile = new ProfileImpl();
    profile.setUserId(UserService.dummyUser.getId());

    return linksInjector.injectLinks(
        asDto(profile, UserService.dummyUser), getServiceContext());
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @GenerateLink(rel = LINK_REL_CURRENT_PROFILE)
  @ApiOperation("Get profile by user's id")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains requested profile entity"),
    @ApiResponse(
        code = 404,
        message = "Profile for the user with requested identifier doesn't exist"),
    @ApiResponse(code = 500, message = "Couldn't retrieve profile due to internal server error")
  })
  public ProfileDto getById(@ApiParam("User identifier") @PathParam("id") String userId)
      throws NotFoundException, ServerException {
    return linksInjector.injectLinks(
        asDto(profileManager.getById(userId), userManager.getById(userId)), getServiceContext());
  }

  @PUT
  @Path("/{id}/attributes")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Update the profile attributes of the user with requested identifier",
      notes =
          "The replace strategy is used for the update, so all the existing profile "
              + "attributes will be override by the profile update")
  @ApiResponses({
    @ApiResponse(
        code = 200,
        message =
            "The profile successfully updated and the response contains "
                + "newly updated profile entity"),
    @ApiResponse(
        code = 404,
        message = "When profile for the user with requested identifier doesn't exist"),
    @ApiResponse(code = 500, message = "Couldn't retrieve profile due to internal server error")
  })
  public ProfileDto updateAttributesById(
      @ApiParam("Id of the user") @PathParam("id") String userId,
      @ApiParam("New profile attributes") Map<String, String> updates)
      throws NotFoundException, ServerException, BadRequestException {
    checkAttributes(updates);
    final ProfileImpl profile = new ProfileImpl(profileManager.getById(userId));
    profile.setAttributes(updates);
    profileManager.update(profile);
    return linksInjector.injectLinks(
        asDto(profile, userManager.getById(userId)), getServiceContext());
  }

  @PUT
  @Path("/attributes")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @GenerateLink(rel = LINK_REL_CURRENT_PROFILE_ATTRIBUTES)
  @ApiOperation(
      value = "Update the profile attributes of the currently logged in user",
      notes =
          "The replace strategy is used for the update, so all the existing profile "
              + "attributes will be override with incoming values")
  public ProfileDto updateAttributes(
      @ApiParam("New profile attributes") Map<String, String> updates)
      throws NotFoundException, ServerException, BadRequestException {
    checkAttributes(updates);
    final ProfileImpl profile = new ProfileImpl(profileManager.getById(userId()));
    profile.setAttributes(updates);
    profileManager.update(profile);
    return linksInjector.injectLinks(
        asDto(profile, userManager.getById(profile.getUserId())), getServiceContext());
  }

  @DELETE
  @Path("/attributes")
  @GenerateLink(rel = LINK_REL_CURRENT_PROFILE_ATTRIBUTES)
  @Consumes(APPLICATION_JSON)
  @ApiOperation(
      value = "Remove profile attributes which names are equal to given",
      notes =
          "If names list is not send, all the attributes will be removed, "
              + "if there are no attributes which names equal to some of the given names, "
              + "then those names are skipped.")
  @ApiResponses({
    @ApiResponse(code = 204, message = "Attributes successfully removed"),
    @ApiResponse(code = 500, message = "Couldn't remove attributes due to internal server error")
  })
  public void removeAttributes(
      @ApiParam("The names of the profile attributes to remove") List<String> names)
      throws NotFoundException, ServerException {
    final Profile profile = profileManager.getById(userId());
    final Map<String, String> attributes = profile.getAttributes();
    if (names == null) {
      attributes.clear();
    } else {
      names.forEach(attributes::remove);
    }
    profileManager.update(profile);
  }

  private void checkAttributes(Map<String, String> attributes) throws BadRequestException {
    if (attributes == null) {
      throw new BadRequestException("Update attributes required");
    }
    for (String value : attributes.values()) {
      if (value == null) {
        throw new BadRequestException("Update attributes must not be null");
      }
    }
  }

  private static ProfileDto asDto(Profile profile, User user) {
    return DtoFactory.newDto(ProfileDto.class)
        .withUserId(profile.getUserId())
        .withEmail(user.getEmail())
        .withAttributes(profile.getAttributes());
  }

  private static String userId() {
    return EnvironmentContext.getCurrent().getSubject().getUserId();
  }
}
