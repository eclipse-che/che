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
package org.eclipse.che.ide.extension.machine.client.util;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.entry.RecipeWidget;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The class contains business logic which allows us to generate names for environments
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public class NameGenerator {
    private static final String CUSTOM_RECIPE_NAME = "RECIPE";

    /** Utility class for name generation.*/
    private NameGenerator() {

    }


    private static String removeCopyPrefix(String name) {
        RegExp regexp = RegExp.compile("Copy\\d* of (.*)");
        MatchResult matchResult = regexp.exec(name);
        // do not find prefix, return as this
        if (matchResult == null || matchResult.getGroupCount() != 2) {
            return name;
        }
        return matchResult.getGroup(1);
    }

    /**
     * @return recipe name which consists of string 'Copy of ' and existing name with a current date. If there is an existing name,
     * add a number suffix like "Copy2 of", "Copy3 of", etc.
     */
    @NotNull
    public static String generateCopy(@NotNull String name, @NotNull Set<RecipeWidget> recipeWidgets) {
        List<String> existingNames = new ArrayList<>();

        for (RecipeWidget recipe : recipeWidgets) {
            existingNames.add(recipe.getDescriptor().getName());
        }

        name = removeCopyPrefix(name);
        name = name.replace("+", "");

        String copyName = "Copy of ".concat(name);
        boolean alreadyExists = existingNames.contains(copyName);
        int index = 2;
        while (alreadyExists) {
            copyName = "Copy".concat(String.valueOf(index)).concat(" of ").concat(name);
            alreadyExists = existingNames.contains(copyName);
            index++;
        }

        return copyName;
    }

    /**
     * Gets recipe name which is creating from scratch.
     *
     * @param recipeWidgets
     *         list of existing recipes
     * @return name of new recipe
     */
    public static String generateCustomRecipeName(@NotNull Set<RecipeWidget> recipeWidgets) {
        int counter = 1;
        String name = CUSTOM_RECIPE_NAME + '-' + String.valueOf(counter);
        List<Object> recipes = Arrays.asList(recipeWidgets.toArray());
        int recipeCounter = 0;
        while (recipeCounter < recipes.size()) {
            if (((RecipeWidget)recipes.get(recipeCounter)).getDescriptor().getName().equals(name)) {
                counter++;
                name = CUSTOM_RECIPE_NAME + '-' + String.valueOf(counter);
                recipeCounter = 0;
            } else {
                recipeCounter++;
            }
        }

        return name;
    }

}