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
package org.eclipse.che.ide.api.editor;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.project.tree.VirtualFile;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

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
     * Close editor with given file
     *
     * @param file the file to close
     */
    void closeEditor(@NotNull final VirtualFile file);

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
     * @return map with all opened editors
     */
    @NotNull
    Map<String, EditorPartPresenter> getOpenedEditors();

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
     * Get next editor by relation to active editor
     * @return
     */
    @Nullable
    EditorPartPresenter getNextEditor();

    /**
     * Get previous editor by relation to active editor
     * @return
     */
    @Nullable
    EditorPartPresenter getPreviousEditor();

    /**
     * Get last opened editor
     * @return
     */
    @Nullable
    EditorPartPresenter getLastEditor();

    /**
     * Get first opened editor
     * @return
     */
    @Nullable
    EditorPartPresenter getFirstEditor();

    /**
     * Updates editor node. This method replace old editor node to new one
     *
     * @param path editor path
     * @param virtualFile new file for editor
     */
    void updateEditorNode(@NotNull String path, @NotNull VirtualFile virtualFile);

    interface OpenEditorCallback {
        void onEditorOpened(EditorPartPresenter editor);

        void onEditorActivated(EditorPartPresenter editor);

        void onInitializationFailed();
    }
}
