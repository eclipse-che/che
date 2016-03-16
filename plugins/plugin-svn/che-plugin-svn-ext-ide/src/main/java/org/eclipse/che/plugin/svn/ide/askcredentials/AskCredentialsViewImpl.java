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
package org.eclipse.che.plugin.svn.ide.askcredentials;

import javax.inject.Inject;

import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.ide.ui.window.Window;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AskCredentialsViewImpl extends Window implements AskCredentialsView {

    private AskCredentialsDelegate delegate;

    @UiField(provided = true)
    SubversionExtensionLocalizationConstants locale;

    @UiField(provided = true)
    SubversionExtensionResources res;

    @UiField
    Element repositoryUrl;

    @UiField
    TextBox username;

    @UiField
    PasswordTextBox password;

    private final Button btnCancel;
    private final Button btnSave;

    @UiTemplate(value = "AskCredentialsViewImpl.ui.xml")
    interface AskCredentialsViewImplUiBinder extends UiBinder<Widget, AskCredentialsViewImpl> {}

    @Inject
    public AskCredentialsViewImpl(final AskCredentialsViewImplUiBinder uibinder,
                                  final SubversionExtensionLocalizationConstants constants,
                                  final SubversionExtensionResources resources,
                                  final Window.Resources windowResources) {
        super(true);
        this.locale = constants;
        this.res = resources;
        final Widget widget = uibinder.createAndBindUi(this);

        this.setTitle(locale.askCredentialsTitle());
        this.setWidget(widget);

        btnCancel = createButton(locale.askCredentialsCancel(), "svn-askcred-cancel", new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        btnSave = createButton(locale.askCredentialsValidate(), "svn-askcred-save", new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                delegate.onSaveClicked();
            }
        });
        btnSave.addStyleName(windowResources.windowCss().button());

        getFooter().add(btnSave);
        getFooter().add(btnCancel);
    }

    @Override
    protected void onClose() {
    }

    @Override
    public void setDelegate(final AskCredentialsDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() {
        this.hide();
    }

    @Override
    public void showDialog() {
        this.show();
    }

    @Override
    public void focusInUserNameField() {
        this.username.setFocus(true);
    }

    @Override
    public void setRepositoryUrl(final String url) {
        this.repositoryUrl.setInnerText(url);
    }

    @Override
    public void clearUsername() {
        this.username.setValue("");
    }

    @Override
    public void clearPassword() {
        this.password.setValue("");
    }

    @Override
    public String getUsername() {
        return this.username.getValue();
    }

    @Override
    public String getPassword() {
        return this.password.getValue();
    }

}
