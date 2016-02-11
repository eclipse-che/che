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
package org.eclipse.che.ide.jseditor.client.texteditor;

/**
 * Front for an editor module, that allows to be warned when it's initialized.
 * @param <T> the type of the editor widget.
 */
public interface EditorModule<T extends EditorWidget> {

    /**
     * Tells if the editor module is initialized.
     * @return true iff the moduleis ready
     */
    boolean isReady();

    /**
     * Tells if the module initialization failed.
     * @return true iff the initialization failed
     */
    boolean isError();

    /**
     * Asks the module to warn the caller by using the provided callback.
     * @param callback the callback to call when the module is ready or failed
     */
    void waitReady(EditorModuleReadyCallback callback);

    /** Callback to call whe nthe module is ready of failed. */
    public interface EditorModuleReadyCallback {
        /** Used when the initialization is done. */
        void onEditorModuleReady();
        /** Used when the initialization failed. */
        void onEditorModuleError();
    }
}
