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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.inject.factories.WidgetsFactory;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidget;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidgetImpl;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Valeriy Svydenko
 */
public class RecipeEditorViewImpl extends Composite implements RecipeEditorView {

    interface PropertiesPanelViewImplUiBinder extends UiBinder<Widget, RecipeEditorViewImpl> {
    }

    private static final PropertiesPanelViewImplUiBinder UI_BINDER = GWT.create(PropertiesPanelViewImplUiBinder.class);

    @UiField
    FlowPanel buttonsPanel;
    @UiField
    FlowPanel tagsPanel;
    @UiField
    FlowPanel urlPanel;
    @UiField
    FlowPanel namePanel;

    @UiField
    DockLayoutPanel   recipePanel;
    @UiField
    SimpleLayoutPanel editorPanel;
    @UiField
    TextBox           scriptUrl;
    @UiField
    TextBox           tags;
    @UiField
    TextBox           name;

    @UiField(provided = true)
    final MachineResources resources;

    private final WidgetsFactory widgetFactory;

    private ActionDelegate     delegate;
    private EditorButtonWidget saveBtn;
    private EditorButtonWidget cloneBtn;
    private EditorButtonWidget cancelBtn;
    private EditorButtonWidget deleteBtn;

    @Inject
    public RecipeEditorViewImpl(MachineResources resources, MachineLocalizationConstant locale, WidgetsFactory widgetFactory) {
        this.resources = resources;
        this.widgetFactory = widgetFactory;

        initWidget(UI_BINDER.createAndBindUi(this));

        EditorButtonWidget.ActionDelegate createDelegate = new EditorButtonWidget.ActionDelegate() {
            @Override
            public void onButtonClicked() {
                delegate.onNewButtonClicked();
            }
        };
        createButton(locale.editorButtonNew(), createDelegate, EditorButtonWidgetImpl.Background.BLUE);

        EditorButtonWidget.ActionDelegate cloneDelegate = new EditorButtonWidget.ActionDelegate() {
            @Override
            public void onButtonClicked() {
                delegate.onCloneButtonClicked();
            }
        };
        cloneBtn = createButton(locale.editorButtonClone(), cloneDelegate, EditorButtonWidgetImpl.Background.GREY);

        EditorButtonWidget.ActionDelegate saveDelegate = new EditorButtonWidget.ActionDelegate() {
            @Override
            public void onButtonClicked() {
                delegate.onSaveButtonClicked();
            }
        };
        saveBtn = createButton(locale.editorButtonSave(), saveDelegate, EditorButtonWidgetImpl.Background.GREY);

        EditorButtonWidget.ActionDelegate deleteDelegate = new EditorButtonWidget.ActionDelegate() {
            @Override
            public void onButtonClicked() {
                delegate.onDeleteButtonClicked();
            }
        };
        deleteBtn = createButton(locale.editorButtonDelete(), deleteDelegate, EditorButtonWidgetImpl.Background.GREY);

        EditorButtonWidget.ActionDelegate cancelDelegate = new EditorButtonWidget.ActionDelegate() {
            @Override
            public void onButtonClicked() {
                delegate.onCancelButtonClicked();
            }
        };
        cancelBtn = createButton(locale.editorButtonCancel(), cancelDelegate, EditorButtonWidgetImpl.Background.GREY);
    }

    @NotNull
    private EditorButtonWidget createButton(@NotNull String title,
                                            @NotNull EditorButtonWidget.ActionDelegate delegate,
                                            @NotNull EditorButtonWidgetImpl.Background background) {
        EditorButtonWidget button = widgetFactory.createEditorButton(title, background);
        button.setDelegate(delegate);

        buttonsPanel.add(button);

        return button;
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void setScriptUrl(@NotNull String url) {
        scriptUrl.setText(url);
    }

    /** {@inheritDoc} */
    @Override
    public void setTags(@NotNull List<String> tags) {
        StringBuilder stringTags = new StringBuilder();
        for (String tag : tags) {
            stringTags.append(tag).append(" ");
        }
        this.tags.setText(stringTags.toString());
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getScriptUrl() {
        return scriptUrl.getText();
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getName() {
        return name.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@NotNull String name) {
        this.name.setText(name);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public List<String> getTags() {
        List<String> tagList = new ArrayList<>();

        for (String tag : tags.getText().split(" ")) {
            if (!tag.isEmpty()) {
                tagList.add(tag.trim());
            }
        }

        return tagList;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableSaveButton(boolean enable) {
        saveBtn.setEnable(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableCancelButton(boolean enable) {
        cancelBtn.setEnable(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableDeleteButton(boolean enable) {
        deleteBtn.setEnable(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleSaveButton(boolean visible) {
        saveBtn.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleDeleteButton(boolean visible) {
        deleteBtn.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleCancelButton(boolean visible) {
        cancelBtn.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableCloneButton(boolean enable) {
        cloneBtn.setEnable(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleCloneButton(boolean visible) {
        cloneBtn.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void showEditor(@NotNull EditorPartPresenter editor) {
        editor.go(editorPanel);
    }

    /** {@inheritDoc} */
    @Override
    public void hideButtonsPanel() {
        recipePanel.setWidgetHidden(buttonsPanel, true);
    }

    @UiHandler({"tags", "name"})
    public void onTextInputted(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        cancelBtn.setEnable(true);
        saveBtn.setEnable(true);
    }

}