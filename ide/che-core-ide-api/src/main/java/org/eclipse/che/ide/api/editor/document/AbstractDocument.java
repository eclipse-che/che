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
package org.eclipse.che.ide.api.editor.document;

import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.resources.VirtualFile;

/** An abstract implementation of {@link Document}. */
public abstract class AbstractDocument implements Document, DocumentHandle {

    /** The document event bus. */
    private final DocumentEventBus eventBus = new DocumentEventBus();

    /** The file holding the document. */
    private VirtualFile file;

    @Override
    public DocumentEventBus getDocEventBus() {
        return this.eventBus;
    }

    @Override
    public boolean isSameAs(final DocumentHandle document) {
        return (this.equals(document));
    }

    @Override
    public Document getDocument() {
        return this;
    }

    @Override
    public DocumentHandle getDocumentHandle() {
        return this;
    }

    @Override
    public void replace(int startLine, int startChar, int endLine, int endChar, String text) {
        // does nothing by default
    }

    @Override
    public void setFile(VirtualFile fileNode) {
        this.file = fileNode;
    }

    @Override
    public VirtualFile getFile() {
        return this.file;
    }

    @Override
    public ReadOnlyDocument getReadOnlyDocument() {
        return this;
    }

    @Override
    public void setSelectedRange(final TextRange range) {
        setSelectedRange(range, false);
    }

    @Override
    public void setSelectedRange(final TextRange range, final boolean show) {
        // does nothing by default
    }

    @Override
    public void setSelectedRange(final LinearRange range) {
        setSelectedRange(range, false);
    }
    
    @Override
    public void setSelectedRange(final LinearRange range, final boolean show) {
     // does nothing by default
    }
}
