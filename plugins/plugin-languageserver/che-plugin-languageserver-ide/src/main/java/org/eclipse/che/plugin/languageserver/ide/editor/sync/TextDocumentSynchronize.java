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
      Document document, TextPosition start, TextPosition end, String insertedText, int version);
}
