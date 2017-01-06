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
package org.eclipse.che.ide.workspace.create;

import com.google.inject.ImplementedBy;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 * Provides methods which allow to set up special parameters for creating user workspaces.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(CreateWorkspaceViewImpl.class)
interface CreateWorkspaceView extends View<CreateWorkspaceView.ActionDelegate> {

    /** Shows dialog window to set up creating workspace. */
    void show();

    /** Hides dialog window. */
    void hide();

    /**
     * Sets name for workspace in special place on view
     *
     * @param name
     *         name which will be set
     */
    void setWorkspaceName(String name);

    /** Returns special recipe url to get docker image. */
    String getRecipeUrl();

    /** Returns list of tags using which we will find recipes. */
    List<String> getTags();

    /** Returns name of workspace from special place on view. */
    String getWorkspaceName();

    /**
     * Sets list of recipes found by tag to special place on view.
     *
     * @param recipes
     *         recipes which will be shown
     */
    void showFoundByTagRecipes(List<RecipeDescriptor> recipes);

    /**
     * Sets list of predefined recipes to special place on view.
     *
     * @param recipes
     *         recipes which will be shown
     */
    void showPredefinedRecipes(List<RecipeDescriptor> recipes);

    /**
     * Changes visibility of error message for recipe url.
     *
     * @param visible
     *         <code>true</code> error message is visible, <code>false</code> error message is not visible
     */
    void setVisibleUrlError(boolean visible);

    /**
     * Changes visibility of error message for tags.
     *
     * @param visible
     *         <code>true</code> error message is visible, <code>false</code> error message is not visible
     */
    void setVisibleTagsError(boolean visible);

    /**
     * Shows error message for workspace name.
     *
     * @param error
     *         error message which will be shown
     */
    void showValidationNameError(String error);

    /**
     * Changes enabling of create workspace button.
     *
     * @param visible
     *         <code>true</code> button is enable, <code>false</code> button is not enable
     */
    void setEnableCreateButton(boolean visible);

    interface ActionDelegate {
        /** Performs some actions when user clicks on create workspace button. */
        void onCreateButtonClicked();

        /** Performs some actions when user change name of workspace. */
        void onNameChanged();

        /** Performs some actions when user change recipe url. */
        void onRecipeUrlChanged();

        /** Performs some actions when user change tags. */
        void onTagsChanged(HidePopupCallBack hidePopupCallBack);

        /** Performs some actions when user clicks on predefined recipes field. */
        void onPredefinedRecipesClicked();
    }

    interface HidePopupCallBack {
        /** Hides popup with tags. */
        void hidePopup();
    }
}
