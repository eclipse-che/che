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
package org.eclipse.che.api.machine.server.util;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Test of {@link RecipeRetriever} class
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class RecipeRetrieverTest {

    /**
     * Typ of the recipe used in tests.
     */
    private static final String RECIPE_TYPE = "MY_TYPE";

    /**
     * Downloader instance that might be used by recipe retriever for type = location.
     */
    @Mock
    private RecipeDownloader recipeDownloader;

    /**
     * Machine config sent to recipe retriever.
     */
    @Mock
    private MachineConfig machineConfig;

    /**
     * Machine source embedded in machine config.
     */
    @Mock
    private MachineSource machineSource;

    /**
     * Instance used in tests.
     */
    @InjectMocks
    private RecipeRetriever recipeRetriever;


    /**
     * Setup the rules used in all tests.
     */
    @BeforeMethod
    public void init() {
        when(machineConfig.getSource()).thenReturn(machineSource);
        when(machineSource.getType()).thenReturn(RECIPE_TYPE);
    }


    /**
     * Check that when content is set in machine source, recipe is based on this content.
     * @throws MachineException if recipe is not retrieved
     */
    @Test
    public void checkWithContent() throws MachineException {
        String RECIPE = "FROM TOTO";
        when(machineSource.getContent()).thenReturn(RECIPE);
        Recipe recipe = recipeRetriever.getRecipe(machineConfig);
        Assert.assertNotNull(recipe);
        assertEquals(recipe.getType(), RECIPE_TYPE);
        assertEquals(recipe.getScript(), RECIPE);
    }


    /**
     * Check that when location is set in machine source, recipe retriever ask the recipe downloader.
     * @throws MachineException if recipe is not retrieved
     */
    @Test
    public void checkWithLocation() throws MachineException {
        String LOCATION = "http://eclipse.org/my-che.recipe";
        when(machineSource.getLocation()).thenReturn(LOCATION);
        recipeRetriever.getRecipe(machineConfig);
        verify(recipeDownloader).getRecipe(machineConfig);
    }
}
