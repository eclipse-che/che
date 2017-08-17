/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.recipe;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.core.db.DBInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Loads predefined recipes.
 *
 * <p>It's used for machine template selection during
 * creation of workspace or creation any machine in workspace.
 *
 * @author Anton Korneta
 */
@Singleton
public class RecipeLoader {

    public static final String CHE_PREDEFINED_RECIPES = "che.predefined.recipes";

    private static final Logger LOG  = LoggerFactory.getLogger(RecipeLoader.class);
    private static final Gson   GSON = new GsonBuilder().create();

    protected final RecipeDao recipeDao;

    private final Set<String>   predefinedRecipes;
    private final DBInitializer dbInitializer;

    @Inject
    public RecipeLoader(@Named(CHE_PREDEFINED_RECIPES) Set<String> predefinedRecipes,
                        RecipeDao recipeDao,
                        DBInitializer dbInitializer) {
        this.predefinedRecipes = predefinedRecipes;
        this.recipeDao = recipeDao;
        this.dbInitializer = dbInitializer;
    }

    @PostConstruct
    public void start() {
        if (dbInitializer.isBareInit()) {
            for (String toLoad : predefinedRecipes) {
                loadRecipes(toLoad).forEach(this::doCreate);
            }
            LOG.info("Recipes initialization finished");
        }
    }

    protected void doCreate(OldRecipeImpl recipe) {
        try {
            try {
                recipeDao.update(recipe);
            } catch (NotFoundException ex) {
                recipeDao.create(recipe);
            }
        } catch (ServerException | ConflictException ex) {
            LOG.error("Failed to store recipe {} ", recipe.getId(), ex.getMessage());
        }
    }

    /**
     * Loads recipes by given path.
     *
     * @param recipesPath
     *         path to recipe file
     * @return list of recipes or empty list when failed to fetch recipes by given path
     */
    private List<OldRecipeImpl> loadRecipes(String recipesPath) {
        final List<OldRecipeImpl> recipes = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(IoUtil.getResource(recipesPath)))) {
            recipes.addAll(GSON.fromJson(reader, new TypeToken<List<OldRecipeImpl>>() {}.getType()));
        } catch (IOException | JsonParseException ex) {
            LOG.error("Failed to deserialize recipes from specified path " + recipesPath, ex);
        }
        return recipes;
    }

}
