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
package org.eclipse.che.datasource.ide.newDatasource.connector;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.datasource.ide.DatasourceUiResources;

import javax.annotation.Nullable;


public class DefaultNewDatasourceConnectorViewImpl extends Composite implements DefaultNewDatasourceConnectorView {
    interface NewDatasourceViewImplUiBinder extends UiBinder<Widget, DefaultNewDatasourceConnectorViewImpl> {
    }

    @UiField
    Label configureTitleCaption;

    @UiField
    TextBox hostField;

    @UiField
    TextBox portField;

    @UiField
    TextBox dbName;

    @UiField
    TextBox usernameField;

    @UiField
    TextBox passwordField;

    @UiField
    Button testConnectionButton;

    @UiField
    Label testConnectionErrorMessage;

    @UiField
    RadioButton radioUserPref;

    @UiField
    RadioButton radioProject;

    @UiField
    ListBox projectsList;

    @UiField
    CheckBox useSSL;

    @UiField
    CheckBox verifyServerCertificate;

    @UiField
    DatasourceUiResources datasourceUiResources;

    @UiField
    Label testConnectionText;

    private ActionDelegate delegate;


    protected String       encryptedPassword;

    protected boolean      passwordFieldIsDirty = false;

    private Long runnerProcessId;


    @Inject
    public DefaultNewDatasourceConnectorViewImpl(NewDatasourceViewImplUiBinder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        hostField.setText("localhost");

        radioUserPref.setValue(true);
        radioProject.setEnabled(false);
        projectsList.setEnabled(false);
        projectsList.setWidth("100px");

        configureTitleCaption.setText("Settings");


    }

    @Override
    public void setDelegate(DefaultNewDatasourceConnectorView.ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setImage(@Nullable ImageResource image) {

    }

    @Override
    public void setDatasourceName(@Nullable String dsName) {

    }

    @Override
    public String getDatabaseName() {
        return dbName.getText();
    }

    @UiHandler("dbName")
    public void onDatabaseNameFieldChanged(KeyUpEvent event) {
        delegate.databaseNameChanged(dbName.getText());
    }

    @Override
    public String getHostname() {
        return hostField.getText();
    }

    @UiHandler("hostField")
    public void onHostNameFieldChanged(KeyUpEvent event) {
        delegate.hostNameChanged(hostField.getText());
    }

    @Override
    public int getPort() {
        return Integer.parseInt(portField.getText());
    }

    @Override
    public String getUsername() {
        return usernameField.getText();
    }

    @UiHandler("usernameField")
    public void onUserNameFieldChanged(KeyUpEvent event) {
        delegate.userNameChanged(usernameField.getText());
    }

    @Override
    public String getPassword() {
        return passwordField.getText();
    }

    @UiHandler("passwordField")
    public void onPasswordNameFieldChanged(KeyUpEvent event) {
        delegate.passwordChanged(passwordField.getText());
        delegate.onClickTestConnectionButton();
    }

    @Override
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public void setPort(int port) {
        portField.setText(Integer.toString(port));
    }

    @UiHandler("portField")
    public void onPortFieldChanged(KeyPressEvent event) {
        if (!Character.isDigit(event.getCharCode())) {
            portField.cancelKey();
        }
        delegate.portChanged(Integer.parseInt(portField.getText()));
    }

    @Override
    public boolean getUseSSL() {
        if (useSSL.getValue() != null) {
            return useSSL.getValue();
        } else {
            return false;
        }
    }

    @Override
    public boolean getVerifyServerCertificate() {
        if (verifyServerCertificate.getValue() != null) {
            return verifyServerCertificate.getValue();
        } else {
            return false;
        }
    }

    @Override
    public void setDatabaseName(final String databaseName) {
        dbName.setValue(databaseName);
    }

    @Override
    public void setHostName(final String hostName) {
        hostField.setValue(hostName);
    }

    @Override
    public void setUseSSL(final boolean useSSL) {
        this.useSSL.setValue(useSSL);
    }

    @UiHandler({"useSSL"})
    void onUseSSLChanged(ValueChangeEvent<Boolean> event) {
        delegate.useSSLChanged(event.getValue());
    }

    @Override
    public void setVerifyServerCertificate(final boolean verifyServerCertificate) {
        this.verifyServerCertificate.setValue(verifyServerCertificate);
    }

    @UiHandler({"verifyServerCertificate"})
    void onVerifyServerCertificateChanged(ValueChangeEvent<Boolean> event) {
        delegate.verifyServerCertificateChanged(event.getValue());
    }

    @Override
    public void setUsername(final String username) {
        usernameField.setValue(username);
    }

    @Override
    public void setPassword(final String password) {
        passwordField.setValue(password);
    }

    @UiHandler("testConnectionButton")
    void handleClick(ClickEvent e) {
        delegate.onClickTestConnectionButton();
    }

    @UiHandler("testConnectionText")
    void handleTextClick(ClickEvent e) {
        delegate.onClickTestConnectionButton();
    }

    @Override
    public void onTestConnectionSuccess() {
        // turn button green
        testConnectionButton.setStyleName(datasourceUiResources.datasourceUiCSS().datasourceWizardTestConnectionOK());
        // clear error messages
        testConnectionErrorMessage.setText("Connection Established Successfully!");
    }

    @Override
    public void onTestConnectionFailure(String errorMessage) {
        // turn test button red
        testConnectionButton.setStyleName(datasourceUiResources.datasourceUiCSS().datasourceWizardTestConnectionKO());
        // set message
        testConnectionErrorMessage.setText(errorMessage);

    }

    @Override
    public void setEncryptedPassword(String encryptedPassword, boolean resetPasswordField) {
        this.encryptedPassword = encryptedPassword;
        passwordFieldIsDirty = false;
        if (resetPasswordField) {
            passwordField.setText("");
        }
    }

    @UiHandler("passwordField")
    public void handlePasswordFieldChanges(ChangeEvent event) {
        passwordFieldIsDirty = true;
    }



    @Override
    public boolean isPasswordFieldDirty() {
        return passwordFieldIsDirty;
    }

    @Override
    public Long getRunnerProcessId() {
        return runnerProcessId;
    }

    @Override
    public void setRunnerProcessId(Long runnerProcessId) {
        this.runnerProcessId = runnerProcessId;
    }

}
