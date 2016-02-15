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
package org.eclipse.che.api.machine.server.recipe;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.api.machine.server.dao.RecipeDao;
import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.eclipse.che.api.machine.shared.Permissions;
import org.eclipse.che.api.machine.shared.dto.recipe.GroupDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.PermissionsDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.dto.server.DtoFactory;

import org.eclipse.che.commons.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_CREATE_RECIPE;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_GET_RECIPES_BY_CREATOR;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_GET_RECIPE_SCRIPT;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_REMOVE_RECIPE;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_SEARCH_RECIPES;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_UPDATE_RECIPE;

/**
 * Recipe API
 *
 * @author Eugene Voevodin
 */
@Path("/recipe")
public class RecipeService extends Service {

    private final RecipeDao          recipeDao;
    private final PermissionsChecker permissionsChecker;

    @Inject
    public RecipeService(RecipeDao recipeDao, PermissionsChecker permissionsChecker) {
        this.recipeDao = recipeDao;
        this.permissionsChecker = permissionsChecker;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_CREATE_RECIPE)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response createRecipe(NewRecipe newRecipe) throws ApiException {
        if (newRecipe == null) {
            throw new BadRequestException("Recipe required");
        }
        if (isNullOrEmpty(newRecipe.getType())) {
            throw new BadRequestException("Recipe type required");
        }
        if (isNullOrEmpty(newRecipe.getScript())) {
            throw new BadRequestException("Recipe script required");
        }
        if (isNullOrEmpty(newRecipe.getName())) {
            throw new BadRequestException("Recipe name required");
        }
        String userId = EnvironmentContext.getCurrent().getUser().getId();
        Permissions permissions = null;
        if (newRecipe.getPermissions() != null) {
            if (!isSystemUser() && permissionsChecker.hasPublicSearchPermission(newRecipe.getPermissions())) {
                throw new ForbiddenException(format("User %s doesn't have access to use 'public: search' permission", userId));
            }
            permissions = PermissionsImpl.fromDescriptor(newRecipe.getPermissions());
        }

        final ManagedRecipe recipe = new RecipeImpl().withId(NameGenerator.generate("recipe", 16))
                                                     .withName(newRecipe.getName())
                                                     .withCreator(userId)
                                                     .withType(newRecipe.getType())
                                                     .withScript(newRecipe.getScript())
                                                     .withTags(newRecipe.getTags())
                                                     .withPermissions(permissions);
        recipeDao.create(recipe);

        return Response.status(CREATED)
                       .entity(asRecipeDescriptor(recipe))
                       .build();
    }

    @GET
    @Path("/{id}/script")
    @Produces(TEXT_PLAIN)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public String getRecipeScript(@PathParam("id") String id) throws ApiException {
        final ManagedRecipe recipe = recipeDao.getById(id);

        final User user = EnvironmentContext.getCurrent().getUser();
        if (!user.getId().equals(recipe.getCreator()) &&
            !user.isMemberOf("system/admin") &&
            !user.isMemberOf("system/manager") &&
            !permissionsChecker.hasAccess(recipe, user.getId(), "read")) {
            throw new ForbiddenException(format("User %s doesn't have access to recipe %s", user.getId(), id));
        }

        return recipe.getScript();
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public RecipeDescriptor getRecipe(@PathParam("id") String id) throws ApiException {
        final ManagedRecipe recipe = recipeDao.getById(id);

        final User user = EnvironmentContext.getCurrent().getUser();
        if (!user.getId().equals(recipe.getCreator()) &&
            !user.isMemberOf("system/admin") &&
            !user.isMemberOf("system/manager") &&
            !permissionsChecker.hasAccess(recipe, user.getId(), "read")) {
            throw new ForbiddenException(format("User %s doesn't have access to recipe %s", user.getId(), id));
        }

        return asRecipeDescriptor(recipe);
    }

    @GET
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_GET_RECIPES_BY_CREATOR)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public List<RecipeDescriptor> getCreatedRecipes(@DefaultValue("0") @QueryParam("skipCount") Integer skipCount,
                                                    @DefaultValue("30") @QueryParam("maxItems") Integer maxItems) throws ApiException {
        final List<ManagedRecipe> recipes = recipeDao.getByCreator(EnvironmentContext.getCurrent().getUser().getId(), skipCount, maxItems);
        return FluentIterable.from(recipes)
                             .transform(new Function<ManagedRecipe, RecipeDescriptor>() {
                                 @Nullable
                                 @Override
                                 public RecipeDescriptor apply(@Nullable ManagedRecipe recipe) {
                                     return asRecipeDescriptor(recipe);
                                 }
                             })
                             .toList();
    }

