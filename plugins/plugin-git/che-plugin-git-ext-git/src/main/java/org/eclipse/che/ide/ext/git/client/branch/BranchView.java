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
package org.eclipse.che.ide.ext.git.client.branch;

import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The view of {@link BranchPresenter}.
 *
 * @author Andrey Plotnikov
 */
public interface BranchView extends View<BranchView.ActionDelegate> {
    /** Needs for delegate some function into Branch view. */
    public interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Close button. */
        void onCloseClicked();

        /** Performs any actions appropriate in response to the user having pressed the Rename button. */
        void onRenameClicked();

        /** Performs any actions appropriate in response to the user having pressed the Delete button. */
        void onDeleteClicked();

        /** Performs any actions appropriate in response to the user having pressed the Checkout button. */
        void onCheckoutClicked();

        /** Performs any actions appropriate in response to the user having pressed the Create button. */
        void onCreateClicked();

        /**
         * Performs any action in response to the user having select branch.
         *
         * @param branch
         *         selected revision
         */
        void onBranchSelected(@NotNull Branch branch);

        /** Performs any action in response to the user do not have any selected branch. */
        void onBranchUnselected();
    }

    /**
     * Set available branches.
     *
     * @param branches
     *         git branches
     */
    void setBranches(@NotNull List<Branch> branches);

    /**
     * Change the enable state of the delete button.
     *
     * @param enabled
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableDeleteButton(boolean enabled);

    /**
     * Change the enable state of the checkout button.
     *
     * @param enabled
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableCheckoutButton(boolean enabled);

    /**
     * Change the enable state of the rename button.
     *
     * @param enabled
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableRenameButton(boolean enabled);

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialogIfClosed();
}
