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

import com.google.gwt.user.client.ui.IsWidget;

import org.eclipse.che.ide.api.parts.AbstractPartPresenter;
import org.eclipse.che.ide.api.editor.EditorAgent.OpenEditorCallback;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of {@link EditorPartPresenter} that is intended
 * to be used by subclassing instead of directly implementing an interface.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public abstract class AbstractEditorPresenter extends AbstractPartPresenter implements EditorPartPresenter {
    protected boolean     dirtyState;
    protected EditorInput input;
    protected final List<EditorPartCloseHandler> closeHandlers = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public void init(@NotNull EditorInput input, final OpenEditorCallback callback) {
        this.input = input;
        initializeEditor(callback);
    }

    /** Initializes this editor. */
    protected abstract void initializeEditor(final OpenEditorCallback callback);

    /**
     * Set dirty state and notify expressions
     *
     * @param dirty
     */
    protected void updateDirtyState(boolean dirty) {
        dirtyState = dirty;
        firePropertyChange(EditorPartPresenter.TITLE_PROPERTY);
        firePropertyChange(PROP_DIRTY);
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        throw new UnsupportedOperationException("This method isn't supported in this class " + getClass());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDirty() {
        return dirtyState;
    }

    /** {@inheritDoc} */
    @Override
    public void addCloseHandler(@NotNull EditorPartCloseHandler closeHandler) {
        if (!closeHandlers.contains(closeHandler)) {
            closeHandlers.add(closeHandler);
        }
    }

    /** {@inheritDoc} */
    @Override
    public EditorInput getEditorInput() {
        return input;
    }

    protected void handleClose() {
        for (EditorPartCloseHandler handler : closeHandlers) {
            handler.onClose(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onFileChanged() {
        firePropertyChange(TITLE_PROPERTY);
    }
}
