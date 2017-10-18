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
package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.events.DocumentChangedHandler;
import org.eclipse.che.ide.api.editor.events.DocumentChangingEvent;
import org.eclipse.che.ide.api.editor.events.DocumentChangingHandler;
import org.eclipse.che.ide.api.editor.reconciler.DirtyRegion;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilingStrategy;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.plugin.languageserver.ide.editor.sync.TextDocumentSynchronize;
import org.eclipse.che.plugin.languageserver.ide.editor.sync.TextDocumentSynchronizeFactory;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Responsible for document synchronization
 *
 * @author Evgen Vidolob
 */
public class LanguageServerReconcileStrategy implements ReconcilingStrategy {

  private final TextDocumentSynchronize synchronize;
  private int version = 0;
  private TextPosition lastEventStart;
  private TextPosition lastEventEnd;

  @Inject
  public LanguageServerReconcileStrategy(
      TextDocumentSynchronizeFactory synchronizeFactory,
      @Assisted ServerCapabilities serverCapabilities) {

    Either<TextDocumentSyncKind, TextDocumentSyncOptions> sync =
        serverCapabilities.getTextDocumentSync();
    TextDocumentSyncKind documentSync = null;
    // sync may be null
    if (sync != null) {
      if (sync.isLeft()) {
        documentSync = sync.getLeft();
      } else {
        documentSync = sync.getRight().getChange();
      }
    }

    synchronize = synchronizeFactory.getSynchronize(documentSync);
  }

  @Override
  public void setDocument(Document document) {
    document
        .getDocumentHandle()
        .getDocEventBus()
        .addHandler(
            DocumentChangedEvent.TYPE,
            new DocumentChangedHandler() {
              @Override
              public void onDocumentChanged(DocumentChangedEvent event) {
                synchronize.syncTextDocument(
                    event.getDocument().getDocument(),
                    lastEventStart,
                    lastEventEnd,
                    event.getRemoveCharCount(),
                    event.getText(),
                    ++version);
              }
            });
    document
        .getDocumentHandle()
        .getDocEventBus()
        .addHandler(
            DocumentChangingEvent.TYPE,
            new DocumentChangingHandler() {
              @Override
              public void onDocumentChanging(DocumentChangingEvent event) {
                lastEventStart =
                    event.getDocument().getDocument().getPositionFromIndex(event.getOffset());
                lastEventEnd =
                    event
                        .getDocument()
                        .getDocument()
                        .getPositionFromIndex(event.getOffset() + event.getRemoveCharCount());
              }
            });
  }

  @Override
  public void reconcile(DirtyRegion dirtyRegion, Region subRegion) {
    doReconcile();
  }

  public void doReconcile() {
    // TODO use DocumentHighlight to add additional highlight for file
  }

  @Override
  public void reconcile(Region partition) {
    doReconcile();
  }

  @Override
  public void closeReconciler() {}
}
