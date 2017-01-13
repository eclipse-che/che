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
package org.eclipse.che.plugin.languageserver.ide.editor.sync;

import io.typefox.lsapi.TextDocumentSyncKind;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;

/**
 * Provide synchronizes for according {@link TextDocumentSyncKind}
 *
 * @author Evgen Vidolob
 */
@Singleton
public class TextDocumentSynchronizeFactory {

    private static final TextDocumentSynchronize NONE = new NoneSynchronize();
    private final FullTextDocumentSynchronize        fullTextDocumentSynchronize;
    private final IncrementalTextDocumentSynchronize incrementalTextDocumentSynchronize;

    @Inject
    public TextDocumentSynchronizeFactory(FullTextDocumentSynchronize fullTextDocumentSynchronize,
                                          IncrementalTextDocumentSynchronize incrementalTextDocumentSynchronize) {
        this.fullTextDocumentSynchronize = fullTextDocumentSynchronize;
        this.incrementalTextDocumentSynchronize = incrementalTextDocumentSynchronize;
    }

    public TextDocumentSynchronize getSynchronize(TextDocumentSyncKind kind) {
        if (kind == null) {
            // use NONE syncronizer if server doesn't require any
            return NONE;
        }
        switch (kind) {
            case None:
                return NONE;
            case Full:
                return fullTextDocumentSynchronize;
            case Incremental:
                return incrementalTextDocumentSynchronize;
            default:
                throw new RuntimeException("Unsupported synchronization kind: " + kind);

        }
    }


    private static class NoneSynchronize implements TextDocumentSynchronize {
        @Override
        public void syncTextDocument(DocumentChangeEvent event, int version) {
            //no implementation
        }
    }
}

