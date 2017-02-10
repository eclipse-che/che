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
package org.eclipse.che.plugin.svn.ide.commit.diff;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View for {@link DiffViewerPresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
public interface DiffViewerView extends View<DiffViewerView.ActionDelegate> {

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {
        /** Perform actions when close button clicked. */
        void onCloseClicked();
    }

    /** Display diff for current file in console */
    void showDiff(String content);

    /** Perform actions when close window performed. */
    void onClose();

    /** Perform actions when open window performed. */
    void onShow();
}
