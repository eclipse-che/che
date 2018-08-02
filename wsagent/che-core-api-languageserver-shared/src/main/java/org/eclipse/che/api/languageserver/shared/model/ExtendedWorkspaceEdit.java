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

/** */
public class ExtendedWorkspaceEdit {
  private List<ExtendedTextDocumentEdit> documentChanges;

  public ExtendedWorkspaceEdit() {}

  public ExtendedWorkspaceEdit(List<ExtendedTextDocumentEdit> documentChanges) {
    this.documentChanges = documentChanges;
  }

  public List<ExtendedTextDocumentEdit> getDocumentChanges() {
    return documentChanges;
  }

  public void setDocumentChanges(List<ExtendedTextDocumentEdit> documentChanges) {
    this.documentChanges = documentChanges;
  }
}
