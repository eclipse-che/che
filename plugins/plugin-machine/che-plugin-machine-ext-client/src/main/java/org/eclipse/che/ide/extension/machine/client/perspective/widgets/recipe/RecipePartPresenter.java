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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.container.RecipesContainerPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.RecipeEditorPanel;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.RecipeEditorView;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.entry.RecipeEntry;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.entry.RecipeWidget;
import org.eclipse.che.ide.extension.machine.client.util.NameGenerator;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * The class defines methods which contains business logic to control recipe's script.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class RecipePartPresenter extends BasePresenter implements RecipePartView.ActionDelegate,
                                                                  RecipeEntry.ActionDelegate,
                                                                  RecipeEditorPanel.ActionDelegate,
                                                                  ActivePartChangedHandler {
    private static final String RECIPE_TYPE = "docker";

    private final RecipePartView              view;
    private final MachineLocalizationConstant locale;
    private final MachineResources            resources;
    private final RecipesContainerPresenter   recipesContainerPresenter;
    private final NotificationManager         notificationManager;
    private final RecipeServiceClient         service;
    private final DtoFactory                  dtoFactory;

    private RecipeWidget                            selectedRecipe;
    private HashMap<RecipeWidget, RecipeDescriptor> recipes;

    @Inject
    public RecipePartPresenter(RecipePartView view,
                               MachineResources resources,
                               EventBus eventBus,
                               NotificationManager notificationManager,
                               MachineLocalizationConstant locale,
                               RecipesContainerPresenter recipesContainerPresenter,
                               DtoFactory dtoFactory,
                               RecipeServiceClient service) {
        this.view = view;
        this.locale = locale;
        this.notificationManager = notificationManager;
        this.resources = resources;
        this.recipesContainerPresenter = recipesContainerPresenter;
        this.service = service;
        this.dtoFactory = dtoFactory;

        recipes = new HashMap<>();

        view.setDelegate(this);
        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    /** Shows all created recipes. */
    public void showRecipes() {
        Promise<List<RecipeDescriptor>> recipesPromise = service.getAllRecipes();
        recipesPromise.then(new Operation<List<RecipeDescriptor>>() {
            @Override
            public void apply(List<RecipeDescriptor> recipeDescriptors) throws OperationException {
                view.clear();
                for (RecipeDescriptor recipeDescriptor : recipeDescriptors) {
                    addRecipe(recipeDescriptor);
                }
                if (recipes.isEmpty()) {
                    showEditorStubPanel();
                } else {
                    selectRecipe();
                }
            }
        });
    }

    /**
     * Adds new recipe to list.
     *
     * @param recipeDescriptor
     *         a descriptor of new recipe
     */
    @NotNull
    private RecipeWidget addRecipe(@NotNull RecipeDescriptor recipeDescriptor) {
        RecipeWidget recipe = new RecipeWidget(recipeDescriptor, resources);
        recipe.setDelegate(this);
        view.addRecipe(recipe);
        recipesContainerPresenter.addRecipePanel(recipe);
        recipes.put(recipe, recipeDescriptor);

        return recipe;
    }

    /** {@inheritDoc} */
    @Override
    public void onDeleteButtonClicked() {
        final RecipeDescriptor selectedRecipeDescriptor = recipes.get(selectedRecipe);
        Promise<Void> recipeRemoved = service.removeRecipe(selectedRecipeDescriptor.getId());
        recipeRemoved.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                notificationManager.notify("Recipe \"" + selectedRecipeDescriptor.getName() + "\"  was deleted.");
                recipes.remove(selectedRecipe);
                view.removeRecipe(selectedRecipe);
                recipesContainerPresenter.removeRecipePanel(selectedRecipe);
                selectedRecipe = null;

                if (recipes.isEmpty()) {
                    showEditorStubPanel();
                } else {
                    selectRecipe();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onCloneButtonClicked() {
        RecipeDescriptor selectedRecipeDescriptor = recipes.get(selectedRecipe);

        String recipeName = NameGenerator.generateCopy(selectedRecipeDescriptor.getName(), recipes.keySet());

        NewRecipe newRecipe = dtoFactory.createDto(NewRecipe.class)
                                        .withType(selectedRecipeDescriptor.getType())
                                        .withScript(selectedRecipeDescriptor.getScript())
                                        .withName(recipeName)
                                        .withTags(selectedRecipeDescriptor.getTags());

        createRecipe(newRecipe);
    }

    /** {@inheritDoc} */
    @Override
    public void onNewButtonClicked() {
        RecipeEditorPanel stubPanel = recipesContainerPresenter.getEditorStubPanel();

        String recipeName = NameGenerator.generateCustomRecipeName(recipes.keySet());
        List<String> tags = stubPanel.getTags();
        if (recipes.isEmpty() && !stubPanel.getName().isEmpty()) {
            recipeName = stubPanel.getName();
            stubPanel.setTags(Collections.<String>emptyList());
        }

        NewRecipe newRecipe = dtoFactory.createDto(NewRecipe.class)
                                        .withType(RECIPE_TYPE)
                                        .withScript(resources.recipeTemplate().getText())
                                        .withName(recipeName)
                                        .withTags(tags);

        createRecipe(newRecipe);
    }

    private void createRecipe(@NotNull NewRecipe newRecipe) {
        Promise<RecipeDescriptor> createRecipe = service.createRecipe(newRecipe);
        createRecipe.then(new Operation<RecipeDescriptor>() {
            @Override
            public void apply(RecipeDescriptor recipeDescriptor) throws OperationException {
                RecipeWidget createdRecipeWidget = addRecipe(recipeDescriptor);
                onRecipeClicked(createdRecipeWidget);
            }
        });

        createRecipe.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                if (arg.getMessage() != null) {
                    notificationManager.notify(locale.failedToCreateRecipe(), arg.getMessage(), FAIL, FLOAT_MODE);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onSaveButtonClicked() {
        RecipeEditorPanel editorPanel = recipesContainerPresenter.getEditorPanel(selectedRecipe);
        RecipeDescriptor recipeDescriptor = selectedRecipe.getDescriptor();
        final RecipeUpdate recipeUpdate = dtoFactory.createDto(RecipeUpdate.class)
                                                    .withId(recipeDescriptor.getId())
                                                    .withType(recipeDescriptor.getType())
                                                    .withScript(editorPanel.getScript())
                                                    .withName(editorPanel.getName())
                                                    .withTags(editorPanel.getTags());
        Promise<RecipeDescriptor> updateRecipe = service.updateRecipe(recipeUpdate);
        updateRecipe.then(new Operation<RecipeDescriptor>() {
            @Override
            public void apply(RecipeDescriptor recipeDescriptor) throws OperationException {
                RecipeDescriptor selectedRecipeDescriptor = recipes.get(selectedRecipe);
                selectedRecipeDescriptor.setScript(recipeDescriptor.getScript());
                selectedRecipeDescriptor.setTags(recipeDescriptor.getTags());
                selectedRecipeDescriptor.setName(recipeDescriptor.getName());

                selectedRecipe.setName(recipeDescriptor.getName());

                notificationManager.notify("Recipe \"" + recipeDescriptor.getName() + "\" was saved.");
            }
        });

        updateRecipe.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                if (arg.getMessage() != null) {
                    notificationManager.notify(locale.failedToSaveRecipe(), arg.getMessage(), FAIL, FLOAT_MODE);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void selectRecipe() {
        LinkedList<RecipeWidget> list = new LinkedList<>(recipes.keySet());

        if (list.isEmpty() || (selectedRecipe != null && selectedRecipe.equals(list.getFirst()))) {
            return;
        }

        for (RecipeWidget recipe : recipes.keySet()) {
            recipe.unSelect();
        }

        selectedRecipe = list.getFirst();
        selectedRecipe.select();
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getTitle() {
        return locale.viewRecipePanelTitle();
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return resources.recipesPartIcon();
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public String getTitleToolTip() {
        return locale.viewRecipePanelTooltip();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
        showRecipes();
    }

    /** {@inheritDoc} */
    @Override
    public void onRecipeClicked(@NotNull RecipeWidget recipeWidget) {
        if (selectedRecipe != null && selectedRecipe.equals(recipeWidget)) {
            return;
        }

        for (RecipeWidget recipe : recipes.keySet()) {
            recipe.unSelect();
        }

        selectedRecipe = recipeWidget;
        selectedRecipe.select();
        recipesContainerPresenter.showEditorPanel(selectedRecipe);
        RecipeEditorPanel recipePanel = recipesContainerPresenter.getEditorPanel(selectedRecipe);
        recipePanel.setDelegate(this);
    }

    private void showEditorStubPanel() {
        RecipeEditorPanel editorStubPanel = recipesContainerPresenter.getEditorStubPanel();

        RecipeEditorView stubPanelView = (RecipeEditorView)editorStubPanel.getView();
        stubPanelView.setName("");
        stubPanelView.setTags(Collections.<String>emptyList());

        editorStubPanel.setVisibleSaveCancelCloneDeleteBtns(false);
        editorStubPanel.setDelegate(this);
        recipesContainerPresenter.showEditorStubPanel();
    }

    /** {@inheritDoc} */
    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        if (!(event.getActivePart() instanceof RecipePartPresenter)) {
            return;
        }
        recipesContainerPresenter.showEditorPanel(selectedRecipe);
        RecipeEditorPanel recipePanel = recipesContainerPresenter.getEditorPanel(selectedRecipe);
        recipePanel.setDelegate(this);
    }

}
