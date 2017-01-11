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

import com.google.common.base.Strings;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * Handle how recipe is retrieved, either by downloading it with external location or by using the provided content.
 *
 * @author Florent Benoit
 */
public class RecipeRetriever {

    /**
     * For recipe stored on an external location, needs to delegate.
     */
    @Inject
    private RecipeDownloader recipeDownloader;

    /**
     * Gets the recipe from a machine configuration
     *
     * @param machineConfig
     *         the machine configuration that is containing the content or a location to get recipe
     * @return recipe with set content and type
     * @throws MachineException
     *         if any error occurs
     */
    public Recipe getRecipe(@NotNull MachineConfig machineConfig) throws MachineException {
        MachineSource machineSource = machineConfig.getSource();
        if (!Strings.isNullOrEmpty(machineSource.getContent())) {
            return new RecipeImpl().withType(machineSource.getType())
                                   .withScript(machineSource.getContent());
        } else {
            return recipeDownloader.getRecipe(machineConfig);
        }

    }
}
