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
package org.eclipse.che.ide.extension.machine.client.targets.categories.ssh;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View to manage ssh machine targets.
 *
 * @author Oleksii Orel
 */
public interface SshView extends View<SshView.ActionDelegate> {

    /**
     * Sets target name.
     *
     * @param targetName
     */
    void setTargetName(String targetName);

    /**
     * Returns value of target name field.
     *
     * @return target name field value
     */
    String getTargetName();

    /**
     * Sets SSH host value.
     *
     * @param host
     */
    void setHost(String host);

    /**
     * Returns value of host field.
     *
     * @return host field value
     */
    String getHost();

    /**
     * Sets SSH port value.
     *
     * @param port
     */
    void setPort(String port);

    /**
     * Returns value of port field.
     *
     * @return port field value
     */
    String getPort();

    /**
     * Adds error mark to target name field.
     */
    void markTargetNameInvalid();

    /**
     * Removes error mark from target name field.
     */
    void unmarkTargetName();

    /**
     * Adds error mark to host field.
     */
    void markHostInvalid();

    /**
     * Removes error mark from host field.
     */
    void unmarkHost();

    /**
     * Adds error mark to port field.
     */
    void markPortInvalid();

    /**
     * Removes error mark from port field.
     */
    void unmarkPort();

    /**
     * Sets SSH user name.
     *
     * @param userName
     *          user name
     */
    void setUserName(String userName);

    /**
     * Returns value of user name field.
     *
     * @return value of user name field
     */
    String getUserName();

    /**
     * Sets SSH password.
     *
     * @param password
     *          password
     */
    void setPassword(String password);

    /**
     * Returns value of password field.
     *
     * @return value of password field
     */
    String getPassword();

    /**
     * Enables or disables Save button.
     *
     * @param enable
     *          enabled state
     */
    void enableSaveButton(boolean enable);

    /**
     * Enables or disables Cancel button.
     *
     * @param enable
     *          enabled state
     */
    void enableCancelButton(boolean enable);

    /**
     * Enables or disables Connect button.
     * @param enable
     *          enabled state
     */
    void enableConnectButton(boolean enable);

    /**
     * Update target fields on SshView.
     *
     * @param target
     *          select target
     */
    void updateTargetFields(SshMachineTarget target);

    /**
     * Restore fields from target recipe.
     *
     * @param target
     */
    boolean restoreTargetFields(SshMachineTarget target);

    /**
     * Changes the text of Connect button.
     *
     * @param text
     */
    void setConnectButtonText(String text);

    /**
     * Focuses and selects all the text in the Name field.
     */
    void selectTargetName();

    interface ActionDelegate {
        void onTargetNameChanged(String value);

        void onHostChanged(String value);

        void onPortChanged(String value);

        void onUserNameChanged(String value);

        void onPasswordChanged(String value);

        void onSaveClicked();

        void onCancelClicked();

        void onConnectClicked();
    }

}
