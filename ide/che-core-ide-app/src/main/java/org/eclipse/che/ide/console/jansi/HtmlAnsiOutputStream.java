/**
 * Copyright (C) 2009, Progress Software Corporation and/or its subsidiaries or affiliates. All
 * rights reserved.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.che.ide.console.jansi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="http://code.dblock.org">Daniel Doubrovkine</a> */
public class HtmlAnsiOutputStream extends AnsiOutputStream {

  private boolean concealOn = false;

  private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

  @Override
  public void close() throws IOException {
    closeAttributes();
    super.close();
  }

  private static final String ANSI_COLOR_MAP[] = {
    "black", "red", "green", "yellow", "blue", "magenta", "cyan", "white",
  };

  private static final byte[] BYTES_QUOT = "&quot;".getBytes();
  private static final byte[] BYTES_AMP = "&amp;".getBytes();
  private static final byte[] BYTES_LT = "&lt;".getBytes();
  private static final byte[] BYTES_GT = "&gt;".getBytes();

  public HtmlAnsiOutputStream(OutputStream os) {
    super(os);
  }

  private List<String> closingAttributes = new ArrayList<>();

  private void write(String s) throws IOException {
    super.out.write(s.getBytes());
  }

  private void writeAttribute(String s) throws IOException {
    write("<" + s + ">");
    closingAttributes.add(0, s.split(" ", 2)[0]);
  }

  private void closeAttributes() throws IOException {
    for (String attr : closingAttributes) {
      write("</" + attr + ">");
    }
    closingAttributes.clear();
  }

  public void write(int data) throws IOException {
    // buffer.write(data);
    switch (data) {
      case 34: // "
        out.write(BYTES_QUOT);
        break;
      case 38: // &
        out.write(BYTES_AMP);
        break;
      case 60: // <
        out.write(BYTES_LT);
        break;
      case 62: // >
        out.write(BYTES_GT);
        break;
      default:
        super.write(data);
    }
  }

  public void writeLine(byte[] buf, int offset, int len) throws IOException {
    write(buf, offset, len);
    closeAttributes();
  }

  @Override
  protected void processSetAttribute(int attribute) throws IOException {
    switch (attribute) {
      case ATTRIBUTE_CONCEAL_ON:
        write("\u001B[8m");
        concealOn = true;
        break;
      case ATTRIBUTE_INTENSITY_BOLD:
        writeAttribute("b");
        break;
      case ATTRIBUTE_INTENSITY_NORMAL:
        closeAttributes();
        break;
      case ATTRIBUTE_UNDERLINE:
        writeAttribute("u");
        break;
      case ATTRIBUTE_UNDERLINE_OFF:
        closeAttributes();
        break;
      case ATTRIBUTE_NEGATIVE_ON:
        break;
      case ATTRIBUTE_NEGATIVE_Off:
        break;
    }
  }

  @Override
  protected void processAttributeRest() throws IOException {
    if (concealOn) {
      write("\u001B[0m");
      concealOn = false;
    }
    closeAttributes();
  }

  @Override
  protected void processSetForegroundColor(int color) throws IOException {
    writeAttribute("span style=\"color: " + ANSI_COLOR_MAP[color] + ";\"");
  }

  @Override
  protected void processSetBackgroundColor(int color) throws IOException {
    writeAttribute("span style=\"background-color: " + ANSI_COLOR_MAP[color] + ";\"");
  }
}
