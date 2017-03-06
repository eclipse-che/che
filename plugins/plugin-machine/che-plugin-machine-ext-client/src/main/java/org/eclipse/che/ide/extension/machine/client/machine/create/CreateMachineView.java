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
package org.eclipse.che.ide.extension.machine.client.machine.create;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 * The view of {@link CreateMachinePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface CreateMachineView extends View<CreateMachineView.ActionDelegate> {

    /** Show view. */
    void show();

    /** Close view. */
    void close();

    /** Returns machine name. */
    String getMachineName();

    /** Sets machine name. */
    void setMachineName(String name);

    /** Returns recipe URL. */
    String getRecipeURL();

    /** Sets recipe URL. */
    void setRecipeURL(String url);

    /** Sets error hint visibility. */
    void setErrorHint(boolean show);

    /** Returns tags. */
    List<String> getTags();

    /** Sets tags. */
    void setTags(String tags);

    /** Sets 'no recipe' hint visibility. */
    void setNoRecipeHint(boolean show);

    /** Sets recipes corresponded to tags. */
    void setRecipes(List<RecipeDescriptor> recipes);

    /**
     * Sets whether 'Create' button is enabled.
     *
     * @param enabled
     *         <code>true</code> to enable the button,
     *         <code>false</code> to disable it
     */
    void setCreateButtonState(boolean enabled);

    /**
     * Sets whether 'Replace' button is enabled.
     *
     * @param enabled
     *         <code>true</code> to enable the button,
     *         <code>false</code> to disable it
     */
    void setReplaceButtonState(boolean enabled);

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {

        /** Called when machines name has been changed. */
        void onNameChanged();

        /** Called when recipe URL has been changed. */
        void onRecipeUrlChanged();

        /** Called when tags has been changed. */
        void onTagsChanged();

        /**
         * Called when recipe has been selected.
         *
         * @param recipe
         *         selected recipe
         */
        void onRecipeSelected(RecipeDescriptor recipe);

        /** Called when 'Create' button has been clicked. */
        void onCreateClicked();

        /** Called when 'Replace Dev Machine' button has been clicked. */
        void onReplaceDevMachineClicked();

        /** Called when 'Cancel' button has been clicked. */
        void onCancelClicked();
    }
}
