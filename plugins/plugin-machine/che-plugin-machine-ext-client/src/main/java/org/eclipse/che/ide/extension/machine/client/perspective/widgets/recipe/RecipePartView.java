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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

import javax.validation.constraints.NotNull;

/**
 * The interface defines methods to control displaying recipes.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(RecipesPartViewImpl.class)
public interface RecipePartView extends View<RecipePartView.ActionDelegate> {
    interface ActionDelegate extends BaseActionDelegate {
    }

    /**
     * Displays recipe on Permissions panel.
     *
     * @param recipe
     *         the base view of recipes
     */
    void addRecipe(@NotNull Widget recipe);

    /**
     * Removes recipe on Permissions panel.
     *
     * @param recipe
     *         the base view of recipes
     */
    void removeRecipe(@NotNull Widget recipe);

    /** Removes all recipes from view */
    void clear();

    /**
     * Sets whether this object is visible.
     *
     * @param visible
     *         <code>true</code> to show the tab, <code>false</code> to
     *         hide it
     */
    void setVisible(boolean visible);
}
