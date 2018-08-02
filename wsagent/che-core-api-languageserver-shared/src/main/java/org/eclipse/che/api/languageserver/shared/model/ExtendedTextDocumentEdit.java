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
package org.eclipse.che.api.languageserver.shared.model;

import java.util.List;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

/** */
public class ExtendedTextDocumentEdit {
  private VersionedTextDocumentIdentifier textDocument;
  private List<ExtendedTextEdit> edits;

  public ExtendedTextDocumentEdit() {}

  public ExtendedTextDocumentEdit(
      VersionedTextDocumentIdentifier textDocument, List<ExtendedTextEdit> edits) {
    this.textDocument = textDocument;
    this.edits = edits;
  }

  public VersionedTextDocumentIdentifier getTextDocument() {
    return textDocument;
  }

  public void setTextDocument(VersionedTextDocumentIdentifier textDocument) {
    this.textDocument = textDocument;
  }

  public List<ExtendedTextEdit> getEdits() {
    return edits;
  }

  public void setEdits(List<ExtendedTextEdit> edits) {
    this.edits = edits;
  }
}
