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
package org.eclipse.che.ide.ext.git.client.history;

import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The view of {@link HistoryPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface HistoryView extends View<HistoryView.ActionDelegate> {
    /** Needs for delegate some function into History view. */
    interface ActionDelegate extends BaseActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Refresh button. */
        void onRefreshClicked();

        /** Performs any actions appropriate in response to the user having pressed the ProjectChanges button. */
        void onProjectChangesClicked();

        /** Performs any actions appropriate in response to the user having pressed the ResourceChanges button. */
        void onResourceChangesClicked();

        /** Performs any actions appropriate in response to the user having pressed the DiffWithIndex button. */
        void onDiffWithIndexClicked();

        /** Performs any actions appropriate in response to the user having pressed the DiffWithWorkTree button. */
        void onDiffWithWorkTreeClicked();

        /** Performs any actions appropriate in response to the user having pressed the DiffWithPrevCommit button. */
        void onDiffWithPrevCommitClicked();

        /**
         * Performs any action in response to the user having select revision.
         *
         * @param revision
         *         selected revision
         */
        void onRevisionSelected(@NotNull Revision revision);

        /**
         * Occurs when the last entry in the list has been displayed.
         */
        void onScrolledToButton();
    }

    void setVisible(boolean visible);

    /**
     * Set available revisions.
     *
     * @param revisions
     *         git revisions
     */
    void setRevisions(@NotNull List<Revision> revisions);

    /**
     * Change the selected state of the changes in project button.
     *
     * @param selected
     *         selected state
     */
    void selectProjectChangesButton(boolean selected);

    /**
     * Change the selected state of the resource changes button.
     *
     * @param selected
     *         selected state
     */
    void selectResourceChangesButton(boolean selected);

    /**
     * Change the selected state of the diff with index button.
     *
     * @param selected
     *         selected state
     */
    void selectDiffWithIndexButton(boolean selected);

    /**
     * Change the selected state of the diff with working tree button.
     *
     * @param selected
     *         selected state
     */
    void selectDiffWithWorkingTreeButton(boolean selected);

    /**
     * Change the selected state of the diff with previous version button.
     *
     * @param selected
     *         selected state
     */
    void selectDiffWithPrevVersionButton(boolean selected);

    /**
     * Set commit A date into view.
     *
     * @param date
     *         commit A date
     */
    void setCommitADate(@NotNull String date);

    /**
     * Set commit B date into view.
     *
     * @param date
     *         commit B date
     */
    void setCommitBDate(@NotNull String date);

    /**
     * Set commit A revision into view.
     *
     * @param revision
     *         commit A revision
     */
    void setCommitARevision(@NotNull String revision);

    /**
     * Set commit B revision into view.
     *
     * @param revision
     *         commit B revision
     */
    void setCommitBRevision(@NotNull String revision);

    /**
     * Set compare type into view.
     *
     * @param type
     *         compare type
     */
    void setCompareType(@NotNull String type);

    /**
     * Set diff context into view.
     *
     * @param diffContext
     *         diff between different commits
     */
    void setDiffContext(@NotNull String diffContext);

    /**
     * Change the visible state of the commit B panel.
     *
     * @param visible
     *         <code>true</code> to show the panel, <code>false</code> to hire it
     */
    void setCommitBPanelVisible(boolean visible);

    /**
     * Sets dialog title.
     *
     * @param title
     *         title of dialog
     */
    void setTitle(String title);

    /** Clear all fields and panels of the view.*/
    void clear();

}
