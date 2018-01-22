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
package org.eclipse.che.api.languageserver.util;

import java.io.IOException;
import java.io.Reader;

public class LineReader {
  private int nextLineStart;
  private int currentLineStart = -1;
  private String currentLine = "";
  private Reader in;
  private int currentLineIndex = -1;

  public LineReader(Reader in) throws IOException {
    this.in = in;
    readLine();
  }

  private boolean readLine() throws IOException {
    currentLineStart = nextLineStart;
    StringBuilder buf = new StringBuilder();
    int ch = in.read();
    nextLineStart++;
    while (ch >= 0 && ch != '\n') {
      if (ch != '\r') {
        buf.append((char) ch);
      }
      ch = in.read();
      nextLineStart++;
    }
    currentLine = buf.toString();
    currentLineIndex++;
    return ch == -1;
  }

  public void readTo(int offset) throws IOException {
    boolean wasEnd = false;
    while (offset >= nextLineStart) {
      if (wasEnd) {
        throw new IllegalArgumentException("Illegal offset: " + offset);
      }
      wasEnd = readLine();
    }
  }

  public String getCurrentLine() {
    return currentLine;
  }

  public int getCurrentLineIndex() {
    return currentLineIndex;
  }

  public int getCurrentLineStartOffset() {
    return currentLineStart;
  }
}
