/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.editor.sync;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.lsp4j.TextDocumentSyncKind;

/**
 * Provide synchronizes for according {@link TextDocumentSyncKind}
 *
 * @author Evgen Vidolob
 */
@Singleton
public class TextDocumentSynchronizeFactory {

  private static final TextDocumentSynchronize NONE = new NoneSynchronize();
  private final FullTextDocumentSynchronize fullTextDocumentSynchronize;
  private final IncrementalTextDocumentSynchronize incrementalTextDocumentSynchronize;

  @Inject
  public TextDocumentSynchronizeFactory(
      FullTextDocumentSynchronize fullTextDocumentSynchronize,
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
    public void syncTextDocument(
        Document document,
        TextPosition start,
        TextPosition end,
        int removedChars,
        String insertedText,
        int version) {
      // no-op implementation
    }
  }
}
