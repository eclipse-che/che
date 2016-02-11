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
package org.eclipse.che.plugin.docker.ext.client.manage.input;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import static org.eclipse.che.ide.ui.window.Window.Resources;

/**
 * The footer for input dialog {@link InputDialogViewImpl}
 *
 * @author Sergii Leschenko
 */
public class InputDialogFooter extends Composite {
    interface ConfirmWindowFooterUiBinder extends UiBinder<Widget, InputDialogFooter> {}

    @UiField
    Button saveButton;

    @UiField
    Button cancelButton;

    private InputDialogView.ActionDelegate actionDelegate;

    @Inject
    public InputDialogFooter(Resources resources, ConfirmWindowFooterUiBinder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        saveButton.addStyleName(resources.windowCss().primaryButton());
        saveButton.getElement().setId("inputCredentials-dialog-save");
        cancelButton.addStyleName(resources.windowCss().button());
        cancelButton.getElement().setId("inputCredentials-dialog-cancel");
    }

    public void setDelegate(final InputDialogView.ActionDelegate delegate) {
        this.actionDelegate = delegate;
    }

    @UiHandler("saveButton")
    public void handleOkClick(final ClickEvent event) {
        this.actionDelegate.accepted();
    }

    @UiHandler("cancelButton")
    public void handleCancelClick(final ClickEvent event) {
        this.actionDelegate.cancelled();
    }

    Button getSaveButton() {
        return saveButton;
    }
}
