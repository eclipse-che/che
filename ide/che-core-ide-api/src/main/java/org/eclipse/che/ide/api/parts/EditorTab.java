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
package org.eclipse.che.ide.api.parts;

import com.google.gwt.event.dom.client.DoubleClickHandler;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.eclipse.che.ide.api.resources.VirtualFile;

import javax.validation.constraints.NotNull;

/**
 * @author Dmitry Shnurenko
 */
public interface EditorTab extends View<EditorTab.ActionDelegate>, TabItem {

    void setReadOnlyMark(boolean isVisible);

    void setErrorMark(boolean isVisible);

    void setWarningMark(boolean isVisible);

    String getId();

    /**
     * Return virtual file associated with editor tab.
     *
     * @return {@link VirtualFile} file
     */
    VirtualFile getFile();

    /**
     * Sets associated file with editor tab.
     *
     * @param file
     *         associated file
     */
    void setFile(VirtualFile file);

    /**
     * Get editor part which associated with given tab
     *
     * @return editor part which associated with given tab
     */
    EditorPartPresenter getRelativeEditorPart();

    /**
     * Set pin mark to editor tab item.
     *
     * @param pinned
     *         true if tab should be pinned, otherwise false
     */
    void setPinMark(boolean pinned);

    /**
     * Indicates whether editor tab is either pinned or not.
     *
     * @return true if editor tab is pinned
     */
    boolean isPinned();

    interface ActionDelegate {

        void onTabClicked(@NotNull TabItem tab);

        void onTabClose(@NotNull TabItem tab);

        void onTabDoubleClicked(@NotNull TabItem tab);

    }

}
