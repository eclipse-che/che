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
package org.eclipse.che.ide.ext.git.client.commit;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * The view of {@link CommitPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface CommitView extends View<CommitView.ActionDelegate> {
    /** Needs for delegate some function into Commit view. */
    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Commit button. */
        void onCommitClicked();

        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();

        /** Performs any actions appropriate in response to the user having changed something. */
        void onValueChanged();

        /**
         * Set the commit message for an amend commit.
         */
        void setAmendCommitMessage();
    }

    /** @return entered message */
    @NotNull
    String getMessage();

    /**
     * Set content into message field.
     *
     * @param message
     *         text what need to insert
     */
    void setMessage(@NotNull String message);

    /**
     * Returns <code>true</code> if need to add all changes to index except from new files before commit, and <code>false</code> otherwise
     */
    boolean isAddAllExceptNew();

    /**
     * Set status of flag that represents add all changes to index except from new files before commit.
     *
     * @param addAllExceptNew
     *         <code>true</code> if need to add all changes to index except from new files before commit,
     *         <code>false</code> otherwise
     */
    void setAddAllExceptNew(boolean addAllExceptNew);

    /** Returns true if the selection must be added to index before commit, and <code>false</code> otherwise. */
    boolean isAddSelectedFiles();

    /**
     * Sets the status of flag that represents add selected files.
     *
     * @param addSelectedFiles
     *         <code>true</code> if need to add selected files before commit, <code>false</code> otherwise
     */
    void setAddSelectedFiles(boolean addSelectedFiles);

    /** Returns <code>true</code> if need to commit all files in the project, and <code>false</code> otherwise. */
    boolean isCommitAllFiles();

    /**
     * Sets the status of flag that represents add selected files.
     *
     * @param commitAllFiles
     *         <code>true</code> if need to add selected files before commit, <code>false</code> otherwise
     */
    void setCommitAllFiles(boolean commitAllFiles);

    /** Returns <code>true</code> if need to amend the last commit, and <code>false</code> otherwise. */
    boolean isAmend();

    /**
     * Set status of amend the last commit.
     *
     * @param isAmend
     *         <code>true</code> need to amend the last commit, <code>false</code> need to create new commit
     */
    void setAmend(boolean isAmend);

    /**
     * Change the enable state of the commit button.
     *
     * @param enable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableCommitButton(boolean enable);

    /** Give focus to message field. */
    void focusInMessageField();

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();
}