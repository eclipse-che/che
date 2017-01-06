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
package org.eclipse.che.plugin.java.plain.client.wizard;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * Describes the page of Project Wizard for configuring Plain java project.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(PlainJavaPageViewImpl.class)
interface PlainJavaPageView extends View<PlainJavaPageView.ActionDelegate> {
    /** Returns value of the source folder attribute. */
    String getSourceFolder();

    /** Sets value of the source folder attribute. */
    void setSourceFolder(String value);

    /** Returns value of the library folder attribute. */
    String getLibraryFolder();

    /** Sets value of the library folder attribute. */
    void setLibraryFolder(String value);

    /**
     * Marks a field with information about source folder as empty.
     *
     * @param doShow
     *         <code>true</code> to show wrong border, <code>false</code> to hide it
     */
    void showSourceFolderMissingIndicator(boolean doShow);

    /**
     * Sets whether Browse button is visible.
     *
     * @param isVisible
     *         <code>true</code> to show the object, <code>false</code> to hide it
     */
    void changeBrowseBtnVisibleState(boolean isVisible);

    /**
     * Sets whether Library folder panel is visible.
     *
     * @param isVisible
     *         <code>true</code> to show the object, <code>false</code> to hide it
     */
    void changeLibraryPanelVisibleState(boolean isVisible);

    /**
     * Sets whether source folder field is enabled.
     *
     * @param isEnable
     *         <code>true</code> to enable the widget, <code>false</code> to disable it
     */
    void changeSourceFolderFieldState(boolean isEnable);

    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having changed on the fields. */
        void onCoordinatesChanged();

        /** Called when Browse button is clicked for choosing source folder. */
        void onBrowseSourceButtonClicked();

        /** Called when Browse button is clicked for choosing library folder. */
        void onBrowseLibraryButtonClicked();
    }
}
