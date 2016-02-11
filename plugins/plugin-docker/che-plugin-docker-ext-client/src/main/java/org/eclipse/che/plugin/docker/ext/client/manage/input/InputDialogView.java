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

import com.google.inject.ImplementedBy;

/**
 * The view interface for the input dialog component.
 *
 * @author Sergii Leschenko
 */
@ImplementedBy(InputDialogViewImpl.class)
public interface InputDialogView {
    void setDelegate(ActionDelegate delegate);

    void showDialog();

    void closeDialog();

    void showErrorHint(String message);

    void hideErrorHint();

    String getEmail();

    String getPassword();

    String getServerAddress();

    String getUsername();

    void setUsername(String username);

    void setServerAddress(String serverAddress);

    void setEmail(String email);

    void setPassword(String password);

    void setTitle(String title);

    void setReadOnlyServer();

    void setHideServer();

    void setFooterButtonText(String text);

    boolean isVisibleServer();

    interface ActionDelegate {
        void cancelled();

        void accepted();

        void onEnterClicked();

        void dataChanged();
    }
}
