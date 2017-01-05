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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class RecipeLoaderTest {

    private RecipeLoader recipeLoader;

    @Mock
    private RecipeDao recipeDao;

    @Test
    public void shouldLoadPredefinedRecipesFromValidJson() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("recipes.json");
        if (url != null) {
            recipeLoader = new RecipeLoader(Collections.singleton(url.getPath()), recipeDao);
        }

        recipeLoader.start();

        verify(recipeDao, times(2)).update(any());
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Failed to get recipes from specified path.*")
    public void shouldThrowExceptionWhenLoadPredefinedRecipesFromInvalidJson() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("invalid-recipes.json");
        if (url != null) {
            recipeLoader = new RecipeLoader(Collections.singleton(url.getPath()), recipeDao);
        }

        recipeLoader.start();
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Failed to store recipe.*")
    public void shouldThrowExceptionWhenImpossibleToStoreRecipes() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("recipes.json");
        if (url != null) {
            recipeLoader = new RecipeLoader(Collections.singleton(url.getPath()), recipeDao);
        }
        doThrow(NotFoundException.class).when(recipeDao).update(any());
        doThrow(ConflictException.class).when(recipeDao).create(any());

        recipeLoader.start();
    }
}
