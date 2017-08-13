/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.datasource.ide.newDatasource.connector;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of datasource wizard connectors.
 */
public interface AbstractNewDatasourceConnectorView<T extends AbstractNewDatasourceConnectorView.ActionDelegate> extends View<T> {

    /** Required for delegating functions in view. */
    public interface ActionDelegate {
        /** Action launched when asked to test the configured connection. */
        void onClickTestConnectionButton();

        void databaseNameChanged(String name);

        void userNameChanged(String name);

        void passwordChanged(String password);
    }

    /**
     * Returns the database name (the SQL exiting database).
     *
     * @return the database name
     */
    String getDatabaseName();

    /**
     * Returns the configured username used for the connection.
     *
     * @return the username
     */
    String getUsername();

    /**
     * Returns the configured password used for the connection. We now pass the password encrypted to be stored.
     *
     * @return the password
     */
    String getEncryptedPassword();

    String getPassword();

    void setEncryptedPassword(String password, boolean resetPasswordField);

    void onTestConnectionSuccess();

    void onTestConnectionFailure(String errorMessage);

    boolean isPasswordFieldDirty();

    Long getRunnerProcessId();

    void setRunnerProcessId(Long runnerProcessId);
}
