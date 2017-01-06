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
package org.eclipse.che.ide.api.editor.events;

import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import com.google.gwt.event.shared.GwtEvent;

public class DocumentChangeEvent extends GwtEvent<DocumentChangeHandler> {

    /** The type instance for this event. */
    public static final Type<DocumentChangeHandler> TYPE = new Type<>();

    /** The document handle */
    private final DocumentHandle document;

    /** The document offset */
    private final int offset;

    /** Length of the replaced document text */
    private final int length;

    /** Text inserted into the document */
    private final String text;

    private final int removedCharCount;

    public DocumentChangeEvent(final DocumentHandle document, final int offset, final int length, final String text, int removedCharCount) {
        this.offset = offset;
        this.length = length;
        this.text = text;
        this.document = document;
        this.removedCharCount = removedCharCount;
    }

    @Override
    public Type<DocumentChangeHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final DocumentChangeHandler handler) {
        handler.onDocumentChange(this);
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public String getText() {
        return text;
    }

    public DocumentHandle getDocument() {
        return document;
    }

    public int getRemoveCharCount() {
        return removedCharCount;
    }
}
