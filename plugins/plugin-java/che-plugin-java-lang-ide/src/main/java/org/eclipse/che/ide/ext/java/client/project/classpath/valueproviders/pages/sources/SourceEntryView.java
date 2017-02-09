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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.sources;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.node.NodeWidget;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;

import java.util.Map;

/**
 * View interface for the information about source folders on the build path.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(SourceEntryViewImpl.class)
public interface SourceEntryView extends View<SourceEntryView.ActionDelegate> {
    /** Clear and Render sources. */
    void renderNodes();

    /**
     * Set sources map for displaying.
     *
     * @param data
     *         map which binds categories of the sources
     */
    void setData(Map<String, ClasspathEntryDto> data);

    /**
     * Removes node from the sources.
     *
     * @param nodeWidget
     *         widget which should be removed
     */
    void removeNode(NodeWidget nodeWidget);

    /** Sets enabled state of the 'Add Source' button. */
    void setAddSourceButtonState(boolean enabled);

    /** Clears sources panel. */
    void clear();

    interface ActionDelegate {
        /** Returns true if project is plain. */
        boolean isPlainJava();

        /** Performs some actions when user click on Add Jar button. */
        void onAddSourceClicked();

        /** Performs some actions when user click on Remove button. */
        void onRemoveClicked(String path);
    }
}
