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

import com.google.common.collect.FluentIterable;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.machine.server.dao.RecipeDao;
import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.GroupDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.PermissionsDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Eugene Voevodin
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class RecipeServiceTest {

    @SuppressWarnings("unused")
    static final EnvironmentFilter  FILTER  = new EnvironmentFilter();
    @SuppressWarnings("unused")
    static final ApiExceptionMapper MAPPER  = new ApiExceptionMapper();
    static final String             USER_ID = "user123";
    static final LinkedList<String> ROLES   = new LinkedList<>(asList("user"));

    @Mock
    RecipeDao          recipeDao;
    @Mock
    PermissionsChecker permissionsChecker;
    @Mock
    UriInfo            uriInfo;
    @InjectMocks
    RecipeService      service;

    @BeforeMethod
    public void setUpUriInfo() throws NoSuchFieldException, IllegalAccessException {
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        final Field uriField = service.getClass()
                                      .getSuperclass()
                                      .getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(service, uriInfo);
    }

    @AfterMethod
    public void cleanUp() {
        ROLES.remove("system/admin");
    }

    @Test
    public void shouldThrowBadRequestExceptionOnCreateRecipeWithNullBody() {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Recipe required");
    }

    @Test
    public void shouldThrowBadRequestExceptionOnCreateRecipeWithNewRecipeWhichDoesNotHaveType() {
        final NewRecipe newRecipe = newDto(NewRecipe.class).withScript("FROM ubuntu\n");

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(newRecipe)
                                         .when()
                                         .post(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Recipe type required");
    }

    @Test
    public void shouldThrowBadRequestExceptionOnCreateRecipeWithNewRecipeWhichDoesNotHaveScript() {
        final NewRecipe newRecipe = newDto(NewRecipe.class).withType("docker");

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(newRecipe)
                                         .when()
                                         .post(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Recipe script required");
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenCreatingRecipeWithoutName() {
        final NewRecipe newRecipe = newDto(NewRecipe.class).withType("docker").withScript("script");

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(newRecipe)
                                         .when()
                                         .post(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Recipe name required");
    }

    @Test
    public void shouldCreateNewRecipe() throws Exception {
        final GroupDescriptor group = newDto(GroupDescriptor.class).withName("public").withAcl(asList("read"));
        final NewRecipe newRecipe = newDto(NewRecipe.class).withType("docker")
                                                           .withName("name")
                                                           .withScript("FROM ubuntu\n")
                                                           .withTags(asList("java", "mongo"))
                                                           .withPermissions(newDto(PermissionsDescriptor.class).withGroups(asList(group)));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(newRecipe)
                                         .when()
                                         .post(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 201);
        verify(recipeDao).create(any(ManagedRecipe.class));
        final RecipeDescriptor descriptor = unwrapDto(response, RecipeDescriptor.class);
        assertNotNull(descriptor.getId());
        assertEquals(descriptor.getName(), newRecipe.getName());
        assertEquals(descriptor.getCreator(), USER_ID);
        assertEquals(descriptor.getScript(), newRecipe.getScript());
        assertEquals(descriptor.getTags(), newRecipe.getTags());
        assertEquals(descriptor.getPermissions(), newRecipe.getPermissions());
    }

    @Test
    public void shouldNotBeAbleToCreateNewRecipeWithPublicSearchPermissionForUser() {
        final GroupDescriptor group = newDto(GroupDescriptor.class).withName("public").withAcl(asList("read", "search"));
        PermissionsDescriptor permissions = newDto(PermissionsDescriptor.class).withGroups(asList(group));
        final NewRecipe newRecipe = newDto(NewRecipe.class).withType("docker")
                                                           .withName("name")
                                                           .withScript("FROM ubuntu\n")
                                                           .withPermissions(permissions);
        when(permissionsChecker.hasPublicSearchPermission(any(PermissionsDescriptor.class))).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(newRecipe)
                                         .when()
                                         .post(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 403);
        final ServiceError error = unwrapDto(response, ServiceError.class);
        assertEquals(error.getMessage(), "User " + USER_ID + " doesn't have access to use 'public: search' permission");
    }

    @Test
    public void shouldBeAbleToCreateNewRecipeWithPublicSearchPermissionForSystemAdmin() {
        ROLES.add("system/admin");

        final GroupDescriptor group = newDto(GroupDescriptor.class).withName("public").withAcl(asList("read", "search"));
        final NewRecipe newRecipe = newDto(NewRecipe.class).withType("docker")
                                                           .withName("name")
                                                           .withScript("FROM ubuntu\n")
                                                           .withPermissions(newDto(PermissionsDescriptor.class).withGroups(asList(group)));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(newRecipe)
                                         .when()
                                         .post(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 201);
        final RecipeDescriptor descriptor = unwrapDto(response, RecipeDescriptor.class);
        assertEquals(descriptor.getPermissions(), newRecipe.getPermissions());
    }

    @Test
    public void shouldBeAbleToGetRecipeScript() throws Exception {
        final Map<String, List<String>> users = Collections.singletonMap(USER_ID, asList("read", "write"));
        final ManagedRecipe recipe = new RecipeImpl().withCreator("other-user")
                                                     .withId("recipe123")
                                                     .withScript("FROM ubuntu\n")
                                                     .withPermissions(new PermissionsImpl(users, null));
        when(recipeDao.getById(recipe.getId())).thenReturn(recipe);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "read")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/recipe/" + recipe.getId() + "/script");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.getBody().print(), recipe.getScript());
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHaveReadAccessToRecipeScript() throws Exception {
        final ManagedRecipe recipe = new RecipeImpl().withCreator("someone2")
                                                     .withId("recipe123")
                                                     .withScript("FROM ubuntu\n");
        when(recipeDao.getById(recipe.getId())).thenReturn(recipe);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "read")).thenReturn(false);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/recipe/" + recipe.getId());

        assertEquals(response.getStatusCode(), 403);
        final String expMessage = format("User %s doesn't have access to recipe %s", USER_ID, recipe.getId());
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), expMessage);
    }

    @Test
    public void shouldBeAbleToGetRecipe() throws Exception {
        final Map<String, List<String>> users = Collections.singletonMap(USER_ID, asList("read", "write"));
        final ManagedRecipe recipe = new RecipeImpl().withCreator("someone2")
                                                     .withId("recipe123")
                                                     .withName("name")
                                                     .withType("docker")
                                                     .withScript("FROM ubuntu\n")
                                                     .withTags(asList("java", "mognodb"))
                                                     .withPermissions(new PermissionsImpl(users, null));
        when(recipeDao.getById(recipe.getId())).thenReturn(recipe);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "read")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/recipe/" + recipe.getId());

        assertEquals(response.getStatusCode(), 200);
        final RecipeDescriptor descriptor = unwrapDto(response, RecipeDescriptor.class);
        assertEquals(descriptor.getId(), recipe.getId());
        assertEquals(descriptor.getName(), recipe.getName());
        assertEquals(descriptor.getType(), recipe.getType());
        assertEquals(descriptor.getScript(), recipe.getScript());
        assertEquals(descriptor.getTags(), recipe.getTags());
        assertEquals(descriptor.getCreator(), recipe.getCreator());
        assertEquals(PermissionsImpl.fromDescriptor(descriptor.getPermissions()), recipe.getPermissions());
    }

    @Test
    public void shouldBeAbleToGetCreatedRecipes() throws Exception {
        final ManagedRecipe recipe1 = new RecipeImpl().withId("id1")
                                                      .withCreator(USER_ID)
                                                      .withType("docker")
                                                      .withScript("script1 content");
        final ManagedRecipe recipe2 = new RecipeImpl().withId("id2")
                                                      .withCreator(USER_ID)
                                                      .withType("docker")
                                                      .withScript("script2 content");
        when(recipeDao.getByCreator(eq(USER_ID), any(int.class), any(int.class))).thenReturn(asList(recipe1, recipe2));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(unwrapDtoList(response, RecipeDescriptor.class).size(), 2);
    }

    @Test
    public void shouldBeAbleToSearchRecipes() throws Exception {
        final ManagedRecipe recipe1 = new RecipeImpl().withId("id1")
                                                      .withCreator(USER_ID)
                                                      .withType("docker")
                                                      .withScript("script1 content")
                                                      .withTags(asList("java"));
        final ManagedRecipe recipe2 = new RecipeImpl().withId("id2")
                                                      .withCreator(USER_ID)
                                                      .withType("docker")
                                                      .withScript("script2 content")
                                                      .withTags(asList("java", "mongodb"));
        when(recipeDao.search(eq(asList("java", "mongodb")),
                              eq("docker"),
                              any(int.class),
                              any(int.class))).thenReturn(asList(recipe1, recipe2));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .queryParameter("tags", asList("java", "mongodb"))
                                         .queryParameter("type", "docker")
                                         .get(SECURE_PATH + "/recipe/list");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(unwrapDtoList(response, RecipeDescriptor.class).size(), 2);
    }

    @Test
    public void shouldBeAbleToRemoveRecipe() throws Exception {
        final ManagedRecipe recipe = new RecipeImpl().withId("id")
                                                     .withCreator(USER_ID)
                                                     .withType("docker")
                                                     .withScript("script1 content")
                                                     .withTags(asList("java"));
        when(recipeDao.getById(recipe.getId())).thenReturn(recipe);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "write")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .delete(SECURE_PATH + "/recipe/" + recipe.getId());

        assertEquals(response.getStatusCode(), 204);
        verify(recipeDao).remove(recipe.getId());
    }

    @Test
    public void shouldNotBeAbleToRemoveRecipeIfUserDoesNotHaveWritePermission() throws Exception {
        final ManagedRecipe recipe = new RecipeImpl().withId("id")
                                                     .withCreator("some user")
                                                     .withType("docker")
                                                     .withScript("script1 content")
                                                     .withTags(asList("java"));
        when(recipeDao.getById(recipe.getId())).thenReturn(recipe);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "write")).thenReturn(false);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .delete(SECURE_PATH + "/recipe/" + recipe.getId());

        assertEquals(response.getStatusCode(), 403);
        final String expMessage = format("User %s doesn't have access to recipe %s", USER_ID, recipe.getId());
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), expMessage);
    }

    @Test
    public void shouldBeAbleToUpdateRecipe() throws Exception {
        ROLES.add("system/admin");
        final ManagedRecipe recipe = new RecipeImpl().withId("id")
                                                     .withCreator(USER_ID)
                                                     .withType("docker")
                                                     .withScript("script1 content")
                                                     .withTags(asList("java"));
        when(recipeDao.getById(recipe.getId())).thenReturn(recipe);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "write")).thenReturn(true);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "update_acl")).thenReturn(true);

        //prepare update
        GroupDescriptor group = newDto(GroupDescriptor.class).withName("public").withAcl(asList("read"));
        RecipeUpdate update = newDto(RecipeUpdate.class).withId(recipe.getId())
                                                        .withType("new-type")
                                                        .withScript("new script content")
                                                        .withTags(asList("java", "mongodb"))
                                                        .withPermissions(newDto(PermissionsDescriptor.class).withGroups(asList(group)));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(update)
                                         .when()
                                         .put(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 200);
        verify(recipeDao).update(any(RecipeUpdate.class));
        verify(recipeDao, times(2)).getById("id");
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenRecipeUpdateIsNull() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .put(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 400);
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenUpdatingRecipeWithNullId() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(newDto(RecipeUpdate.class))
                                         .when()
                                         .put(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 400);
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHaveAccessToUpdateRecipe() throws Exception {
        final ManagedRecipe recipe = new RecipeImpl().withId("id")
                                                     .withCreator("some_foreign_user")
                                                     .withType("docker")
                                                     .withScript("script1 content")
                                                     .withTags(asList("java"));
        when(recipeDao.getById(recipe.getId())).thenReturn(recipe);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "write")).thenReturn(false);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(newDto(RecipeUpdate.class).withId("id"))
                                         .when()
                                         .put(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 403);
        final String expMessage = format("User %s doesn't have access to update recipe %s", USER_ID, recipe.getId());
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), expMessage);
        verify(recipeDao).getById(recipe.getId());
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHaveAccessToUpdateRecipePermissions() throws Exception {
        final ManagedRecipe recipe = new RecipeImpl().withId("id")
                                                     .withCreator("some_foreign_user")
                                                     .withType("docker")
                                                     .withScript("script1 content")
                                                     .withTags(asList("java"));
        when(recipeDao.getById(recipe.getId())).thenReturn(recipe);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "write")).thenReturn(true);
        when(permissionsChecker.hasAccess(recipe, USER_ID, "update_acl")).thenReturn(false);

        //prepare update
        GroupDescriptor group = newDto(GroupDescriptor.class).withName("public").withAcl(asList("read"));
        RecipeUpdate update = newDto(RecipeUpdate.class).withId("id")
                                                        .withType("new-type")
                                                        .withScript("new script content")
                                                        .withTags(asList("java", "mongodb"))
                                                        .withPermissions(newDto(PermissionsDescriptor.class).withGroups(asList(group)));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(update)
                                         .when()
                                         .put(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 403);
        final String expMessage = format("User %s doesn't have access to update recipe %s permissions", USER_ID, recipe.getId());
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), expMessage);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {

        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setUser(new UserImpl("user", USER_ID, "token", ROLES, false));
        }
    }

    private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }

    private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
        return FluentIterable.from(DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass)).toList();
    }
}
