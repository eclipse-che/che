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

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;

import javax.validation.constraints.NotNull;

/**
 * Multi Part Stack is layout element, containing {@code EditorPartStack}s and provides methods to control them.
 *
 * @author Roman Nikitenko
 */
public interface EditorMultiPartStack extends PartStack {

    /**
     * Get active {@link EditorPartStack}
     *
     * @return active editor part stack or null if this one is absent
     */
    @Nullable
    EditorPartStack getActivePartStack();

    /**
     * Get {@link EditorPartStack} for given {@code part}
     *
     * @param part
     *         editor part to find corresponding editor part stack
     * @return editor part stack which contains given {@code part} or null if this one is not found in any {@link EditorPartStack}
     */
    @Nullable
    EditorPartStack getPartStackByPart(PartPresenter part);

    /**
     * Get editor part which associated with given {@code tabId}
     *
     * @param tabId
     *         ID of tab to find corresponding editor part
     * @return editor part or null if this one is not found in any {@link EditorPartStack}
     */
    @Nullable
    EditorPartPresenter getPartByTabId(@NotNull String tabId);

    /**
     * Get {@link EditorTab} for given {@code editorPart}
     *
     * @param editorPart
     *         editor part to find corresponding editor tab
     * @return tab for given {@code editorPart} or null if this one is not found in any {@link EditorPartStack}
     */
    @Nullable
    EditorTab getTabByPart(EditorPartPresenter editorPart);

    /**
     * Get next opened editor based on given {@code editorPart}
     *
     * @param editorPart
     *         the starting point to evaluate next opened editor
     * @return opened editor or null if it does not exist
     */
    @Nullable
    EditorPartPresenter getNextFor(EditorPartPresenter editorPart);

    /**
     * Get previous opened editor based on given {@code editorPart}
     *
     * @param editorPart
     *         the starting point to evaluate previous opened editor
     * @return opened editor or null if it does not exist
     */
    @Nullable
    EditorPartPresenter getPreviousFor(EditorPartPresenter editorPart);
}
