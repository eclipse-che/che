/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.recipe;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.collect.FluentIterable;
import com.jayway.restassured.response.Response;
import java.lang.reflect.Field;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.workspace.shared.recipe.NewOldRecipe;
import org.eclipse.che.api.workspace.shared.recipe.OldRecipeDescriptor;
import org.eclipse.che.api.workspace.shared.recipe.OldRecipeUpdate;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class RecipeServiceTest {

  @SuppressWarnings("unused")
  static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @SuppressWarnings("unused")
  static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  static final String USER_ID = "user123";

  @Mock RecipeDao recipeDao;
  @Mock UriInfo uriInfo;
  @InjectMocks RecipeService service;

  @BeforeMethod
  public void setUpUriInfo() throws NoSuchFieldException, IllegalAccessException {
    when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

    final Field uriField = service.getClass().getSuperclass().getDeclaredField("uriInfo");
    uriField.setAccessible(true);
    uriField.set(service, uriInfo);
  }

  @Test
  public void shouldThrowBadRequestExceptionOnCreateRecipeWithNullBody() {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .post(SECURE_PATH + "/recipe");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "OldRecipe required");
  }

  @Test
  public void shouldThrowBadRequestExceptionOnCreateRecipeWithNewRecipeWhichDoesNotHaveType() {
    final NewOldRecipe newRecipe = newDto(NewOldRecipe.class).withScript("FROM ubuntu\n");

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(newRecipe)
            .when()
            .post(SECURE_PATH + "/recipe");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "OldRecipe type required");
  }

  @Test
  public void shouldThrowBadRequestExceptionOnCreateRecipeWithNewRecipeWhichDoesNotHaveScript() {
    final NewOldRecipe newRecipe = newDto(NewOldRecipe.class).withType("docker");

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(newRecipe)
            .when()
            .post(SECURE_PATH + "/recipe");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "OldRecipe script required");
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenCreatingRecipeWithoutName() {
    final NewOldRecipe newRecipe =
        newDto(NewOldRecipe.class).withType("docker").withScript("script");

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(newRecipe)
            .when()
            .post(SECURE_PATH + "/recipe");

    assertEquals(response.getStatusCode(), 400);
    assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "OldRecipe name required");
  }

  @Test
  public void shouldCreateNewRecipe() throws Exception {
    final NewOldRecipe newRecipe =
        newDto(NewOldRecipe.class)
            .withType("docker")
            .withName("name")
            .withScript("FROM ubuntu\n")
            .withTags(asList("java", "mongo"));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(newRecipe)
            .when()
            .post(SECURE_PATH + "/recipe");

    assertEquals(response.getStatusCode(), 201);
    verify(recipeDao).create(any(OldRecipeImpl.class));
    final OldRecipeDescriptor descriptor = unwrapDto(response, OldRecipeDescriptor.class);
    assertNotNull(descriptor.getId());
    assertEquals(descriptor.getName(), newRecipe.getName());
    assertEquals(descriptor.getCreator(), USER_ID);
    assertEquals(descriptor.getScript(), newRecipe.getScript());
    assertEquals(descriptor.getTags(), newRecipe.getTags());
  }

  @Test
  public void shouldBeAbleToGetRecipeScript() throws Exception {
    final OldRecipeImpl recipe =
        new OldRecipeImpl()
            .withCreator("other-user")
            .withId("recipe123")
            .withScript("FROM ubuntu\n");
    when(recipeDao.getById(recipe.getId())).thenReturn(recipe);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/recipe/" + recipe.getId() + "/script");

    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getBody().print(), recipe.getScript());
  }

  @Test
  public void shouldBeAbleToGetRecipe() throws Exception {
    final OldRecipeImpl recipe =
        new OldRecipeImpl()
            .withCreator("someone2")
            .withId("recipe123")
            .withName("name")
            .withType("docker")
            .withScript("FROM ubuntu\n")
            .withTags(asList("java", "mognodb"));
    when(recipeDao.getById(recipe.getId())).thenReturn(recipe);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/recipe/" + recipe.getId());

    assertEquals(response.getStatusCode(), 200);
    final OldRecipeDescriptor descriptor = unwrapDto(response, OldRecipeDescriptor.class);
    assertEquals(descriptor.getId(), recipe.getId());
    assertEquals(descriptor.getName(), recipe.getName());
    assertEquals(descriptor.getType(), recipe.getType());
    assertEquals(descriptor.getScript(), recipe.getScript());
    assertEquals(descriptor.getTags(), recipe.getTags());
    assertEquals(descriptor.getCreator(), recipe.getCreator());
  }

  @Test
  public void shouldBeAbleToSearchRecipes() throws Exception {
    final OldRecipeImpl recipe1 =
        new OldRecipeImpl()
            .withId("id1")
            .withCreator(USER_ID)
            .withType("docker")
            .withScript("script1 content")
            .withTags(asList("java"));
    final OldRecipeImpl recipe2 =
        new OldRecipeImpl()
            .withId("id2")
            .withCreator(USER_ID)
            .withType("docker")
            .withScript("script2 content")
            .withTags(asList("java", "mongodb"));
    when(recipeDao.search(
            eq("user123"),
            eq(asList("java", "mongodb")),
            eq("docker"),
            any(int.class),
            any(int.class)))
        .thenReturn(asList(recipe1, recipe2));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .queryParameter("tags", asList("java", "mongodb"))
            .queryParameter("type", "docker")
            .get(SECURE_PATH + "/recipe");

    assertEquals(response.getStatusCode(), 200);
    assertEquals(unwrapDtoList(response, OldRecipeDescriptor.class).size(), 2);
  }

  @Test
  public void shouldBeAbleToRemoveRecipe() throws Exception {
    final OldRecipeImpl recipe =
        new OldRecipeImpl()
            .withId("id")
            .withCreator(USER_ID)
            .withType("docker")
            .withScript("script1 content")
            .withTags(asList("java"));
    when(recipeDao.getById(recipe.getId())).thenReturn(recipe);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/recipe/" + recipe.getId());

    assertEquals(response.getStatusCode(), 204);
    verify(recipeDao).remove(recipe.getId());
  }

  @Test
  public void shouldBeAbleToUpdateRecipe() throws Exception {
    final OldRecipeImpl recipe =
        new OldRecipeImpl()
            .withId("id")
            .withCreator(USER_ID)
            .withType("docker")
            .withScript("script1 content")
            .withTags(asList("java"));
    final OldRecipeImpl updatedRecipe =
        new OldRecipeImpl(recipe)
            .withType("new-type")
            .withScript("new script content")
            .withTags(asList("java", "mongodb"));
    when(recipeDao.update(any())).thenReturn(updatedRecipe);

    OldRecipeUpdate update =
        newDto(OldRecipeUpdate.class)
            .withId(recipe.getId())
            .withType("new-type")
            .withScript("new script content")
            .withTags(asList("java", "mongodb"));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(update)
            .when()
            .put(SECURE_PATH + "/recipe");

    assertEquals(response.getStatusCode(), 200);
    verify(recipeDao).update(any(OldRecipeImpl.class));
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenRecipeUpdateIsNull() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .put(SECURE_PATH + "/recipe");

    assertEquals(response.getStatusCode(), 400);
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenUpdatingRecipeWithNullId() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(newDto(OldRecipeUpdate.class))
            .when()
            .put(SECURE_PATH + "/recipe");

    assertEquals(response.getStatusCode(), 400);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {

    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(new SubjectImpl("user", USER_ID, "token", false));
    }
  }

  private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return FluentIterable.from(
            DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass))
        .toList();
  }
}
