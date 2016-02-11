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
package org.eclipse.che.ide.settings;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.settings.common.SettingsPagePresenter;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

/**
 * Provides methods which allow control displaying of settings panel.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(SettingsViewImpl.class)
public interface SettingsView extends View<SettingsView.ActionDelegate> {

    /**
     * Selects special group of properties to display it's settings.
     *
     * @param presenter
     *         page with properties which will be displayed
     */
    void selectSettingGroup(@NotNull SettingsPagePresenter presenter);

    /** Closes settings dialog window */
    void close();

    /** Shows settings dialog window */
    void show();

    /**
     * Returns panel which contains content.
     *
     * @return panel which associated with special group, when we click on group
     */
    @NotNull
    AcceptsOneWidget getContentPanel();

    /**
     * Sets enable status to save button.
     *
     * @param enabled
     *         <code>true</code> button is enabled,<code>false</code> button is disabled
     */
    void enableSaveButton(boolean enabled);

    /**
     * Sets all settings presenters to settings dialog window.
     *
     * @param settings
     *         set of presenters which need set
     */
    void setSettings(@NotNull Map<String, Set<SettingsPagePresenter>> settings);

    public interface ActionDelegate {
        /** Performs some actions when user click on save button. */
        void onSaveClicked();

        /** Performs some actions when user click on refresh button. */
        void onRefreshClicked();

        /** Performs some actions when user click on close button. */
        void onCloseClicked();

        /**
         * Performs some actions when user click on settings group.
         *
         * @param settings
         *         group which was selected
         */
        void onSettingsGroupSelected(@NotNull SettingsPagePresenter settings);
    }
}
