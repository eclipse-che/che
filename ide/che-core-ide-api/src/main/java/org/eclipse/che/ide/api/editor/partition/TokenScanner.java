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
package org.eclipse.che.ide.api.editor.partition;

import org.eclipse.che.ide.api.editor.text.rules.Token;

/**
 * A token scanner scans a range of a document and reports about the token it finds. A scanner has
 * state. When asked, the scanner returns the offset and the length of the last found token.
 *
 * @see Token
 */
public interface TokenScanner {
  /**
   * Configures the scanner by providing access to the document range that should be scanned.
   *
   * @param document the document to scan
   */
  void setScannedString(String document);

  /**
   * Returns the next token in the document.
   *
   * @return the next token in the document
   */
  Token nextToken();

  /**
   * Returns the offset of the last token read by this scanner.
   *
   * @return the offset of the last token read by this scanner
   */
  int getTokenOffset();

  /**
   * Returns the length of the last token read by this scanner.
   *
   * @return the length of the last token read by this scanner
   */
  int getTokenLength();
}
