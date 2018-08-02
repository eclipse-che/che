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

import org.eclipse.che.api.languageserver.shared.model.ExtendedTextEdit;

/** Holds {@link ExtendedTextEdit} and file path */
public class RenameChange {

  private final ExtendedTextEdit textEdit;
  private final String filePath;

  public RenameChange(ExtendedTextEdit textEdit, String filePath) {
    this.textEdit = textEdit;
    this.filePath = filePath;
  }

  public ExtendedTextEdit getTextEdit() {
    return textEdit;
  }

  public String getFilePath() {
    return filePath;
  }
}
