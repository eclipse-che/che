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


import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.user.shared.dto.ProfileDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;

import com.google.common.util.concurrent.Striped;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.api.user.server.Constants.LINK_REL_UPDATE_CURRENT_USER_PROFILE;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_CURRENT_USER_PROFILE;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_USER_PROFILE_BY_ID;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_REMOVE_ATTRIBUTES;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_REMOVE_PREFERENCES;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_UPDATE_PREFERENCES;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_UPDATE_USER_PROFILE_BY_ID;
import static com.google.common.base.Strings.nullToEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.concurrent.locks.Lock;

/**
 * User Profile API
 *
 * @author Eugene Voevodin
 * @author Max Shaposhnik
 */
@Api(value = "/profile",
     description = "User profile manager")
@Path("/profile")
public class UserProfileService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileService.class);
    
    // Assuming 1000 concurrent users at most trying to update their preferences (if more they will wait for another user to finish).
    // Using the lazy weak version of Striped so the locks will be created on demand and not eagerly, and garbage collected when not needed anymore.
    private static final Striped<Lock> preferencesUpdateLocksByUser = Striped.lazyWeakLock(1000);

    private final UserProfileDao profileDao;
    private final UserDao        userDao;
    private final PreferenceDao  preferenceDao;

    @Inject
    public UserProfileService(UserProfileDao profileDao, PreferenceDao preferenceDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
        this.preferenceDao = preferenceDao;
    }

    /**
     * <p>Returns {@link ProfileDescriptor} for current user profile.</p>
     * <p>By default user email will be added to attributes with key <i>'email'</i>.</p>
     *
     * @return descriptor of profile
     * @throws ServerException
     *         when some error occurred while retrieving/updating profile
     * @see ProfileDescriptor
     * @see #updateCurrent(Map, SecurityContext)
     */
    @ApiOperation(value = "Get user profile",
                  notes = "Get user profile details. Roles allowed: user, temp_user",
                  response = ProfileDescriptor.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @RolesAllowed({"user", "temp_user"})
    @GenerateLink(rel = LINK_REL_GET_CURRENT_USER_PROFILE)
    @Produces(APPLICATION_JSON)
    public ProfileDescriptor getCurrent(@Context SecurityContext context) throws NotFoundException, ServerException {
        final User user = userDao.getById(currentUser().getUserId());
        final Profile profile = profileDao.getById(user.getId());
        profile.getAttributes().put("email", user.getEmail());
        return toDescriptor(profile, context);
    }

    /**
     * Returns preferences for current user
     */
    @ApiOperation(value = "Get user preferences",
            notes = "Get user preferences, like SSH keys, recently opened project and files. It is possible " +
                    "to use a filter, e.g. CodenvyAppState or ssh.key.public.github.com to get the last opened project " +
                    "or a public part of GitHub SSH key (if any)",
            response = ProfileDescriptor.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/prefs")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"user", "temp_user"})
    public Map<String, String> getPreferences(@ApiParam(value = "Filer")
                                                  @QueryParam("filter") String filter) throws ServerException {
        if (filter != null) {
            return preferenceDao.getPreferences(currentUser().getUserId(), filter);
        }
        return preferenceDao.getPreferences(currentUser().getUserId());
    }

    /**
     * Updates attributes of current user profile.
     *
     * @param updates
     *         attributes to update
     * @return descriptor of updated profile
     * @throws ServerException
     *         when some error occurred while retrieving/persisting profile
     * @see ProfileDescriptor
     */

    @POST
    @RolesAllowed("user")
    @GenerateLink(rel = LINK_REL_UPDATE_CURRENT_USER_PROFILE)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public ProfileDescriptor updateCurrent(@Description("attributes to update") Map<String, String> updates,
                                           @Context SecurityContext context) throws NotFoundException, ServerException, ConflictException {
        if (updates == null || updates.isEmpty()) {
            throw new ConflictException("Attributes to update required");
        }
        final User user = userDao.getById(currentUser().getUserId());
        final Profile profile = profileDao.getById(user.getId());
        profile.getAttributes().putAll(updates);
        profileDao.update(profile);
        logEventUserUpdateProfile(user, profile.getAttributes());
        return toDescriptor(profile, context);
    }


    /**
     * Updates attributes of certain profile.
     *
     * @param profileId
     *         profile identifier
     * @param updates
     *         attributes to update
     * @return descriptor of updated profile
     * @throws NotFoundException
     *         when profile with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving/updating profile
     * @see ProfileDescriptor
     * @see #getById(String, SecurityContext)
     */

    @POST
    @Path("/{id}")
    @RolesAllowed({"system/admin"})
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public ProfileDescriptor update(@PathParam("id") String profileId,
                                    Map<String, String> updates,
                                    @Context SecurityContext context) throws NotFoundException, ServerException, ConflictException {
        if (updates == null || updates.isEmpty()) {
            throw new ConflictException("Attributes to update required");
        }
        final Profile profile = profileDao.getById(profileId);
        profile.getAttributes().putAll(updates);
        profileDao.update(profile);
        final User user = userDao.getById(profile.getUserId());
        logEventUserUpdateProfile(user, profile.getAttributes());
        return toDescriptor(profile, context);
    }

    /**
     * Searches for profile with given identifier and {@link ProfileDescriptor} if found.
     *
     * @param profileId
     *         profile identifier
     * @return descriptor of found profile
     * @throws NotFoundException
     *         when profile with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving user or profile
     * @see ProfileDescriptor
     * @see #getById(String, SecurityContext)
     */
    @ApiOperation(value = "Get profile of a specific user",
                  notes = "Get profile of a specific user. Roles allowed: system/admin, system/manager",
                  response = ProfileDescriptor.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/{id}")
    @RolesAllowed({"user", "system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    public ProfileDescriptor getById(@ApiParam(value = "  ID")
                                     @PathParam("id")
                                     String profileId,
                                     @Context SecurityContext context) throws NotFoundException, ServerException {
        final Profile profile = profileDao.getById(profileId);
        final User user = userDao.getById(profile.getUserId());
        profile.getAttributes().put("email", user.getEmail());
        return toDescriptor(profile, context);
    }

    /**
     * <p>Updates preferences of current user profile.</p>
     *
     * @param update
     *         update preferences
     * @return descriptor of updated profile
     * @throws ServerException
     *         when some error occurred while retrieving/updating profile
     * @throws ConflictException
     *         when update is {@code null} or <i>empty</i>
     * @see ProfileDescriptor
     * @see #updateCurrent(Map, SecurityContext)
     */
    @POST
    @Path("/prefs")
    @RolesAllowed({"user", "temp_user"})
    @GenerateLink(rel = LINK_REL_UPDATE_PREFERENCES)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Map<String, String> updatePreferences(@Required Map<String, String> update) throws NotFoundException,
                                                                                              ServerException,
                                                                                              ConflictException {
        if (update == null || update.isEmpty()) {
            throw new ConflictException("Preferences to update required");
        }
        
        String userId = currentUser().getUserId();
        // Keep the lock in a variable so it isn't garbage collected while in use
        Lock lock = preferencesUpdateLocksByUser.get(userId);
        lock.lock();
        try {
            final Map<String, String> preferences = preferenceDao.getPreferences(userId);
            preferences.putAll(update);
            preferenceDao.setPreferences(currentUser().getUserId(), preferences);
            return preferences;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes attributes with given names from current user profile.
     * If names are {@code null} - all attributes will be removed
     *
     * @param attrNames
     *         attributes names to remove
     * @throws ConflictException
     *         when given list of attributes names is {@code null}
     * @throws ServerException
     *         when some error occurred while retrieving/updating profile
     */
    @ApiOperation(value = "Remove attributes of a current user",
                  notes = "Remove attributes of a current user",
                  position = 6)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Attributes names required"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("/attributes")
    @GenerateLink(rel = LINK_REL_REMOVE_ATTRIBUTES)
    @RolesAllowed({"user", "temp_user"})
    @Consumes(APPLICATION_JSON)
    public void removeAttributes(@ApiParam(value = "Attributes", required = true)
                                 @Required
                                 @Description("Attributes names to remove")
                                 List<String> attrNames,
                                 @Context SecurityContext context) throws NotFoundException, ServerException, ConflictException {
        final Profile currentProfile = profileDao.getById(currentUser().getUserId());
        if (attrNames == null) {
            currentProfile.getAttributes().clear();
        } else {
            for (String attributeName : attrNames) {
                currentProfile.getAttributes().remove(attributeName);
            }
        }
        profileDao.update(currentProfile);
    }

    /**
     * Removes preferences with given name from current user profile.
     * If names are {@code null} - all preferences will be removed
     *
     * @param names
     *         preferences names to remove
     * @throws ServerException
     *         when some error occurred while retrieving/updating profile
     * @see #removeAttributes(List, SecurityContext)
     */
    @ApiOperation(value = "Remove profile references of a current user",
                  notes = "Remove profile references of a current user",
                  position = 7)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Preferences names required"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("/prefs")
    @GenerateLink(rel = LINK_REL_REMOVE_PREFERENCES)
    @RolesAllowed({"user", "temp_user"})
    @Consumes(APPLICATION_JSON)
    public void removePreferences(@ApiParam(value = "Preferences to remove", required = true)
                                  @Required
                                  List<String> names) throws ServerException, NotFoundException {
        String userId = currentUser().getUserId();
        if (names == null) {
            preferenceDao.remove(userId);
        } else {
            // Keep the lock in a variable so it isn't garbage collected while in use
            Lock lock = preferencesUpdateLocksByUser.get(userId);
            lock.lock();
            try {
                final Map<String, String> preferences = preferenceDao.getPreferences(userId);
                for (String name : names) {
                    preferences.remove(name);
                }
                preferenceDao.setPreferences(userId, preferences);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Converts {@link Profile} to {@link ProfileDescriptor}
     */
    /* package-private used in tests*/ProfileDescriptor toDescriptor(Profile profile, SecurityContext context) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final List<Link> links = new LinkedList<>();
        if (context.isUserInRole("user")) {
            links.add(LinksHelper.createLink(HttpMethod.GET,
                                             uriBuilder.clone()
                                                       .path(getClass(), "getCurrent")
                                                       .build()
                                                       .toString(),
                                             null,
                                             APPLICATION_JSON,
                                             LINK_REL_GET_CURRENT_USER_PROFILE));
            links.add(LinksHelper.createLink(HttpMethod.GET,
                                             uriBuilder.clone()
                                                       .path(getClass(), "getById")
                                                       .build(profile.getId())
                                                       .toString(),
                                             null,
                                             APPLICATION_JSON,
                                             LINK_REL_GET_USER_PROFILE_BY_ID));
            links.add(LinksHelper.createLink(HttpMethod.POST,
                                             uriBuilder.clone()
                                                       .path(getClass(), "updateCurrent")
                                                       .build()
                                                       .toString(),
                                             APPLICATION_JSON,
                                             APPLICATION_JSON,
                                             LINK_REL_UPDATE_CURRENT_USER_PROFILE));
            links.add(LinksHelper.createLink(HttpMethod.POST,
                                             uriBuilder.clone()
                                                       .path(getClass(), "updatePreferences")
                                                       .build()
                                                       .toString(),
                                             APPLICATION_JSON,
                                             APPLICATION_JSON,
                                             LINK_REL_UPDATE_PREFERENCES));
        }
        if (context.isUserInRole("system/admin") || context.isUserInRole("system/manager")) {
            links.add(LinksHelper.createLink(HttpMethod.GET,
                                             uriBuilder.clone()
                                                       .path(getClass(), "getById")
                                                       .build(profile.getId())
                                                       .toString(),
                                             null,
                                             APPLICATION_JSON,
                                             LINK_REL_GET_USER_PROFILE_BY_ID));
        }
        if (context.isUserInRole("system/admin")) {
            links.add(LinksHelper.createLink(HttpMethod.POST,
                                             uriBuilder.clone()
                                                       .path(getClass(), "update")
                                                       .build(profile.getId())
                                                       .toString(),
                                             APPLICATION_JSON,
                                             APPLICATION_JSON,
                                             LINK_REL_UPDATE_USER_PROFILE_BY_ID));
        }
        return DtoFactory.getInstance().createDto(ProfileDescriptor.class)
                         .withId(profile.getId())
                         .withUserId(profile.getUserId())
                         .withAttributes(profile.getAttributes())
                         .withLinks(links);
    }

    private Subject currentUser() {
        return EnvironmentContext.getCurrent().getSubject();
    }

    private void logEventUserUpdateProfile(User user, Map<String, String> attributes) {
        final Set<String> emails = new HashSet<>(user.getAliases());
        emails.add(user.getEmail());

        LOG.info("EVENT#user-update-profile# USER#{}# FIRSTNAME#{}# LASTNAME#{}# COMPANY#{}# PHONE#{}# JOBTITLE#{}# EMAILS#{}# USER-ID#{}#",
                 user.getEmail(),
                 nullToEmpty(attributes.get("firstName")),
                 nullToEmpty(attributes.get("lastName")),
                 nullToEmpty(attributes.get("employer")),
                 nullToEmpty(attributes.get("phone")),
                 nullToEmpty(attributes.get("jobtitle")),
                 user.getAliases(),
                 user.getId());
    }
}
