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
package org.eclipse.che.ide.ext.git.client.reset.files;

import org.eclipse.che.api.git.shared.IndexFile;
import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The view of {@link org.eclipse.che.ide.ext.git.client.reset.commit.ResetToCommitPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface ResetFilesView extends View<ResetFilesView.ActionDelegate> {
    String FILES = "Files for commit";

    /** Needs for delegate some function into ResetFiles view. */
    public interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Reset button. */
        void onResetClicked();

        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();
    }

    /**
     * Set indexed files into table on view.
     *
     * @param indexedFiles
     *         indexed files
     */
    void setIndexedFiles(@NotNull List<IndexFile> indexedFiles);

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();
}