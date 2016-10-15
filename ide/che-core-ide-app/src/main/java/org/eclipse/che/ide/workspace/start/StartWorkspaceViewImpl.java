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
package org.eclipse.che.ide.workspace.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.workspace.start.workspacewidget.WorkspaceWidget;

/**
 * @author Dmitry Shnurenko
 */
@Singleton
class StartWorkspaceViewImpl extends Window implements StartWorkspaceView {
    interface StartWorkspaceViewImplUiBinder extends UiBinder<Widget, StartWorkspaceViewImpl> {
    }

    private static final StartWorkspaceViewImplUiBinder UI_BINDER = GWT.create(StartWorkspaceViewImplUiBinder.class);

    private static final int BORDER_WIDTH = 1;

    private final PopupPanel popupPanel;
    private final FlowPanel  workspacesPanel;

    private ActionDelegate delegate;
    private Button         startButton;

    @UiField(provided = true)
    final CoreLocalizationConstant locale;

    @UiField
    TextBox workspaces;

    @Inject
    public StartWorkspaceViewImpl(CoreLocalizationConstant locale, org.eclipse.che.ide.Resources resources, FlowPanel workspacesPanel) {
        this.locale = locale;

        setWidget(UI_BINDER.createAndBindUi(this));

        setTitle(locale.startWsTitle());

        this.workspacesPanel = workspacesPanel;

        this.popupPanel = new PopupPanel(true);
        this.popupPanel.setStyleName(resources.coreCss().createWsTagsPopup());
        this.popupPanel.setWidget(workspacesPanel);
        this.popupPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupPanel.hide();
            }
        }, ClickEvent.getType());

        workspaces.getElement().setPropertyString("placeholder", locale.placeholderSelectWsToStart());

        startButton = createButton(locale.startWsButton(), "start-workspace-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onStartWorkspaceClicked();
            }
        });

        Button createButton = createButton(locale.createWsButton(), "create-workspace-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCreateWorkspaceClicked();
            }
        });

        addButtonToFooter(startButton);
        addButtonToFooter(createButton);
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        super.show();

        setEnableStartButton(!workspaces.getText().isEmpty());
    }

    /** {@inheritDoc} */
    @Override
    public void clearWorkspacesPanel() {
        workspacesPanel.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableStartButton(boolean enable) {
        startButton.setEnabled(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void addWorkspace(WorkspaceWidget workspace) {
        workspacesPanel.add(workspace);
    }

    /** {@inheritDoc} */
    @Override
    public void setWsName(String wsName) {
        workspaces.setText(wsName);
    }

    @UiHandler("workspaces")
    public void onWorkspacesFieldClicked(@SuppressWarnings("UnusedParameters") ClickEvent event) {
        int xPanelCoordinate = workspaces.getAbsoluteLeft() + BORDER_WIDTH;
        int yPanelCoordinate = workspaces.getAbsoluteTop() + workspaces.getOffsetHeight();

        popupPanel.setPopupPosition(xPanelCoordinate, yPanelCoordinate);
        popupPanel.show();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

}
