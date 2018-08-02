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
package org.eclipse.che.plugin.languageserver.ide.rename.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

/**
 * Synthetic object for tree, represent file where edits should apply, hods file name and list of
 * the edits
 */
public class RenameFile {

  private final String fileName;
  private final String filePath;
  private final List<RenameChange> changes;

  public RenameFile(String fileName, String filePath, List<RenameChange> changes) {
    this.fileName = fileName;
    this.filePath = filePath;
    this.changes = changes;
  }

  public String getFileName() {
    return fileName;
  }

  public List<RenameChange> getChanges() {
    return changes;
  }

  TextDocumentEdit getTextDocumentEdit() {
    VersionedTextDocumentIdentifier identifier = new VersionedTextDocumentIdentifier(-1);
    identifier.setUri(filePath);
    List<TextEdit> edits = new ArrayList<>();
    for (RenameChange change : changes) {
      edits.add(new TextEdit(change.getTextEdit().getRange(), change.getTextEdit().getNewText()));
    }
    return new TextDocumentEdit(identifier, edits);
  }
}
