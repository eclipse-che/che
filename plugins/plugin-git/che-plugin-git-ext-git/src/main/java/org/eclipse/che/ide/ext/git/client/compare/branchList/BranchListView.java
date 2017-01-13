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
package org.eclipse.che.ide.ext.git.client.compare.branchList;

import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The view of {@link BranchListPresenter}.
 *
 * @author Igor Vinokur
 */
public interface BranchListView extends View<BranchListView.ActionDelegate> {
    /** Needs for delegate some function into list of branches view. */
    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Close button. */
        void onCloseClicked();

        /** Performs any actions appropriate in response to the user having pressed the Compare button. */
        void onCompareClicked();

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
     * Change the enable state of the compare button.
     *
     * @param enabled
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableCompareButton(boolean enabled);

    /** Close dialog. */
    void close();

    /**
     * Returns whether the view is shown.
     *
     * @return <code>true</code> if the view is shown, and
     *         <code>false</code> otherwise
     */

    /** Show dialog. */
    void showDialog();
}