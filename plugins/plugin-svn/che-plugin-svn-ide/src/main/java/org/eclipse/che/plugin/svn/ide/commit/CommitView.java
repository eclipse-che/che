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
package org.eclipse.che.plugin.svn.ide.commit;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.plugin.svn.shared.StatusItem;

import java.util.List;

/**
 * View for {@link CommitPresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
public interface CommitView extends View<CommitView.ActionDelegate> {

    /** Action handler for the view actions/controls. */
    public interface ActionDelegate {
        /** Perform actions when cancel button clicked. */
        void onCancelClicked();

        /** Perform actions when commit button clicked. */
        void onCommitClicked();

        /** Show diff for specified file in new window. */
        void showDiff(String path);

        /** Perform actions when commit mode changed. */
        void onCommitModeChanged();
    }

    /** Get commit message. */
    String getMessage();

    /** Return true if keep lock state check box selected. */
    boolean isKeepLocksStateSelected();

    /** Return true if commit all section selected. */
    boolean isCommitAllSelected();

    /** Return true if commit selection section selected. */
    boolean isCommitSelectionSelected();

    /** Set list of files that will be commited. */
    void setChangesList(List<StatusItem> changes);

    /** Perform actions when close window performed. */
    void onClose();

    /** Perform actions when open window performed. */
    void onShow();
}
