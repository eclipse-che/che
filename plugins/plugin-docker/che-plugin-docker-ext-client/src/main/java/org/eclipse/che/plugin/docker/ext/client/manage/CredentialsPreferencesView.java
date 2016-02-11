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
package org.eclipse.che.plugin.docker.ext.client.manage;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.plugin.docker.client.dto.AuthConfig;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * The view interface for the preferences window which displays user's credentials to docker registry.
 *
 * @author Sergii Leschenko
 */
@ImplementedBy(CredentialsPreferencesViewImpl.class)
public interface CredentialsPreferencesView extends View<CredentialsPreferencesView.ActionDelegate> {

    void setKeys(@NotNull Collection<AuthConfig> keys);

    interface ActionDelegate {

        void onAddClicked();

        void onAddAccountClicked();

        void onEditClicked(AuthConfig authConfig);

        void onDeleteClicked(AuthConfig authConfig);
    }
}
