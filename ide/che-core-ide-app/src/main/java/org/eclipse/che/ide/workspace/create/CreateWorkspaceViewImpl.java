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
package org.eclipse.che.ide.workspace.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.workspace.WorkspaceWidgetFactory;
import org.eclipse.che.ide.workspace.create.recipewidget.RecipeWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * The class contains business logic which allows to set up special parameters for creating user workspaces.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
class CreateWorkspaceViewImpl extends Window implements CreateWorkspaceView, RecipeWidget.ActionDelegate {

    interface CreateWorkspaceViewImplUiBinder extends UiBinder<Widget, CreateWorkspaceViewImpl> {
    }

    private static final CreateWorkspaceViewImplUiBinder UI_BINDER = GWT.create(CreateWorkspaceViewImplUiBinder.class);

    private static final int BORDER_WIDTH = 1;

    private final WorkspaceWidgetFactory tagFactory;
    private final PopupPanel             popupPanel;
    private final FlowPanel              tagsPanel;
    private final HidePopupCallBack      hidePopupCallBack;

    private ActionDelegate delegate;
    private Button         createButton;
    private boolean        isPredefinedRecipe;

    @UiField(provided = true)
    final CoreLocalizationConstant locale;

    @UiField
    TextBox wsName;
    @UiField
    TextBox recipeURL;
    @UiField
    Label   recipeUrlError;
    @UiField
    TextBox tags;
    @UiField
    Label   tagsError;
    @UiField
    Label   nameError;
    @UiField
    TextBox predefinedRecipes;

    @Inject
    public CreateWorkspaceViewImpl(CoreLocalizationConstant locale,
                                   org.eclipse.che.ide.Resources resources,
                                   WorkspaceWidgetFactory tagFactory,
                                   FlowPanel tagsPanel) {
        this.locale = locale;
        this.tagFactory = tagFactory;

        this.tagsPanel = tagsPanel;
        this.tagsPanel.setStyleName(resources.coreCss().tagsPanel());

        this.popupPanel = new PopupPanel(true);
        this.popupPanel.setStyleName(resources.coreCss().createWsTagsPopup());
        this.popupPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupPanel.hide();
            }
        }, ClickEvent.getType());

        this.hidePopupCallBack = new HidePopupCallBack() {
            @Override
            public void hidePopup() {
                popupPanel.hide();
            }
        };

        setWidget(UI_BINDER.createAndBindUi(this));

        setTitle(locale.createWsTitle());

        wsName.setText(locale.createWsDefaultName());

        predefinedRecipes.getElement().setPropertyString("placeholder", locale.placeholderChoosePredefined());
        recipeURL.getElement().setPropertyString("placeholder", locale.placeholderInputRecipeUrl());
        tags.getElement().setPropertyString("placeholder", locale.placeholderFindByTags());

        createButton = createButton(locale.createWsButton(), "create-workspace-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCreateButtonClicked();
            }
        });

        addButtonToFooter(createButton);
    }

    /** {@inheritDoc} */
    @Override
    public void setWorkspaceName(String name) {
        wsName.setText(name);
    }

    /** {@inheritDoc} */
    @Override
    public String getRecipeUrl() {
        return recipeURL.getText();
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getTags() {
        List<String> tagList = new ArrayList<>();

        for (String tag : tags.getValue().split(" ")) {
            if (!tag.isEmpty()) {
                tagList.add(tag.trim());
            }
        }

        return tagList;
    }

    /** {@inheritDoc} */
    @Override
    public String getWorkspaceName() {
        return wsName.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void showFoundByTagRecipes(List<RecipeDescriptor> recipes) {
        addRecipesToPanel(recipes);

        int xPanelCoordinate = tags.getAbsoluteLeft() + BORDER_WIDTH;
        int yPanelCoordinate = tags.getAbsoluteTop() + tags.getOffsetHeight();

        popupPanel.setPopupPosition(xPanelCoordinate, yPanelCoordinate);
        popupPanel.show();
    }

    private void addRecipesToPanel(List<RecipeDescriptor> recipes) {
        tagsPanel.clear();

        for (RecipeDescriptor descriptor : recipes) {
            RecipeWidget tag = tagFactory.create(descriptor);
            tag.setDelegate(this);

            tagsPanel.add(tag);
        }

        popupPanel.setWidget(tagsPanel);
    }

    /** {@inheritDoc} */
    @Override
    public void showPredefinedRecipes(List<RecipeDescriptor> recipes) {
        addRecipesToPanel(recipes);

        int xPanelCoordinate = predefinedRecipes.getAbsoluteLeft() + BORDER_WIDTH;
        int yPanelCoordinate = predefinedRecipes.getAbsoluteTop() + predefinedRecipes.getOffsetHeight();

        popupPanel.setPopupPosition(xPanelCoordinate, yPanelCoordinate);
        popupPanel.show();
    }

    /** {@inheritDoc} */
    @Override
    public void onTagClicked(RecipeWidget tag) {
        recipeURL.setText(tag.getRecipeUrl());

        predefinedRecipes.setText(isPredefinedRecipe ? tag.getTagName() : "");

        tags.setText("");

        delegate.onRecipeUrlChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleUrlError(boolean visible) {
        recipeUrlError.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleTagsError(boolean visible) {
        tagsError.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void showValidationNameError(String error) {
        boolean isErrorExist = !error.isEmpty();

        nameError.setVisible(isErrorExist);

        nameError.setText(error);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableCreateButton(boolean visible) {
        createButton.setEnabled(visible);
    }

    @UiHandler("tags")
    public void onTagsChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        tagsChangedAction();
    }

    private void tagsChangedAction() {
        String tag = tags.getText();

        tagsError.setVisible(!tag.isEmpty());

        if (!tag.isEmpty()) {
            delegate.onTagsChanged(hidePopupCallBack);
        }

        isPredefinedRecipe = false;
    }

    @UiHandler("tags")
    public void onTagsClicked(@SuppressWarnings("UnusedParameters") ClickEvent event) {
        tagsChangedAction();
    }

    @UiHandler("recipeURL")
    public void onRecipeUrlChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onRecipeUrlChanged();
    }

    @UiHandler("wsName")
    public void onWorkspaceNameChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onNameChanged();
    }

    @UiHandler("wsName")
    public void onNameFieldFocused(@SuppressWarnings("UnusedParameters") ClickEvent event) {
        delegate.onNameChanged();
    }

    @UiHandler("predefinedRecipes")
    public void onPredefineRecipesClicked(@SuppressWarnings("UnusedParameters") ClickEvent event) {
        delegate.onPredefinedRecipesClicked();

        isPredefinedRecipe = true;
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }
}
