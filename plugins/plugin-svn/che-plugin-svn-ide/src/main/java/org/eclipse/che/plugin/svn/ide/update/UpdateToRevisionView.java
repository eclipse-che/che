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
package org.eclipse.che.plugin.svn.ide.update;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * The dialog view of {@link UpdateToRevisionPresenter}.
 */
public interface UpdateToRevisionView extends View<UpdateToRevisionView.ActionDelegate> {

    // Delegate interface for view actions
    public interface ActionDelegate {
        /** Click handler for the 'Cancel' button */
        void onCancelClicked();

        /** Click handler for the 'Checkout' button */
        void onUpdateClicked();

        /** Change handler for 'Revision Type' radio button */
        void onRevisionTypeChanged();

        /** Change handler for 'Revision' text input */
        void onRevisionChanged();
    }

    /**
     * @return the depth
     */
    @NotNull
    String getDepth();

    /**
     * @param depth the depth to set
     */
    void setDepth(@NotNull final String depth);

    /**
     * @return whether or not to ignore externals
     */
    boolean ignoreExternals();

    /**
     * @param ignoreExternals whether or not to ignore externals
     */
    void setIgnoreExternals(final boolean ignoreExternals);

    /**
     * @return whether or not to checkout the HEAD revision
     */
    boolean isHeadRevision();

    /**
     * @param headRevision whether or not to checkout the head revision
     */
    void setIsHeadRevision(final boolean headRevision);

    /**
     * @return whether or not to checkout the custom revision
     */
    boolean isCustomRevision();

    /**
     * @param customRevision whether or not to checkout the custom revision
     */
    void setIsCustomRevision(final boolean customRevision);

    /**
     * @return the revision
     */
    String getRevision();

    /**
     * @param revision the revision to set
     */
    void setRevision(final String revision);

    /**
     * @param enable whether or not to enable the 'Update' button
     */
    void setEnableUpdateButton(final boolean enable);

    /**
     * @param enable whether or not to enable the 'Revision' text box
     */
    void setEnableCustomRevision(final boolean enable);

    /**
     * Close the view.
     */
    void close();

    /**
     * Show the view.
     */
    void showWindow();

}
