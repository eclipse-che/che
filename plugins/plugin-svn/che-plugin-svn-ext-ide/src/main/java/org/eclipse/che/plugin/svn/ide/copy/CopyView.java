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
package org.eclipse.che.plugin.svn.ide.copy;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * View for {@link CopyPresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
@ImplementedBy(CopyViewImpl.class)
public interface CopyView extends View<CopyView.ActionDelegate> {

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {
        /** Perform actions when copy button clicked. */
        void onCopyClicked();

        /** Perform actions when cancel button clicked. */
        void onCancelClicked();

        /** Perform actions when node selected in project explorer. */
        void onNodeSelected(Resource target);

        /** Perform actions when new item name field changed. */
        void onNewNameChanged(String newName);

        /** Perform actions when source path changed. */
        void onSourcePathChanged();

        /** Perform actions when target path changed. */
        void onTargetUrlChanged();

        /** Perform actions when source url check box changed. */
        void onSourceCheckBoxChanged();

        /** Perform actions when target url check box changed. */
        void onTargetCheckBoxChanged();
    }

    /** Set title for the window according to copy type. e.g. file or directory. */
    void setDialogTitle(String title);

    /** Set project tree nodes. */
    void setProjectNode(Project project);

    /** Show error marker with specified message. */
    void showErrorMarker(String message);

    /** Hide error marker. */
    void hideErrorMarker();

    /** Set new item name field. */
    void setNewName(String name);

    /** Get new item name value. */
    String getNewName();

    /** Return true if url check box activated. */
    boolean isSourceCheckBoxSelected();

    /** Return true if url check box activated. */
    boolean isTargetCheckBoxSelected();

    /** Return url as source. */
    String getSourcePath();

    void setSourcePath(String path, boolean editable);

    /** Return url as target. */
    String getTargetUrl();

    /** Set dummy comment. */
    void setComment(String comment);

    /** Get user comment. */
    String getComment();

    /** Perform actions when open window performed. */
    void show();

    /** Perform actions when close window performed. */
    void hide();

}