    @GET
    @Path("/list")
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_SEARCH_RECIPES)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public List<RecipeDescriptor> searchRecipes(@QueryParam("tags") List<String> tags,
                                                @QueryParam("type") String type,
                                                @DefaultValue("0") @QueryParam("skipCount") Integer skipCount,
                                                @DefaultValue("30") @QueryParam("maxItems") Integer maxItems) throws ApiException {
        final List<ManagedRecipe> recipes = recipeDao.search(tags, type, skipCount, maxItems);
        return FluentIterable.from(recipes)
                             .transform(new Function<ManagedRecipe, RecipeDescriptor>() {
                                 @Nullable
                                 @Override
                                 public RecipeDescriptor apply(@Nullable ManagedRecipe recipe) {
                                     return asRecipeDescriptor(recipe);
                                 }
                             })
                             .toList();
    }

    @PUT
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_UPDATE_RECIPE)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public RecipeDescriptor updateRecipe(RecipeUpdate update) throws ApiException {
        if (update == null) {
            throw new BadRequestException("Update required");
        }
        if (update.getId() == null) {
            throw new BadRequestException("Recipe id required");
        }

        final ManagedRecipe recipe = recipeDao.getById(update.getId());

        final User user = EnvironmentContext.getCurrent().getUser();
        final String userId = user.getId();
        if (!userId.equals(recipe.getCreator()) &&
            !user.isMemberOf("system/admin") &&
            !permissionsChecker.hasAccess(recipe, userId, "write")) {
            throw new ForbiddenException(format("User %s doesn't have access to update recipe %s", userId, update.getId()));
        }
        if (update.getPermissions() != null) {
            //ensure that user has access to update recipe permissions
            if (!userId.equals(recipe.getCreator()) &&
                !user.isMemberOf("system/admin") &&
                !permissionsChecker.hasAccess(recipe, userId, "update_acl")) {
                throw new ForbiddenException(format("User %s doesn't have access to update recipe %s permissions",
                                                    userId,
                                                    update.getId()));
            }
            if (!isSystemUser() && permissionsChecker.hasPublicSearchPermission(update.getPermissions())) {
                throw new ForbiddenException(format("User %s doesn't have access to use 'public: search' permission", userId));
            }
        }

        recipeDao.update(update);

        return asRecipeDescriptor(recipeDao.getById(update.getId()));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public void removeRecipe(@PathParam("id") String id) throws ApiException {
        final ManagedRecipe recipe = recipeDao.getById(id);

        final User user = EnvironmentContext.getCurrent().getUser();
        if (!user.getId().equals(recipe.getCreator()) &&
            !user.isMemberOf("system/admin") &&
            !permissionsChecker.hasAccess(recipe, user.getId(), "write")) {
            throw new ForbiddenException(format("User %s doesn't have access to recipe %s", user.getId(), id));
        }

        recipeDao.remove(id);
    }

    /**
     * Returns true if user has role "system/admin" or "system/manager", otherwise returns false
     */
    private boolean isSystemUser() {
        final User user = EnvironmentContext.getCurrent().getUser();
        return user.isMemberOf("system/admin") || user.isMemberOf("system/manager");
    }

    /**
     * Transforms {@link ManagedRecipe} to {@link RecipeDescriptor}.
     */
    private RecipeDescriptor asRecipeDescriptor(ManagedRecipe recipe) {
        final RecipeDescriptor descriptor = DtoFactory.getInstance()
                                                      .createDto(RecipeDescriptor.class)
                                                      .withId(recipe.getId())
                                                      .withName(recipe.getName())
                                                      .withType(recipe.getType())
                                                      .withScript(recipe.getScript())
                                                      .withCreator(recipe.getCreator())
                                                      .withTags(recipe.getTags());
        final Permissions permissions = recipe.getPermissions();
        if (permissions != null) {
            final List<GroupDescriptor> groups = new ArrayList<>(permissions.getGroups().size());
            for (Group group : permissions.getGroups()) {
                groups.add(DtoFactory.getInstance()
                                     .createDto(GroupDescriptor.class)
                                     .withName(group.getName())
                                     .withUnit(group.getUnit())
                                     .withAcl(group.getAcl()));
            }
            descriptor.setPermissions(DtoFactory.getInstance()
                                                .createDto(PermissionsDescriptor.class)
                                                .withGroups(groups)
                                                .withUsers(permissions.getUsers()));
        }

        final UriBuilder builder = getServiceContext().getServiceUriBuilder();
        final Link removeLink = LinksHelper.createLink("DELETE",
                                                       builder.clone()
                                                              .path(getClass(), "removeRecipe")
                                                              .build(recipe.getId())
                                                              .toString(),
                                                       LINK_REL_REMOVE_RECIPE);
        final Link scriptLink = LinksHelper.createLink("GET",
                                                       builder.clone()
                                                              .path(getClass(), "getRecipeScript")
                                                              .build(recipe.getId())
                                                              .toString(),
                                                       TEXT_PLAIN,
                                                       LINK_REL_GET_RECIPE_SCRIPT);
        descriptor.setLinks(asList(scriptLink, removeLink));
        return descriptor;
    }
}
