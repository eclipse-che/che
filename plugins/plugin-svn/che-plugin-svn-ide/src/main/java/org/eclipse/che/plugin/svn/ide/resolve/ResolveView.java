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
package org.eclipse.che.plugin.svn.ide.resolve;

import org.eclipse.che.ide.api.mvp.View;

public interface ResolveView extends View<ResolveView.ActionDelegate> {

    public interface ActionDelegate {
        /** Click handler for the 'Cancel' button */
        void onCancelClicked();

        /** Click handler for the 'Resolve' button */
        void onResolveClicked();
    }

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();

    /** Init list of files with conflicts */
    void addConflictingFile(String filePath);

    /** Get current selected resolution action for given file */
    String getConflictResolutionAction(String filePath);
}
