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
package org.eclipse.che.api.workspace.server;

import javax.inject.Inject;

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

//    /**
//     * Gets the recipe from a machine configuration
//     *
//     * @param machineConfig
//     *         the machine configuration that is containing the content or a location to get recipe
//     * @return recipe with set content and type
//     * @throws ServerException
//     *         if any error occurs
//     */
//    public OldRecipe getRecipe(@NotNull MachineConfig machineConfig) throws ServerException {
//        MachineSource machineSource = machineConfig.getSource();
//        if (!Strings.isNullOrEmpty(machineSource.getContent())) {
//            return new OldRecipeImpl().withType(machineSource.getType())
//                                      .withScript(machineSource.getContent());
//        } else {
//            return recipeDownloader.getRecipe(machineConfig);
//        }
//
//    }
}
