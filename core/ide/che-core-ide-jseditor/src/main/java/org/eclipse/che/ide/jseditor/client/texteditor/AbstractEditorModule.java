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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.che.ide.util.loging.Log;

/**
 * Abstract implementation of {@link EditorModule}.
 * @param <T> the type of the editor
 */
public abstract class AbstractEditorModule<T extends EditorWidget> implements EditorModule<T> {

    private boolean ready = false;
    private boolean error = false;
    private boolean initializing = false;

    /** The registered callbacks. Will be set to ull once the editor is ready and all callbacks are called once. */
    private List<EditorModuleReadyCallback> callbacks = new ArrayList<>();

    /** The process to initialize the editor. */
    private EditorInitializer initializer;

    @Override
    public boolean isReady() {
        return this.ready;
    }

    /** Change the state of the module to 'ready'. */
    public void setReady() {
        this.ready = true;
        this.initializing = false;
        if (callbacks == null) {
            return;
        }
        // use all the callback to inform all the components that are waiting of the success
        for (final EditorModuleReadyCallback callback : callbacks) {
            callback.onEditorModuleReady();
        }
        this.callbacks = null;
    }

    /** Change the state of the module to 'error'. */
    public boolean isError() {
        return this.error;
    }

    public void setError() {
        this.error = true;
        this.initializing = false;
        if (callbacks == null) {
            return;
        }
        // use all the callback to inform all the components that are waiting of the failure
        for (final EditorModuleReadyCallback callback : callbacks) {
            callback.onEditorModuleError();
        }
        this.callbacks = null;
    }

    @Override
    public void waitReady(final EditorModuleReadyCallback callback) {
        if (ready) {
            Log.warn(this.getClass(), "Attempt to add a callback for 'editor ready' - ignored.");
            return;
        }
        if (!initializing ) {
            if (this.initializer == null) {
                throw new RuntimeException("Editor initializer not set");
            }
            this.initializing = true;
            this.initializer.initialize(new InitializerCallback() {
                @Override
                public void onSuccess() {
                    setReady();
                }
                @Override
                public void onFailure(final Throwable e) {
                    setError();
                }
            });
        }
        this.callbacks.add(callback);
    }

    /**
     * Sets the initializer.
     * @param initializer the initializer
     */
    public void setEditorInitializer(final EditorInitializer initializer) {
        this.initializer = initializer;
    }

    /** Process to initialize the editor module. */
    public interface EditorInitializer {
        /** initializes the editor module. */
        void initialize(InitializerCallback callback);
    }

    public interface InitializerCallback {
        void onSuccess();
        void onFailure(Throwable e);
    }
}
