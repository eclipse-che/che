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
package org.eclipse.che.ide.part.editor.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.ide.api.project.tree.VirtualFile;

/**
 * Event fires when editor tab either pinned or not.
 *
 * @author Vlad Zhukovskiy
 */
public class PinEditorTabEvent extends GwtEvent<PinEditorTabEvent.PinEditorTabEventHandler> {

    public interface PinEditorTabEventHandler extends EventHandler {
        void onEditorTabPinned(PinEditorTabEvent event);
    }

    private static Type<PinEditorTabEventHandler> TYPE;

    public static Type<PinEditorTabEventHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private final VirtualFile file;
    private final boolean     pin;

    public PinEditorTabEvent(VirtualFile file, boolean pin) {
        this.file = file;
        this.pin = pin;
    }

    /** {@inheritDoc} */
    @Override
    public Type<PinEditorTabEventHandler> getAssociatedType() {
        return getType();
    }

    /**
     * Return file associated with pin operation.
     *
     * @return {@link VirtualFile} file
     */
    public VirtualFile getFile() {
        return file;
    }

    /**
     * Return true if opened file should be pinned.
     *
     * @return true if opened file should be pinned
     */
    public boolean isPin() {
        return pin;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(PinEditorTabEventHandler handler) {
        handler.onEditorTabPinned(this);
    }
}