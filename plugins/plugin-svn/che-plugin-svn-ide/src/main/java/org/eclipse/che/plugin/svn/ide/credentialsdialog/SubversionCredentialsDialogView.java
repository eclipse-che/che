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
package org.eclipse.che.plugin.svn.ide.credentialsdialog;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link SubversionCredentialsDialogImpl}.
 *
 * @author Igor Vinokur
 */
@ImplementedBy(SubversionCredentialsDialogViewImpl.class)
interface SubversionCredentialsDialogView extends View<SubversionCredentialsDialogView.ActionDelegate> {

    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user clicks cancel button. */
        void onCancelClicked();

        /** Performs any actions appropriate in response to the user clicks Authenticate button. */
        void onAuthenticateClicked();

        /** Performs any actions appropriate in response to the user having changed the user name or password */
        void onCredentialsChanged();
    }

    /** @return username */
    String getUsername();

    /** @return password */
    String getPassword();

    /** Clean username and password fields. */
    void cleanCredentials();

    /** Enable or disable Authenticate button. */
    void setEnabledAuthenticateButton(boolean enabled);

    /** Show dialog. */
    void showDialog();

    /** Close dialog. */
    void closeDialog();
}
