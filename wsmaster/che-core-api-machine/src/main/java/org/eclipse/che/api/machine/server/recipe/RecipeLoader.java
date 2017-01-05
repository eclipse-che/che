/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server.recipe;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.commons.annotation.Nullable;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Collections.emptyList;

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
    private static final Gson GSON = new GsonBuilder().create();

    private final Set<String> recipesPaths;
    private final RecipeDao   recipeDao;

    @Inject
    @SuppressWarnings("unused")
    public RecipeLoader(@Nullable @Named("predefined.recipe.path") Set<String> recipesPaths,
                        RecipeDao recipeDao) {
        this.recipesPaths = firstNonNull(recipesPaths, Collections.<String>emptySet());
        this.recipeDao = recipeDao;
    }

    @PostConstruct
    public void start() throws ServerException {
        for (String recipesPath : recipesPaths) {
            if (recipesPath != null && !recipesPath.isEmpty()) {
                for (RecipeImpl recipe : loadRecipes(recipesPath)) {
                    try {
                        try {
                            recipeDao.update(recipe);
                        } catch (NotFoundException e) {
                            recipeDao.create(recipe);
                        }
                    } catch (ConflictException e) {
                        throw new ServerException("Failed to store recipe " + recipe, e);
                    }
                }
            }
        }
    }

    /**
     * Loads recipes by specified path.
     *
     * @param recipesPath
     *         path to recipe file
     * @return list of predefined recipes
     * @throws ServerException
     *         when problems occurs with getting or parsing recipe file
     */
    private List<RecipeImpl> loadRecipes(String recipesPath) throws ServerException {
        try (InputStream is = getResource(recipesPath)) {
            return firstNonNull(GSON.fromJson(new InputStreamReader(is), new TypeToken<List<RecipeImpl>>() {}.getType()), emptyList());
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            throw new ServerException("Failed to get recipes from specified path " + recipesPath, e);
        }
    }

    /**
     * Searches for resource by given path.
     *
     * @param resource
     *         path to resource
     * @return resource InputStream
     * @throws IOException
     *         when problem occurs during resource getting
     */
    private InputStream getResource(String resource) throws IOException {
        File resourceFile = new File(resource);
        if (resourceFile.exists() && !resourceFile.isFile()) {
            throw new IOException(String.format("%s is not a file. ", resourceFile.getAbsolutePath()));
        }
        InputStream is = resourceFile.exists() ? new FileInputStream(resourceFile)
                                               : Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new IOException(String.format("Not found resource: %s", resource));
        }
        return is;
    }
}
