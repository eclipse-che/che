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
package org.eclipse.che.plugin.svn.ide.merge;

import com.google.gwt.user.client.ui.HasValue;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.project.tree.TreeNode;

/**
 * An interface representing Merge view.
 */
public interface MergeView extends View<MergeView.ActionDelegate> {

    /** Delegate to handle user actions */
    interface ActionDelegate {

        /** Handle clicking Merge button. */
        void mergeClicked();

        /** Handle clicking Cancel button. */
        void cancelClicked();

        /** Perform actions when clicking Source URL check box. */
        void onSourceCheckBoxClicked();

        /** Perform actions when changing source URL. */
        void onSourceURLChanged(String sourceURL);

        /** Perform actions after node selection in project explorer. */
        void onNodeSelected(TreeNode<?> destinationNode);

        /** Perform actions after node expanding in project explorer. */
        void onNodeExpanded(TreeNode<?> node);

    }

    /**
     * Displays the view.
     */
    void show();

    /**
     * Hides the view.
     */
    void hide();

    /** Returns target text box */
    HasValue<String> targetTextBox();

    /** Sets root node to subversion tree. */
    void setRootNode(TreeNode<?> rootNode);

    /** Renders necessary node in subversion tree. */
    void render(TreeNode<?> node);

    /** Returns source URL check box. */
    HasValue<Boolean> sourceCheckBox();

    /** Returns source URL text box. */
    HasValue<String> sourceURLTextBox();

    /** Enables or disables Merge button. */
    void enableMergeButton(boolean enable);

    /** Sets an error to display. */
    void setError(String message);

}
