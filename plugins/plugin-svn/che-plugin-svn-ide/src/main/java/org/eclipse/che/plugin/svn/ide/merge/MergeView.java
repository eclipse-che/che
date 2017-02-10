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
package org.eclipse.che.plugin.svn.ide.merge;

import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.mvp.View;

/**
 * An interface representing Merge view.
 */
@ImplementedBy(MergeViewImpl.class)
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
        void onNodeSelected(Node destinationNode);
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
    void setRootNode(Node rootNode);

    /** Returns source URL check box. */
    HasValue<Boolean> sourceCheckBox();

    /** Returns source URL text box. */
    HasValue<String> sourceURLTextBox();

    /** Enables or disables Merge button. */
    void enableMergeButton(boolean enable);

    /** Sets an error to display. */
    void setError(String message);

}
