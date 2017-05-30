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
package org.eclipse.che.ide.ext.git.client.reset.commit;

import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The view of {@link ResetToCommitPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface ResetToCommitView extends View<ResetToCommitView.ActionDelegate> {
    /** Needs for delegate some function into ResetToCommit view. */
    public interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Reset button. */
        void onResetClicked();

        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();

        /**
         * Performs any action in response to the user having select revision.
         *
         * @param revision
         *         selected revision
         */
        void onRevisionSelected(@NotNull Revision revision);
    }

    /**
     * Set available revisions.
     *
     * @param revisions
     *         git revisions
     */
    void setRevisions(@NotNull List<Revision> revisions);

    /** @return <code>true</code> if soft mode is chosen, and <code>false</code> otherwise */
    boolean isSoftMode();

    /**
     * Select soft mode.
     *
     * @param isSoft
     *         <code>true</code> to select soft mode, <code>false</code> not to select
     */
    void setSoftMode(boolean isSoft);

    /** @return <code>true</code> if mix mode is chosen, and <code>false</code> otherwise */
    boolean isMixMode();

    /**
     * Select mix mode.
     *
     * @param isMix
     *         <code>true</code> to select mix mode, <code>false</code> not to select
     */
    void setMixMode(boolean isMix);

    /** @return <code>true</code> if hard mode is chosen, and <code>false</code> otherwise */
    boolean isHardMode();

    /**
     * Select mix mode.
     *
     * @param isHard
     *         <code>true</code> to select hard mode, <code>false</code> not to select
     */
    void setHardMode(boolean isHard);

//    /** @return <code>true</code> if keep mode is chosen, and <code>false</code> otherwise */
//    boolean isKeepMode();
//
//    /**
//     * Select keep mode.
//     *
//     * @param isKeep
//     *         <code>true</code> to select keep mode, <code>false</code> not to select
//     */
//    void setKeepMode(boolean isKeep);
//
//    /** @return <code>true</code> if merge mode is chosen, and <code>false</code> otherwise */
//    boolean isMergeMode();
//
//    /**
//     * Select merge mode.
//     *
//     * @param isMerge
//     *         <code>true</code> to select merge mode, <code>false</code> not to select
//     */
//    void setMergeMode(boolean isMerge);

    /**IDEUI-166 No cursor in terminal


     * Change the enable state of the reset button.
     *
     * @param enabled
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableResetButton(boolean enabled);

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();
}