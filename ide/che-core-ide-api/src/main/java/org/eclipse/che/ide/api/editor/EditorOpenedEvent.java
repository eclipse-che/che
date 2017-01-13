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

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * Event that describes the fact that editor is opened.
 *
 * @author Anatoliy Bazko
 */
public class EditorOpenedEvent extends GwtEvent<EditorOpenedEventHandler> {

    public static Type<EditorOpenedEventHandler> TYPE = new Type<>();

    private final VirtualFile         file;
    private final EditorPartPresenter editor;

    /**
     * Creates new {@link EditorOpenedEvent}.
     *
     * @param file
     *         an affected file
     * @param editor
     *         a visual component
     */
    public EditorOpenedEvent(VirtualFile file, EditorPartPresenter editor) {
        this.file = file;
        this.editor = editor;
    }

    /** {@inheritDoc} */
    @Override
    public Type<EditorOpenedEventHandler> getAssociatedType() {
        return TYPE;
    }

    /** @return an affected file */
    public VirtualFile getFile() {
        return file;
    }

    /** @return a visual component */
    public EditorPartPresenter getEditor() {
        return editor;
    }

    @Override
    protected void dispatch(EditorOpenedEventHandler handler) {
        handler.onEditorOpened(this);
    }
}
