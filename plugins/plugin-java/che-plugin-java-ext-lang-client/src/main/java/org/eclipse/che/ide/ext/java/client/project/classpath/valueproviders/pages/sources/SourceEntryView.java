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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.sources;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.node.NodeWidget;

/**
 * View interface for the information about source folders on the build path.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(SourceEntryViewImpl.class)
public interface SourceEntryView extends View<SourceEntryView.ActionDelegate> {
    /**
     * Adds new node to the library.
     *
     * @param addedNode
     *         widget of the new lib node
     */
    void addNode(NodeWidget addedNode);

    /**
     * Removes node from the library.
     *
     * @param nodeWidget
     *         widget which should be removed
     */
    void removeNode(NodeWidget nodeWidget);

    /** Sets enabled state of the 'Add Source' button. */
    void setAddSourceButtonState(boolean enabled);

    /** Clears libraries panel. */
    void clear();

    interface ActionDelegate {
        /** Performs some actions when user click on Add Jar button. */
        void onAddSourceClicked();

        /** Performs some actions when user click on Remove button. */
        void onRemoveClicked();
    }
}
