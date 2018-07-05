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
package org.eclipse.che.ide.api.editor.formatter;

import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/** The interface of a document content formatter. The formatter formats ranges within documents. */
public interface ContentFormatter {
  /**
   * Formats the given region of the specified document.The org.eclipse.che.ide.api.editor.formatter
   * may safely assume that it is the only subject that modifies the document at this point in time.
   *
   * @param document the document to be formatted
   */
  void format(Document document);

  void install(TextEditor editor);

  /**
   * Indicates if the content formatter implementation can format the document at its current state.
   *
   * @param document the document to be formatted
   * @return
   */
  default boolean canFormat(Document document) {
    return true;
  }
}
