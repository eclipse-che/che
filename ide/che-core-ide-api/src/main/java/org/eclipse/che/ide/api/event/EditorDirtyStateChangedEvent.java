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
package org.eclipse.che.ide.api.event;

import com.google.gwt.event.shared.EventHandler;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Fires by editor when change dirty state(content modified or saved)
 *
 * @author Evgen Vidolob
 */
public class EditorDirtyStateChangedEvent extends GwtEvent<EditorDirtyStateChangedEvent.Handler> {

    public interface Handler extends EventHandler {

        /**
         * Editor became dirty, containing unsaved changes, or got saved
         *
         * @param event
         */
        void onEditorDirtyStateChanged(EditorDirtyStateChangedEvent event);

    }

    public static final GwtEvent.Type<Handler> TYPE = new Type<>();

    private EditorPartPresenter editor;

    /** @param editor */
    public EditorDirtyStateChangedEvent(EditorPartPresenter editor) {
        super();
        this.editor = editor;
    }

    /** {@inheritDoc} */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(Handler handler) {
        handler.onEditorDirtyStateChanged(this);
    }

    /** @return the editor */
    public EditorPartPresenter getEditor() {
        return editor;
    }

}
