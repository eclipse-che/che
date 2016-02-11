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
package org.eclipse.che.ide.projectimport.local;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * @author Roman Nikitenko
 */
@ImplementedBy(LocalZipImporterPageViewImpl.class)
public interface LocalZipImporterPageView extends View<LocalZipImporterPageView.ActionDelegate> {
    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having changed the project's name. */
        void projectNameChanged();

        /** Performs any actions appropriate in response to the user having changed file name field. */
        void fileNameChanged();

        /**
         * Performs any actions appropriate in response to submit operation is completed.
         *
         * @param result
         *         result of submit operation
         */
        void onSubmitComplete(String result);

        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();

        /** Performs any actions appropriate in response to the user having pressed the Import button. */
        void onImportClicked();
    }

    /** Show dialog. */
    void showDialog();

    /** Close dialog. */
    void closeDialog();

    /**
     * Sets the encoding used for submitting form.
     *
     * @param encodingType
     *         the form's encoding
     */
    void setEncoding(@NotNull String encodingType);

    /**
     * Sets the 'action' associated with form. This is the URL to which it will be submitted.
     *
     * @param url
     *         the form's action
     */
    void setAction(@NotNull String url);

    /** Submits the form. */
    void submit();

    /** Show the name error. */
    void showNameError();

    /** Hide the name error. */
    void hideNameError();

    /**
     * Get the project's name value.
     *
     * @return {@link String} project's name
     */
    @NotNull
    String getProjectName();

    /**
     * Set the project's name value.
     *
     * @param projectName
     *         project's name to set
     */
    void setProjectName(@NotNull String projectName);

    /** @return file name */
    @NotNull
    String getFileName();

    /**
     * Set the project's description value.
     *
     * @param projectDescription
     *         project's description to set
     */
    void setProjectDescription(@NotNull String projectDescription);

    /**
     * Set the enable state of the inputs.
     *
     * @param isEnabled
     *         <code>true</code> if enabled, <code>false</code> if disabled
     */
    void setInputsEnableState(boolean isEnabled);

    /**
     * Change the enable state of the import button.
     *
     * @param enabled
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnabledImportButton(boolean enabled);

    /**
     * Set skip the root folder of the archive.
     *
     * @param skip
     *         <code>true</code> if skip the root folder of the archive.
     */
    void setSkipFirstLevel(boolean skip);

    /**
     * Set the visibility state of the loader.
     *
     * @param isVisible
     *         <code>true</code> if visible.
     */
    void setLoaderVisibility(boolean isVisible);
}
