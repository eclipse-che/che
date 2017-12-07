package org.eclipse.che.api.languageserver.util;

import java.io.IOException;
import java.io.Reader;

public class LineReader {
  private int nextLineStart;
  private String currentLine = "";
  private Reader in;

  public LineReader(Reader in) throws IOException {
    this.in = in;
    readLine();
  }

  private boolean readLine() throws IOException {
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
}
