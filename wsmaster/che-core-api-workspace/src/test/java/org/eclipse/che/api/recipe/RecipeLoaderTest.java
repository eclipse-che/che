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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.google.common.collect.ImmutableSet;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.core.db.DBInitializer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link RecipeLoader}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class RecipeLoaderTest {

  private RecipeLoader recipeLoader;

  @Mock private RecipeDao recipeDao;

  @Mock private DBInitializer dbInitializer;

  @BeforeMethod
  public void startup() throws Exception {
    when(dbInitializer.isBareInit()).thenReturn(true);
    recipeLoader = new RecipeLoader(ImmutableSet.of("recipes.json"), recipeDao, dbInitializer);
  }

  @Test
  public void shouldLoadPredefinedRecipesFromValidJson() throws Exception {
    recipeLoader.start();

    verify(recipeDao, times(2)).update(any());
  }

  @Test
  public void shouldNotThrowExceptionWhenLoadPredefinedRecipesFromInvalidJson() throws Exception {
    recipeLoader =
        new RecipeLoader(ImmutableSet.of("invalid-recipes.json"), recipeDao, dbInitializer);

    recipeLoader.start();
  }

  @Test
  public void shouldNotThrowExceptionWhenFailedToStoreRecipes() throws Exception {
    doThrow(NotFoundException.class).when(recipeDao).update(any());
    doThrow(ConflictException.class).when(recipeDao).create(any());

    recipeLoader.start();

    verify(recipeDao, atLeast(1)).update(any());
    verify(recipeDao, atLeast(1)).create(any());
  }

  @Test
  public void doNotThrowExceptionWhenFileWithRecipesBySpecifiedPathIsNotExist() throws Exception {
    recipeLoader = new RecipeLoader(ImmutableSet.of("non-existing-file"), recipeDao, dbInitializer);

    recipeLoader.start();

    verify(recipeDao, never()).update(any());
    verify(recipeDao, never()).create(any());
  }
}
