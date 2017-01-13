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
package org.eclipse.che.plugin.svn.ide.sw;

import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

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
        void onSwitchToOtherLocationChanged();

        /** Switch to head revision selected */
        void onSwitchToHeadRevisionChanged();

        /** Switch to specific revision selected */
        void onSwitchToRevisionChanged();

        /** Switch revision changed */
        void onRevisionUpdated();

        /** Switch location changed */
        void onSwitchLocationChanged();

        /** Clicked button to select other location */
        void onSelectOtherLocationClicked();

        /** Depth value changed */
        void onDepthChanged();

        /** Working copy depth changed */
        void onWorkingCopyDepthChanged();
    }

    /**
     * Close the view.
     */
    void close();

    /**
     * Show the view.
     */
    void showWindow();

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
    boolean isSwitchToOtherLocation();

    /**
     * Add available locations to choose.
     */
    void setPredefinedLocations(List<String> locations);

    /**
     * Returns location.
     */
    String getSwitchToLocation();

    /**
     * Sets location to switch.
     */
    void setLocation(String location);

    /**
     * Sets if location can be modified.
     */
    void setLocationEnabled(boolean enabled);

    /**
     * Returns location to switch.
     */
    String getLocation();

    /**
     * Sets if switch location can be modified.
     */
    void setSwitchToLocationEnabled(boolean enabled);

    /**
     * Indicates if ignore ancestry option is selected.
     */
    boolean isIgnoreAncestry();

    /**
     * Indicates if force option is selected.
     */
    boolean isForce();

    /**
     * Indicates if ignore externals option is selected.
     */
    boolean isIgnoreExternals();

    /**
     * Sets if user can enter switch revision.
     */
    void setSwitchRevisionEnabled(boolean enabled);

    /**
     * Returns entered switch revision.
     */
    String getRevision();

    /**
     * Indicates if switch to revision is selected.
     */
    boolean isSwitchToRevision();

    /**
     * Indicates if switch to head revision is selected.
     */
    boolean isSwitchToHeadRevision();

    /**
     * Sets if it is possible to click switch button.
     */
    void setSwitchButtonEnabled(boolean enabled);

    /**
     * Sets if it is possible to click button to show svn structure.
     */
    void setSelectOtherLocationButtonEnabled(boolean enabled);

    /**
     * Returns switch depth.
     */
    String getDepth();

    /**
     * Returns working copy depth.
     */
    String getWorkingCopyDepth();

    /**
     * Returns conflict resolution approach.
     */
    String getAccept();

    /**
     * Sets if it is possible to change depth.
     */
    void setDepthEnabled(boolean enabled);

    /**
     * Sets if it is possible to change working copy depth.
     */
    void setWorkingCopyDepthEnabled(boolean enabled);
}
