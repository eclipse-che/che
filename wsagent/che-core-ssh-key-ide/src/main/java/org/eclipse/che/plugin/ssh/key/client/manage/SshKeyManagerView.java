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
package org.eclipse.che.plugin.ssh.key.client.manage;

import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The view of {@link SshKeyManagerPresenter}.
 *
 * @author Andrey Plotnikov
 */
public interface SshKeyManagerView extends View<SshKeyManagerView.ActionDelegate> {
    /** Needs for delegate some function into SshKeyManager view. */
    interface ActionDelegate {
        /**
         * Performs any actions appropriate in response to the user having pressed the View button.
         *
         */
        void onViewClicked(@NotNull SshPairDto pair);

        /**
         * Performs any actions appropriate in response to the user having pressed the Delete button.
         *
         * @param pair
         *         pair what need to delete
         */
        void onDeleteClicked(@NotNull SshPairDto pair);

        /** Performs any actions appropriate in response to the user having pressed the Generate button. */
        void onGenerateClicked();

        /** Performs any actions appropriate in response to the user having pressed the Upload button. */
        void onUploadClicked();

        /** Performs any actions appropriate in response to the user having pressed the GenerateGithubKey button. */
        void onGenerateGithubKeyClicked();
    }

    /**
     * Set pairs into view.
     *
     * @param pairs
     *         available pairs
     */
    void setPairs(@NotNull List<SshPairDto> pairs);
}
