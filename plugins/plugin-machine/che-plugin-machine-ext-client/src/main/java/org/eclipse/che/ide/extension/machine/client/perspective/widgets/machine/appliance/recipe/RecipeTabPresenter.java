/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.recipe;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.extension.machine.client.RecipeScriptDownloadServiceClient;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;

import javax.validation.constraints.NotNull;

/**
 * The class contains business logic which allows update a recipe for current machine. The class is a tab presenter and
 * shows current machine recipe.
 *
 * @author Valeriy Svydenko
 */
public class RecipeTabPresenter implements TabPresenter {

    private final RecipeView                        view;
    private final RecipeScriptDownloadServiceClient recipeScriptClient;

    @Inject
    public RecipeTabPresenter(RecipeView view, RecipeScriptDownloadServiceClient recipeScriptClient) {
        this.view = view;
        this.recipeScriptClient = recipeScriptClient;
    }

    /**
     * Calls special method on view which updates recipe of current machine.
     * If case of 'image', it is it's location, in case of 'dockerfile'
     * - it is content, that is fecthed by location URL
     *
     * @param machine
     *         machine for which need update information
     */
    public void updateInfo(@NotNull final Machine machine) {
        /*if (machine.getRecipeType() == null) {
            Log.error(RecipeTabPresenter.class, "Recipe type is null for machine '" + machine.getId() + "'");
            view.setScript("Recipe type is null for machine '" + machine.getId() + "'");
            return;
        }
        switch (machine.getRecipeType()) {
            case "image":
                view.setScript("Image location: " + machine.getRecipeLocation());
                break;
            case "dockerfile":
                recipeScriptClient.getRecipeScript(machine).then(new Operation<String>() {
                    @Override
                    public void apply(String recipe) throws OperationException {
                        view.setScript(recipe);
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError error) throws OperationException {
                        Log.error(RecipeTabPresenter.class,
                                  "Failed to get recipe script for machine " + machine.getId() + ": " + error.getMessage());
                        view.setScript("Failed to get recipe script for machine '" + machine.getId() + "'");
                    }
                });
                break;
            case "default":
                view.setScript("Recipe type: " + machine.getRecipeType());
        }*/
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
