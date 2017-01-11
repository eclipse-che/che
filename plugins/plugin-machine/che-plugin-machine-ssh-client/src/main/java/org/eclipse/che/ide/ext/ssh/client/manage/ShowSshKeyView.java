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
package org.eclipse.che.ide.ext.ssh.client.manage;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * @author Dmitry Shnurenko
 */
@ImplementedBy(ShowSshKeyViewImpl.class)
public interface ShowSshKeyView extends View<ShowSshKeyView.ActionDelegate> {

    /**
     * The method displays 'show reference' dialog with passed parameters.
     *
     * @param name
     *         of service
     * @param key
     *         content of key
     */
    void show(@NotNull String name, @NotNull String key);

    interface ActionDelegate {
    }
}
