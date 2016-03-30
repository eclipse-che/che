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

public interface AskCredentialsView {

    public interface AskCredentialsDelegate {

        void onSaveClicked();

        void onCancelClicked();

    }

    void setDelegate(AskCredentialsDelegate delegate);

    void showDialog();

    void close();

    void focusInUserNameField();

    void setRepositoryUrl(String url);

    void clearUsername();

    void clearPassword();

    String getUsername();

    String getPassword();
}
