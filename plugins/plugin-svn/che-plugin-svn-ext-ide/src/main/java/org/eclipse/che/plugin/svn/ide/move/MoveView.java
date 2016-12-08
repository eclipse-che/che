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
package org.eclipse.che.plugin.svn.ide.move;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * View for {@link MovePresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
@ImplementedBy(MoveViewImpl.class)
public interface MoveView extends View<MoveView.ActionDelegate> {
    /** Action handler for the view actions/controls. */
    interface ActionDelegate {
        void onMoveClicked();

        /** Perform actions when cancel button clicked. */
        void onCancelClicked();

        /** Perform actions when url fields changed. */
        void onUrlsChanged();
    }

    /** Set project tree nodes. */
    void setProject(Project project);

    /** Show error marker with specified message. */
    void showErrorMarker(String message);

    /** Hide error marker. */
    void hideErrorMarker();

    /** Return true if url check box selected. */
    boolean isURLSelected();

    /** Return source url. */
    String getSourceUrl();

    /** Return target url. */
    String getTargetUrl();

    /** Return comment. */
    String getComment();

    /** Return target node, in case if we perform copying WC->WC. */
    Resource getDestinationNode();

    /** Perform actions when close window performed. */
    void onClose();

    /** Perform actions when open window performed. */
    void onShow(boolean singleSelectedItem);
}
