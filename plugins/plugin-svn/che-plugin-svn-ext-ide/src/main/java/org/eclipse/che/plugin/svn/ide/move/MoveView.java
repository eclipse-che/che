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

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.project.tree.TreeNode;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * View for {@link MovePresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
public interface MoveView extends View<MoveView.ActionDelegate> {
    /** Action handler for the view actions/controls. */
    public interface ActionDelegate extends BaseActionDelegate {
        void onMoveClicked();

        /** Perform actions when cancel button clicked. */
        void onCancelClicked();

        /** Perform actions when node selected in project explorer. */
        void onNodeSelected(TreeNode<?> destinationNode);

        /** Perform actions when node expanded in project explorer. */
        void onNodeExpanded(TreeNode<?> node);

        /** Perform actions when url fields changed. */
        void onUrlsChanged();
    }

    /** Set project tree nodes. */
    void setProjectNodes(List<TreeNode<?>> rootNodes);

    /** Update project tree node. */
    void updateProjectNode(@NotNull TreeNode<?> oldNode, @NotNull TreeNode<?> newNode);

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

    /** Return new name for target item. */
    String getNewName();

    /** Return target node, in case if we perform copying WC->WC. */
    TreeNode<?> getDestinationNode();

    /** Perform actions when close window performed. */
    void onClose();

    /** Perform actions when open window performed. */
    void onShow(boolean singleSelectedItem);
}
