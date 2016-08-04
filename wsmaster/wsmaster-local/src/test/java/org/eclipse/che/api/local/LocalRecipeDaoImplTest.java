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
package org.eclipse.che.api.local;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for {@link LocalRecipeDaoImpl}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class LocalRecipeDaoImplTest {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting()
                                                .create();

    private LocalRecipeDaoImpl recipeDao;
    private Path               recipesPath;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        final Path targetDir = Paths.get(url.toURI()).getParent();
        final Path storageRoot = targetDir.resolve("recipes");
        recipesPath = storageRoot.resolve("recipes.json");
        recipeDao = new LocalRecipeDaoImpl(new LocalStorageFactory(storageRoot.toString()));
    }

    @Test
    public void testRecipesSerialization() throws Exception {
        final RecipeImpl recipe = createRecipe();

        recipeDao.create(recipe);
        recipeDao.saveRecipes();

        assertEquals(GSON.toJson(singletonMap(recipe.getId(), recipe)), new String(readAllBytes(recipesPath)));
    }

    @Test
    public void testRecipeSnapshotsDeserialization() throws Exception {
        final RecipeImpl recipe = createRecipe();
        Files.write(recipesPath, GSON.toJson(singletonMap(recipe.getId(), recipe)).getBytes());

        recipeDao.loadRecipes();

        final RecipeImpl result = recipeDao.getById(recipe.getId());
        assertEquals(result, recipe);
    }

    @Test
    public void shouldBeAbleToSaveAndGetRecipe() throws Exception {
        final RecipeImpl recipe = createRecipe();

        recipeDao.create(recipe);
        final RecipeImpl stored = recipeDao.getById(recipe.getId());

        assertEquals(recipe, stored);
    }

    @Test
    public void shouldBeAbleToUpdateRecipe() throws Exception {
        recipeDao.create(createRecipe());
        final RecipeImpl newRecipe = createRecipe().withDescription("new description");

        final RecipeImpl stored = recipeDao.update(newRecipe);

        assertEquals(newRecipe, stored);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Recipe with id recipe123 was not found")
    public void shouldBeAbleToRemoveRecipe() throws Exception {
        final RecipeImpl recipe = createRecipe();
        recipeDao.create(recipe);

        recipeDao.remove(recipe.getId());

        recipeDao.getById(recipe.getId());
    }

    @Test
    public void shouldBeAbleToSearchRecipeByTag() throws Exception {
        final RecipeImpl toFind = createRecipe();
        recipeDao.create(toFind);
        recipeDao.create(createRecipe().withId("recipe321")
                                       .withType("custom")
                                       .withTags(emptyList()));

        final List<RecipeImpl> search = recipeDao.search("creator", singletonList("java"), "dockerfile", 0, 0);

        assertEquals(search.size(), 1);
        assertEquals(search.get(0), toFind);
    }

    private RecipeImpl createRecipe() {
        return new RecipeImpl("recipe123",
                              "Test Recipe",
                              "creator",
                              "dockerfile",
                              "FROM che/ubuntu",
                              asList("java", "ubuntu"),
                              "Che ubuntu");
    }
}
