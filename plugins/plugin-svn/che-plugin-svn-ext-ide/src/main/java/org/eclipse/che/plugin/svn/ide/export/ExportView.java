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
package org.eclipse.che.plugin.svn.ide.export;


import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * View for {@link ExportPresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
public interface ExportView extends View<ExportView.ActionDelegate> {

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {
        /** Perform actions when cancel button clicked. */
        void onCancelClicked();

        /** Perform actions when export button clicked. */
        void onExportClicked();
    }

    /** Get user revision. */
    String getRevision();

    /** Return true if user wants to specify exported revision. */
    boolean isRevisionSpecified();

    /** Perform actions when close window performed. */
    void onClose();

    /** Perform actions when open window performed. */
    void onShow();
}
