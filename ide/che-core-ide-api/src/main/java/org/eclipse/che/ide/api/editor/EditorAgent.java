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
package org.eclipse.che.ide.api.editor;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.constraints.Direction;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Editor Agent manages Editors, it allows to open a new editor with given file,
 * retrieve current active editor and find all the opened editors.
 *
 * @author Nikolay Zamosenchuk
 */
public interface EditorAgent {
    /**
     * Open editor with given file
     *
     * @param file the file to open
     */
    void openEditor(@NotNull final VirtualFile file);

    /**
     * Open editor with given file and constraints.
     * Constraints contain info about way how view of editor should be displayed.
     * Editor will be opened in the current {@link EditorPartStack} if {@code constraints} is {@code null},
     * otherwise view of relative Editor Part will be split corresponding to {@code
     * constraints} on two areas and editor will be added into created area.
     * In the latter case you need to specify {@link Direction} and ID of relative {@link EditorTab}.
     *
     * @param file
     *         the file to open
     * @param constraints
     *         contains info about way how view of the editor should be opened
     */
    void openEditor(@NotNull final VirtualFile file, Constraints constraints);

    /**
     * Close editor part
     *
     * @param editorPart
     *         the part to close
     */
    void closeEditor(EditorPartPresenter editorPart);

    /**
     * Open editor with given file, call callback when editor fully loaded and initialized.
     * @param file the file to open
     * @param callback
     */
    void openEditor(@NotNull VirtualFile file, @NotNull OpenEditorCallback callback);

    /**
     * Sets editor as active(switch tabs and pace cursor)
     * @param editor the editor that must be active
     */
    void activateEditor(@NotNull EditorPartPresenter editor);

    /**
     * Returns array of EditorPartPresenters whose content have changed since the last save operation.
     *
     * @return Array<EditorPartPresenter>
     */
    List<EditorPartPresenter> getDirtyEditors();

    /**
     * Get all opened editors
     *
     * @return list with all opened editors
     */
    @NotNull
    List<EditorPartPresenter> getOpenedEditors();

    /**
     * Get all opened editors for given {@link EditorPartStack}
     *
     * @param editorPartStack
     *         editor part stack for searching opened editors
     * @return list with all opened editors for {@code editorPartStack}
     */
    @NotNull
    List<EditorPartPresenter> getOpenedEditorsFor(EditorPartStack editorPartStack);

    /**
     * Get opened editor by related file path for current {@link EditorPartStack}
     *
     * @param path path of the file opened in editor
     * @return opened editor or null if it does not exist
     */
    @Nullable
    EditorPartPresenter getOpenedEditor(Path path);

    /**
     * Saves all opened files whose content have changed since the last save operation
     *
     * @param callback
     */
    void saveAll(AsyncCallback callback);

    /**
     * Current active editor
     *
     * @return the current active editor
     */
    @Nullable
    EditorPartPresenter getActiveEditor();

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

    interface OpenEditorCallback {
        void onEditorOpened(EditorPartPresenter editor);

        void onEditorActivated(EditorPartPresenter editor);

        void onInitializationFailed();
    }
}
