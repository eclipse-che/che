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

import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;

/**
 * Handle TextDocument synchronization
 *
 * @author Evgen Vidolob
 */
public interface TextDocumentSynchronize {
  void syncTextDocument(
      Document document,
      TextPosition start,
      TextPosition end,
      int removedChars,
      String insertedText,
      int version);
}
