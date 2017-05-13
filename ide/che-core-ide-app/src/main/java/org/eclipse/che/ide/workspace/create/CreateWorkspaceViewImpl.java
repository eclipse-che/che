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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The class contains business logic which allows to set up special parameters for creating user workspaces.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
class CreateWorkspaceViewImpl extends Window implements CreateWorkspaceView {

    private static final CreateWorkspaceViewImplUiBinder UI_BINDER = GWT.create(CreateWorkspaceViewImplUiBinder.class);
    @UiField(provided = true)
    final         CoreLocalizationConstant locale;
    private final PopupPanel               popupPanel;
    @UiField
    TextBox wsName;
    @UiField
    Label   nameError;
    private ActionDelegate delegate;
    private Button         createButton;

    @Inject
    public CreateWorkspaceViewImpl(CoreLocalizationConstant locale,
                                   org.eclipse.che.ide.Resources resources,
                                   FlowPanel tagsPanel) {
        this.locale = locale;

        this.popupPanel = new PopupPanel(true);
        this.popupPanel.setStyleName(resources.coreCss().createWsTagsPopup());
        this.popupPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupPanel.hide();
            }
        }, ClickEvent.getType());

        setWidget(UI_BINDER.createAndBindUi(this));

        setTitle(locale.createWsTitle());

        wsName.setText(locale.createWsDefaultName());

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
    public String getWorkspaceName() {
        return wsName.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setWorkspaceName(String name) {
        wsName.setText(name);
    }

    /** {@inheritDoc} */
    @Override
    public void showValidationNameError(String error) {
        boolean isErrorExist = !error.isEmpty();

        nameError.setVisible(isErrorExist);

        nameError.setText(error);
    }

    @UiHandler("wsName")
    public void onWorkspaceNameChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onNameChanged();
    }

    @UiHandler("wsName")
    public void onNameFieldFocused(@SuppressWarnings("UnusedParameters") ClickEvent event) {
        delegate.onNameChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface CreateWorkspaceViewImplUiBinder extends UiBinder<Widget, CreateWorkspaceViewImpl> {
    }
}
