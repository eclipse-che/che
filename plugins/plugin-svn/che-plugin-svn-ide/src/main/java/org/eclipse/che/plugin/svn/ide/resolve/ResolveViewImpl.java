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
package org.eclipse.che.plugin.svn.ide.resolve;

import java.util.HashMap;

import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;

import org.eclipse.che.ide.ui.window.Window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ResolveViewImpl extends Window implements ResolveView {

    interface ResolveViewImplUiBinder extends UiBinder<Widget, ResolveViewImpl> {
    }

    private static ResolveViewImplUiBinder   ourUiBinder = GWT.create(ResolveViewImplUiBinder.class);

    @UiField
    HTMLPanel                                mainPanel;

    Button                                   btnResolve;
    Button                                   btnCancel;

    private final ConflictResolutionAction[] conflictActions;
    private ActionDelegate                   delegate;
    private HashMap<String, ListBox>         filesResolutionActions;

    @Inject
    public ResolveViewImpl(final SubversionExtensionLocalizationConstants constants) {
        Widget widget = ourUiBinder.createAndBindUi(this);

        this.setTitle(constants.resolvedTitle());
        this.setWidget(widget);

        conflictActions = ConflictResolutionAction.values();

        filesResolutionActions = new HashMap<String, ListBox>();

        btnCancel = createButton(constants.buttonCancel(), "svn-resolve-cancel", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        btnResolve = createButton(constants.buttonResolve(), "svn-resolve-resolve", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onResolveClicked();
            }
        });
        btnResolve.addStyleName(resources.windowCss().button());

        addButtonToFooter(btnResolve);
        addButtonToFooter(btnCancel);
    }

    @Override
    public void addConflictingFile(String filePath) {
        Label filePathLabel = new Label(filePath);
        filePathLabel.getElement().getStyle().setFloat(Float.LEFT);
        filePathLabel.getElement().getStyle().setMarginRight(1, Unit.EM);

        ListBox conflictResolutionActions = new ListBox();
        for (ConflictResolutionAction action : conflictActions) {
            conflictResolutionActions.addItem(action.getText());
        }
        filesResolutionActions.put(filePath, conflictResolutionActions);
        
        Element resolutionDiv = DOM.createDiv();
        resolutionDiv.getStyle().setMarginLeft(1, Unit.EM);
        resolutionDiv.getStyle().setMarginRight(1, Unit.EM);
        resolutionDiv.getStyle().setMarginBottom(1, Unit.EM);
        resolutionDiv.getStyle().setTextAlign(TextAlign.RIGHT);

        resolutionDiv.appendChild(filePathLabel.getElement());
        resolutionDiv.appendChild(conflictResolutionActions.getElement());

        mainPanel.getElement().appendChild(resolutionDiv);
    }

    @Override
    public String getConflictResolutionAction(String filePath) {
        ListBox conflictResolutionActions = filesResolutionActions.get(filePath);
        int selectedIndex = conflictResolutionActions.getSelectedIndex();
        return conflictResolutionActions.getValue(selectedIndex);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
        mainPanel.getElement().removeAllChildren();
        filesResolutionActions.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.show();
    }

    @Override
    protected void onClose() {
        mainPanel.getElement().removeAllChildren();
        super.onClose();
    }
}
