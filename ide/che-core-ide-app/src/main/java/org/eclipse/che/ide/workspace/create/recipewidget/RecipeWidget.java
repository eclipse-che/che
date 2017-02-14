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
package org.eclipse.che.ide.workspace.create.recipewidget;

import org.eclipse.che.ide.api.mvp.View;

/**
 * Provides methods which allows get information about tag.
 *
 * @author Dmitry Shnurenko
 */
public interface RecipeWidget extends View<RecipeWidget.ActionDelegate> {

    /** Returns special url via which we can get recipe script. */
    String getRecipeUrl();

    /** Returns tag name associated to recipe. */
    String getTagName();

    interface ActionDelegate {
        /**
         * Performs some actions when user clicks on tag.
         *
         * @param tag
         *         tag which was selected
         */
        void onTagClicked(RecipeWidget tag);
    }
}
