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
package org.eclipse.che.ide.part.widgets.editortab;

import com.google.gwt.event.dom.client.DoubleClickHandler;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.eclipse.che.ide.api.resources.VirtualFile;

import javax.validation.constraints.NotNull;

/**
 * @author Dmitry Shnurenko
 */
public interface EditorTab extends View<EditorTab.ActionDelegate>, TabItem, DoubleClickHandler {

    void setReadOnlyMark(boolean isVisible);

    void setErrorMark(boolean isVisible);

    void setWarningMark(boolean isVisible);

    /**
     * Return virtual file associated with editor tab.
     *
     * @return {@link VirtualFile} file
     */
    VirtualFile getFile();

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
    }

}
