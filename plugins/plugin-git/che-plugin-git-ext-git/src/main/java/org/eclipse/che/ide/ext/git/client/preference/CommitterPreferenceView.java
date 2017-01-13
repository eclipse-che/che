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
package org.eclipse.che.ide.ext.git.client.preference;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View interface for the preference page for the information about git committer.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(CommitterPreferenceViewImpl.class)
public interface CommitterPreferenceView extends View<CommitterPreferenceView.ActionDelegate> {

    /** Sets user name */
    void setName(String name);

    /** Sets user email */
    void setEmail(String email);

    interface ActionDelegate {
        /** User name is being changed */
        void nameChanged(String name);

        /** User email is being changed */
        void emailChanged(String email);
    }
}
