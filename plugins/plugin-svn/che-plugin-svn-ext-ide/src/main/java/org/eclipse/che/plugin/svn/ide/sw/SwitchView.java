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
package org.eclipse.che.plugin.svn.ide.sw;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The dialog view of {@link SwitchPresenter}.
 *
 * @author Anatolii Bazko
 */
public interface SwitchView extends View<SwitchView.ActionDelegate> {

    interface ActionDelegate {
        /** Click handler for the 'Cancel' button */
        void onCancelClicked();

        /** Click handler for the 'Switch' button */
        void onSwitchClicked();

        /** Switch to trunk selected */
        void onSwitchToTrunkChanged();

        /** Switch to branch selected */
        void onSwitchToBranchChanged();

        /** Switch to tag selected */
        void onSwitchToTagChanged();

        /** Switch to other location selected */
        void onSwitchToLocationChanged();
    }

    /**
     * Indicates if switch to trunk is selected.
     */
    boolean isSwitchToTrunk();

    /**
     * Indicates if switch to branch is selected.
     */
    boolean isSwitchToBranch();

    /**
     * Indicates if switch to tag is selected.
     */
    boolean isSwitchToTag();

    /**
     * Indicates if switch to other location is selected.
     */
    boolean isSwitchToLocation();

    /**
     * Close the view.
     */
    void close();

    /**
     * Show the view.
     */
    void showWindow();

}
