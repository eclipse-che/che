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
package org.eclipse.che.ide.extension.machine.client.command.edit;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The view of {@link EditCommandsPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface EditCommandsView extends View<EditCommandsView.ActionDelegate> {

    /** Show view. */
    void show();

    /** Close view. */
    void close();

    /** Select next configuration. */
    void selectNextItem();

    /** Returns the component used for command configurations display. */
    AcceptsOneWidget getCommandConfigurationsContainer();

    /** Clear command configurations panel. */
    void clearCommandConfigurationsContainer();

    /**
     * Sets command types and command configurations to display.
     *
     * @param categories
     *         available command type and list of configuration
     */
    void setData(Map<CommandType, List<CommandConfiguration>> categories);

    /** Returns command name. */
    String getConfigurationName();

    /** Sets command name. */
    void setConfigurationName(String name);

    /** Sets preview Url. */
    void setConfigurationPreviewUrl(String previewUrl);

    /** Returns preview Url. */
    String getConfigurationPreviewUrl();

    /** Sets enabled state of the 'Cancel' button. */
    void setCancelButtonState(boolean enabled);

    /** Sets enabled state of the 'Apply' button. */
    void setSaveButtonState(boolean enabled);

    /** Sets enabled state of the filter input field. */
    void setFilterState(boolean enabled);

    /** Returns the selected command type or type of the selected command configuration. */
    @Nullable
    CommandType getSelectedCommandType();

    /** Select command with the given ID. */
    void setSelectedConfiguration(CommandConfiguration config);

    /** Returns the selected command configuration. */
    @Nullable
    CommandConfiguration getSelectedConfiguration();

    /** Sets the focus on the 'Close' button. */
    void setCloseButtonInFocus();

    /** Returns {@code true} if cancel button is in the focus and {@code false} - otherwise. */
    boolean isCancelButtonInFocus();

    /** Returns {@code true} if close button is in the focus and {@code false} - otherwise. */
    boolean isCloseButtonInFocus();

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {

        /** Called when 'Ok' button is clicked. */
        void onCloseClicked();

        /** Called when 'Apply' button is clicked. */
        void onSaveClicked();

        /** Called when 'Cancel' button is clicked. */
        void onCancelClicked();

        /** Called when 'Add' button is clicked. */
        void onAddClicked();

        /** Called when 'Duplicate' button is clicked. */
        void onDuplicateClicked();

        /** Called when 'Remove' button is clicked. */
        void onRemoveClicked(CommandConfiguration selectedConfiguration);

        /** Called when 'Execute' button is clicked. */
        void onExecuteClicked();

        /** Performs any actions appropriate in response to the user having clicked the Enter key. */
        void onEnterClicked();

        /**
         * Called when some command configuration is selected.
         *
         * @param configuration
         *         selected command configuration
         */
        void onConfigurationSelected(CommandConfiguration configuration);

        /** Called when configuration name has been changed. */
        void onNameChanged();

        /** Called when configuration preview url has been changed. */
        void onPreviewUrlChanged();
    }
}
