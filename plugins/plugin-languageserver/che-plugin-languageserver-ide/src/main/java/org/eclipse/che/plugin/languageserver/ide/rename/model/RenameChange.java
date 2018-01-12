/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
