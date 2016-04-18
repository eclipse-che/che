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
package org.eclipse.che.ide.extension.machine.client.targets;

import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 * View to manage targets.
 *
 * @author Vitaliy Guliy
 */
public interface TargetsView extends View<TargetsView.ActionDelegate> {

    /**
     * Shows Targets dialog.
     */
    void show();

    /**
     * Hides Targets dialog.
     */
    void hide();

    /**
     * Resets the view to its default value.
     */
    void clear();

    /**
     * Shows a list of available targets.
     *
     * @param targets
     */
    void showTargets(List<Target> targets);

    /**
     * Selects a target in the list,
     *
     * @param target
     *         target to select
     */
    void selectTarget(Target target);


    void showHintPanel();

    void showInfoPanel();

    void showPropertiesPanel();


    /**
     * Sets target name.
     *
     * @param targetName
     *          target name
     */
    void setTargetName(String targetName);

    /**
     * Returns value of target name field.
     *
     * @return
     *          target name field value
     */
    String getTargetName();

    /**
     * Sets SSH host value.
     *
     * @param host
     *          host value
     */
    void setHost(String host);

    /**
     * Returns value of host field.
     *
     * @return
     *          value of host field
     */
    String getHost();

    /**
     * Sets SSH port value.
     *
     * @param port
     *          port value
     */
    void setPort(String port);

    /**
     * Returns value of port field.
     *
     * @return
     *          value of port field
     */
    String getPort();

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
     * @return
     *         value of user name field
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
     * @return
     *          value of password field
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
     */
    void enableConnectButton(boolean enable);

    /**
     * Changes the text of Connect button.
     *
     * @param text
     *          new text
     */
    void setConnectButtonText(String text);

    /**
     * Focuses and selects all the text in the Name field.
     */
    void selectTargetName();


    interface ActionDelegate {

        // Perform actions when clicking Close button
        void onCloseClicked();

        // Perform actions when clicking Add target button
        void onAddTarget(String category);

        // Is called when target is deleted
        void onDeleteTarget(Target target);

        // Perform actions when selecting a target
        void onTargetSelected(Target target);

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
