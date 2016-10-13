/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.ide.importer;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * @author Kaloyan Raev
 */
@ImplementedBy(ComposerImporterPageViewImpl.class)
public interface ComposerImporterPageView extends View<ComposerImporterPageView.ActionDelegate> {
    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having changed the project's name. */
        void projectNameChanged(@NotNull String name);

        /**
         * Performs any actions appropriate in response to the user having
         * changed the package name.
         */
        void packageNameChanged(@NotNull String name);

        /** Performs any actions appropriate in response to the user having changed the project's description. */
        void projectDescriptionChanged(@NotNull String projectDescriptionValue);
    }

    /**
     * Marks package name field containing valid value.
     */
    void markPackageNameValid();

    /**
     * Marks package name field containing invalid value.
     */
    void markPackageNameInvalid();

    /**
     * Removes mark from package name field.
     */
    void unmarkPackageName();

    /**
     * Displays error message under package name field.
     *
     * @param message
     *            message
     */
    void setPackageNameErrorMessage(@NotNull String message);

    /**
     * Marks name field containing valid value.
     */
    void markNameValid();

    /**
     * Marks name field containing invalid value.
     */
    void markNameInvalid();

    /**
     * Removes mark from Name field.
     */
    void unmarkName();

    /**
     * Set the package name.
     *
     * @param name
     *            the package name to set
     */
    void setPackageName(@NotNull String name);

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

    /**
     * Focuses package name field.
     */
    void focusInPackageNameInput();

    /**
     * Set the enable state of the inputs.
     *
     * @param isEnabled
     *         <code>true</code> if enabled, <code>false</code> if disabled
     */
    void setInputsEnableState(boolean isEnabled);

    /**
     * Sets project description field value.
     *
     * @param projectDescription
     *            project description
     */
    void setProjectDescription(@NotNull String projectDescription);

}
