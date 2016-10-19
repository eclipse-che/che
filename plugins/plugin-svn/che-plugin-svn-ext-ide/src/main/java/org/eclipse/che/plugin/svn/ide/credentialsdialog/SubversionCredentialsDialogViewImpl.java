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
package org.eclipse.che.plugin.svn.ide.credentialsdialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;

/**
 * Implementation of {@link SubversionCredentialsDialogView}
 *
 * @author Igor Vinokur
 */
public class SubversionCredentialsDialogViewImpl extends Window implements SubversionCredentialsDialogView {

    @UiField(provided = true)
    final SubversionExtensionLocalizationConstants locale;

    interface SubversionAuthenticatorImplUiBinder extends UiBinder<Widget, SubversionCredentialsDialogViewImpl> {
    }

    private static SubversionAuthenticatorImplUiBinder uiBinder = GWT.create(SubversionAuthenticatorImplUiBinder.class);

    private ActionDelegate delegate;

    @UiField
    TextBox         usernameTextBox;
    @UiField
    PasswordTextBox passwordTextBox;

    private final Button authenticateButton;

    @Inject
    public SubversionCredentialsDialogViewImpl(SubversionExtensionLocalizationConstants locale) {
        this.locale = locale;
        this.setWidget(uiBinder.createAndBindUi(this));
        this.setTitle(locale.credentialsDialogTitle());

        authenticateButton = createPrimaryButton(locale.credentialsDialogAuthenticateButton(),
                                                 "svn-authentication-username",
                                                 new ClickHandler() {
                                                     @Override
                                                     public void onClick(ClickEvent event) {
                                                         delegate.onAuthenticateClicked();
                                                     }
                                                 });
        Button cancelButton = createButton(locale.credentialsDialogCancelButton(),
                                           "svn-authentication-password",
                                           new ClickHandler() {
                                               @Override
                                               public void onClick(ClickEvent event) {
                                                   delegate.onCancelClicked();
                                               }
                                           });

        addButtonToFooter(authenticateButton);
        addButtonToFooter(cancelButton);
    }

    @Override
    public void showDialog() {
        super.show();
    }

    @Override
    public void closeDialog() {
        super.hide();
    }

    @Override
    public String getUsername() {
        return usernameTextBox.getText();
    }

    @Override
    public String getPassword() {
        return passwordTextBox.getText();
    }

    @Override
    public void cleanCredentials() {
        usernameTextBox.setText("");
        passwordTextBox.setText("");
        setEnabledAuthenticateButton(false);
    }

    @UiHandler({"usernameTextBox", "passwordTextBox"})
    void credentialChangeHandler(KeyUpEvent event) {
        delegate.onCredentialsChanged();
    }

    @Override
    public void setEnabledAuthenticateButton(boolean enabled) {
        authenticateButton.setEnabled(enabled);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }
}
