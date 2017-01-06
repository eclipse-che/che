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
package org.eclipse.che.ide.api.editor.texteditor;

/**
 * Front for an editor module, that allows to be warned when it's initialized.
 */
@Deprecated
public interface EditorModule {

    /**
     * Tells if the editor module is initialized.
     * @return true if the module is ready
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

    /** Callback to call when the module is ready of failed. */
    @Deprecated
    interface EditorModuleReadyCallback {
        /** Used when the initialization is done. */
        void onEditorModuleReady();
        /** Used when the initialization failed. */
        void onEditorModuleError();
    }
}
