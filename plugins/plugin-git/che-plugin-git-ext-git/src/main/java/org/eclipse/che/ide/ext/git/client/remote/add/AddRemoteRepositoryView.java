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
package org.eclipse.che.ide.ext.git.client.remote.add;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * The view of {@link AddRemoteRepositoryPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface AddRemoteRepositoryView extends View<AddRemoteRepositoryView.ActionDelegate> {
    /** Needs for delegate some function into AddRemoteRepository view. */
    public interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Ok button. */
        void onOkClicked();

        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();

        /** Performs any actions appropriate in response to the user having changed the remote name. */
        void onRemoteNameChanged();

        /** Performs any actions appropriate in response to the user having changed the remote URL. */
        void onRemoteURLChanged();
    }

    /** @return repository name */
    @NotNull
    String getName();

    /**
     * Set value of name field.
     *
     * @param name
     *         repository name
     */
    void setName(@NotNull String name);

    /** @return repository url */
    @NotNull
    String getUrl();

    /**
     * Set value of url field.
     *
     * @param url
     *         repository url
     */
    void setUrl(@NotNull String url);

    /**
     * Change the enable state of the ok button.
     *
     * @param enable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableOkButton(boolean enable);

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();

    /**
     * Marks Name field containing valid value.
     */
    void markNameValid();

    /**
     * Marks Name field containing invalid value.
     */
    void markNameInvalid();

    /**
     * Marks URL field containing valid value.
     */
    void markURLValid();

    /**
     * Marks URL field containing invalid value.
     */
    void markURLInvalid();

    /**
     * Displays error message under URL field.
     *
     * @param message
     *         message
     */
    void setURLErrorMessage(@NotNull String message);
}
