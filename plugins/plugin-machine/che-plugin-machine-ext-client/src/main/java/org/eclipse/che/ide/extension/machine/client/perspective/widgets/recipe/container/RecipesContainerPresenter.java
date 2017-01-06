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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.container;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.RecipeEditorPanel;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.RecipeEditorView;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.entry.RecipeWidget;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Valeriy Svydenko
 */
@Singleton
public class RecipesContainerPresenter implements TabPresenter, RecipesContainerView.ActionDelegate {
    private final RecipesContainerView view;
    private final EntityFactory        entityFactory;
    private final RecipeEditorPanel    editorStubPanel;

    private Map<RecipeWidget, RecipeEditorPanel> recipePanels;

    @Inject
    public RecipesContainerPresenter(EntityFactory entityFactory, RecipesContainerView view) {
        this.entityFactory = entityFactory;
        this.view = view;

        recipePanels = new HashMap<>();
        view.setDelegate(this);

        editorStubPanel = entityFactory.createRecipeEditorPanel(null);
        editorStubPanel.setEnableSaveCancelCloneDeleteBtns(false);
    }

    /**
     * Adds new recipe panel to container.
     *
     * @param recipe
     *         current recipe widget
     */
    public void addRecipePanel(@NotNull RecipeWidget recipe) {
        if (recipePanels.get(recipe) != null) {
            return;
        }
        RecipeEditorPanel editorPanel = entityFactory.createRecipeEditorPanel(recipe.getDescriptor());
        recipePanels.put(recipe, editorPanel);

        RecipeEditorView editorView = ((RecipeEditorView)editorPanel.getView());
        RecipeDescriptor recipeDescriptor = recipe.getDescriptor();
        editorView.setScriptUrl(recipeDescriptor.getLink("get recipe script").getHref());

        editorView.setTags(recipeDescriptor.getTags());
        editorView.setName(recipeDescriptor.getName());
    }

    /**
     * Removes recipe panel from container.
     *
     * @param recipe
     *         current recipe widget
     */
    public void removeRecipePanel(@NotNull RecipeWidget recipe) {
        if (recipePanels.get(recipe) != null) {
            recipePanels.remove(recipe);
        }
    }

    /**
     * Shows recipe panel into container.
     *
     * @param recipe
     *         current recipe widget
     */
    public void showEditorPanel(@NotNull RecipeWidget recipe) {
        RecipeEditorPanel recipeEditorPanel = recipePanels.get(recipe);
        recipeEditorPanel.showEditor();

        view.showWidget(recipeEditorPanel.getView());
    }

    /**
     * Returns recipe panel without the editor part.
     *
     * @param recipe
     *         current recipe widget
     */
    @NotNull
    public RecipeEditorPanel getEditorPanel(@NotNull RecipeWidget recipe) {
        return recipePanels.get(recipe);
    }

    /**
     * Shows recipe panel without the editor part.
     */
    public void showEditorStubPanel() {
        view.showWidget(editorStubPanel.getView());
    }

    /** Returns recipe panel without the editor part. */
    public RecipeEditorPanel getEditorStubPanel() {
        return editorStubPanel;
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}