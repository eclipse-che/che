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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * A ANSI output stream extracts ANSI escape codes written to an output stream.
 *
 * <p>For more information about ANSI escape codes, see:
 * http://en.wikipedia.org/wiki/ANSI_escape_code
 *
 * <p>This class just filters out the escape codes so that they are not sent out to the underlying
 * OutputStream. Subclasses should actually perform the ANSI escape behaviors.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @author Joris Kuipers
 * @since 1.0
 */
public class AnsiOutputStream extends FilterOutputStream {

  public static final byte[] REST_CODE = "\033[0m".getBytes();;

  public AnsiOutputStream(OutputStream os) {
    super(os);
  }

  private static final int MAX_ESCAPE_SEQUENCE_LENGTH = 100;
  private byte buffer[] = new byte[MAX_ESCAPE_SEQUENCE_LENGTH];
  private int pos = 0;
  private int startOfValue;
  private final ArrayList<Object> options = new ArrayList<Object>();

  private static final int LOOKING_FOR_FIRST_ESC_CHAR = 0;
  private static final int LOOKING_FOR_SECOND_ESC_CHAR = 1;
  private static final int LOOKING_FOR_NEXT_ARG = 2;
  private static final int LOOKING_FOR_STR_ARG_END = 3;
  private static final int LOOKING_FOR_INT_ARG_END = 4;
  private static final int LOOKING_FOR_OSC_COMMAND = 5;
  private static final int LOOKING_FOR_OSC_COMMAND_END = 6;
  private static final int LOOKING_FOR_OSC_PARAM = 7;
  private static final int LOOKING_FOR_ST = 8;

  int state = LOOKING_FOR_FIRST_ESC_CHAR;

  private static final int FIRST_ESC_CHAR = 27;
  private static final int SECOND_ESC_CHAR = '[';
  private static final int SECOND_OSC_CHAR = ']';
  private static final int BEL = 7;
  private static final int SECOND_ST_CHAR = '\\';

  // TODO: implement to get perf boost: public void write(byte[] b, int off, int len)

  public void write(int data) throws IOException {
    switch (state) {
      case LOOKING_FOR_FIRST_ESC_CHAR:
        if (data == FIRST_ESC_CHAR) {
          buffer[pos++] = (byte) data;
          state = LOOKING_FOR_SECOND_ESC_CHAR;
        } else {
          out.write(data);
        }
        break;

      case LOOKING_FOR_SECOND_ESC_CHAR:
        buffer[pos++] = (byte) data;
        if (data == SECOND_ESC_CHAR) {
          state = LOOKING_FOR_NEXT_ARG;
        } else if (data == SECOND_OSC_CHAR) {
          state = LOOKING_FOR_OSC_COMMAND;
        } else {
          reset(false);
        }
        break;

      case LOOKING_FOR_NEXT_ARG:
        buffer[pos++] = (byte) data;
        if ('"' == data) {
          startOfValue = pos - 1;
          state = LOOKING_FOR_STR_ARG_END;
        } else if ('0' <= data && data <= '9') {
          startOfValue = pos - 1;
          state = LOOKING_FOR_INT_ARG_END;
        } else if (';' == data) {
          options.add(null);
        } else if ('?' == data) {
          options.add(new Character('?'));
        } else if ('=' == data) {
          options.add(new Character('='));
        } else {
          reset(processEscapeCommand(options, data));
        }
        break;

      case LOOKING_FOR_INT_ARG_END:
        buffer[pos++] = (byte) data;
        if (!('0' <= data && data <= '9')) {
          String strValue = new String(buffer, startOfValue, (pos - 1) - startOfValue, "UTF-8");
          Integer value = new Integer(strValue);
          options.add(value);
          if (data == ';') {
            state = LOOKING_FOR_NEXT_ARG;
          } else {
            reset(processEscapeCommand(options, data));
          }
        }
        break;

      case LOOKING_FOR_STR_ARG_END:
        buffer[pos++] = (byte) data;
        if ('"' != data) {
          String value = new String(buffer, startOfValue, (pos - 1) - startOfValue, "UTF-8");
          options.add(value);
          if (data == ';') {
            state = LOOKING_FOR_NEXT_ARG;
          } else {
            reset(processEscapeCommand(options, data));
          }
        }
        break;

      case LOOKING_FOR_OSC_COMMAND:
        buffer[pos++] = (byte) data;
        if ('0' <= data && data <= '9') {
          startOfValue = pos - 1;
          state = LOOKING_FOR_OSC_COMMAND_END;
        } else {
          reset(false);
        }
        break;

      case LOOKING_FOR_OSC_COMMAND_END:
        buffer[pos++] = (byte) data;
        if (';' == data) {
          String strValue = new String(buffer, startOfValue, (pos - 1) - startOfValue, "UTF-8");
          Integer value = new Integer(strValue);
          options.add(value);
          startOfValue = pos;
          state = LOOKING_FOR_OSC_PARAM;
        } else if ('0' <= data && data <= '9') {
          // already pushed digit to buffer, just keep looking
        } else {
          // oops, did not expect this
          reset(false);
        }
        break;

      case LOOKING_FOR_OSC_PARAM:
        buffer[pos++] = (byte) data;
        if (BEL == data) {
          String value = new String(buffer, startOfValue, (pos - 1) - startOfValue, "UTF-8");
          options.add(value);
          reset(processOperatingSystemCommand(options));
        } else if (FIRST_ESC_CHAR == data) {
          state = LOOKING_FOR_ST;
        } else {
          // just keep looking while adding text
        }
        break;

      case LOOKING_FOR_ST:
        buffer[pos++] = (byte) data;
        if (SECOND_ST_CHAR == data) {
          String value = new String(buffer, startOfValue, (pos - 2) - startOfValue, "UTF-8");
          options.add(value);
          reset(processOperatingSystemCommand(options));
        } else {
          state = LOOKING_FOR_OSC_PARAM;
        }
        break;
    }

    // Is it just too long?
    if (pos >= buffer.length) {
      reset(false);
    }
  }

  /**
   * Resets all state to continue with regular parsing
   *
   * @param skipBuffer if current buffer should be skipped or written to out
   * @throws IOException
   */
  private void reset(boolean skipBuffer) throws IOException {
    if (!skipBuffer) {
      out.write(buffer, 0, pos);
    }
    pos = 0;
    startOfValue = 0;
    options.clear();
    state = LOOKING_FOR_FIRST_ESC_CHAR;
  }

  /**
   * @param options
   * @param command
   * @return true if the escape command was processed.
   */
  private boolean processEscapeCommand(ArrayList<Object> options, int command) throws IOException {
    try {
      switch (command) {
        case 'A':
          processCursorUp(optionInt(options, 0, 1));
          return true;
        case 'B':
          processCursorDown(optionInt(options, 0, 1));
          return true;
        case 'C':
          processCursorRight(optionInt(options, 0, 1));
          return true;
        case 'D':
          processCursorLeft(optionInt(options, 0, 1));
          return true;
        case 'E':
          processCursorDownLine(optionInt(options, 0, 1));
          return true;
        case 'F':
          processCursorUpLine(optionInt(options, 0, 1));
          return true;
        case 'G':
          processCursorToColumn(optionInt(options, 0));
          return true;
        case 'H':
        case 'f':
          processCursorTo(optionInt(options, 0, 1), optionInt(options, 1, 1));
          return true;
        case 'J':
          processEraseScreen(optionInt(options, 0, 0));
          return true;
        case 'K':
          processEraseLine(optionInt(options, 0, 0));
          return true;
        case 'S':
          processScrollUp(optionInt(options, 0, 1));
          return true;
        case 'T':
          processScrollDown(optionInt(options, 0, 1));
          return true;
        case 'm':
          // Validate all options are ints...
          for (Object next : options) {
            if (next != null && next.getClass() != Integer.class) {
              throw new IllegalArgumentException();
            }
          }

          int count = 0;
          for (Object next : options) {
            if (next != null) {
              count++;
              int value = ((Integer) next).intValue();
              if (30 <= value && value <= 37) {
                processSetForegroundColor(value - 30);
              } else if (40 <= value && value <= 47) {
                processSetBackgroundColor(value - 40);
              } else {
                switch (value) {
                  case 39:
                    processDefaultTextColor();
                    break;
                  case 49:
                    processDefaultBackgroundColor();
                    break;
                  case 0:
                    processAttributeRest();
                    break;
                  default:
                    processSetAttribute(value);
                }
              }
            }
          }
          if (count == 0) {
            processAttributeRest();
          }
          return true;
        case 's':
          processSaveCursorPosition();
          return true;
        case 'u':
          processRestoreCursorPosition();
          return true;

        default:
          if ('a' <= command && 'z' <= command) {
            processUnknownExtension(options, command);
            return true;
          }
          if ('A' <= command && 'Z' <= command) {
            processUnknownExtension(options, command);
            return true;
          }
          return false;
      }
    } catch (IllegalArgumentException ignore) {
    }
    return false;
  }

  /**
   * @param options
   * @return true if the operating system command was processed.
   */
  private boolean processOperatingSystemCommand(ArrayList<Object> options) throws IOException {
    int command = optionInt(options, 0);
    String label = (String) options.get(1);
    // for command > 2 label could be composed (i.e. contain ';'), but we'll leave
    // it to processUnknownOperatingSystemCommand implementations to handle that
    try {
      switch (command) {
        case 0:
          processChangeIconNameAndWindowTitle(label);
          return true;
        case 1:
          processChangeIconName(label);
          return true;
        case 2:
          processChangeWindowTitle(label);
          return true;

        default:
          // not exactly unknown, but not supported through dedicated process methods:
          processUnknownOperatingSystemCommand(command, label);
          return true;
      }
    } catch (IllegalArgumentException ignore) {
    }
    return false;
  }

  protected void processRestoreCursorPosition() throws IOException {}

  protected void processSaveCursorPosition() throws IOException {}

  protected void processScrollDown(int optionInt) throws IOException {}

  protected void processScrollUp(int optionInt) throws IOException {}

  protected static final int ERASE_SCREEN_TO_END = 0;
  protected static final int ERASE_SCREEN_TO_BEGINING = 1;
  protected static final int ERASE_SCREEN = 2;

  protected void processEraseScreen(int eraseOption) throws IOException {}

  protected static final int ERASE_LINE_TO_END = 0;
  protected static final int ERASE_LINE_TO_BEGINING = 1;
  protected static final int ERASE_LINE = 2;

  protected void processEraseLine(int eraseOption) throws IOException {}

  protected static final int ATTRIBUTE_INTENSITY_BOLD = 1; // 	Intensity: Bold
  protected static final int ATTRIBUTE_INTENSITY_FAINT =
      2; // 	Intensity; Faint 	not widely supported
  protected static final int ATTRIBUTE_ITALIC =
      3; // 	Italic; on 	not widely supported. Sometimes treated as inverse.
  protected static final int ATTRIBUTE_UNDERLINE = 4; // 	Underline; Single
  protected static final int ATTRIBUTE_BLINK_SLOW = 5; // 	Blink; Slow 	less than 150 per minute
  protected static final int ATTRIBUTE_BLINK_FAST =
      6; // 	Blink; Rapid 	MS-DOS ANSI.SYS; 150 per minute or more
  protected static final int ATTRIBUTE_NEGATIVE_ON =
      7; // 	Image; Negative 	inverse or reverse; swap foreground and background
  protected static final int ATTRIBUTE_CONCEAL_ON = 8; // 	Conceal on
  protected static final int ATTRIBUTE_UNDERLINE_DOUBLE =
      21; // 	Underline; Double 	not widely supported
  protected static final int ATTRIBUTE_INTENSITY_NORMAL =
      22; // 	Intensity; Normal 	not bold and not faint
  protected static final int ATTRIBUTE_UNDERLINE_OFF = 24; // 	Underline; None
  protected static final int ATTRIBUTE_BLINK_OFF = 25; // 	Blink; off
  protected static final int ATTRIBUTE_NEGATIVE_Off = 27; // 	Image; Positive
  protected static final int ATTRIBUTE_CONCEAL_OFF = 28; // 	Reveal 	conceal off

  protected void processSetAttribute(int attribute) throws IOException {}

  protected static final int BLACK = 0;
  protected static final int RED = 1;
  protected static final int GREEN = 2;
  protected static final int YELLOW = 3;
  protected static final int BLUE = 4;
  protected static final int MAGENTA = 5;
  protected static final int CYAN = 6;
  protected static final int WHITE = 7;

  protected void processSetForegroundColor(int color) throws IOException {}

  protected void processSetBackgroundColor(int color) throws IOException {}

  protected void processDefaultTextColor() throws IOException {}

  protected void processDefaultBackgroundColor() throws IOException {}

  protected void processAttributeRest() throws IOException {}

  protected void processCursorTo(int row, int col) throws IOException {}

  protected void processCursorToColumn(int x) throws IOException {}

  protected void processCursorUpLine(int count) throws IOException {}

  protected void processCursorDownLine(int count) throws IOException {
    // Poor mans impl..
    for (int i = 0; i < count; i++) {
      out.write('\n');
    }
  }

  protected void processCursorLeft(int count) throws IOException {}

  protected void processCursorRight(int count) throws IOException {
    // Poor mans impl..
    for (int i = 0; i < count; i++) {
      out.write(' ');
    }
  }

  protected void processCursorDown(int count) throws IOException {}

  protected void processCursorUp(int count) throws IOException {}

  protected void processUnknownExtension(ArrayList<Object> options, int command) {}

  protected void processChangeIconNameAndWindowTitle(String label) {
    processChangeIconName(label);
    processChangeWindowTitle(label);
  }

  protected void processChangeIconName(String label) {}

  protected void processChangeWindowTitle(String label) {}

  protected void processUnknownOperatingSystemCommand(int command, String param) {}

  private int optionInt(ArrayList<Object> options, int index) {
    if (options.size() <= index) throw new IllegalArgumentException();
    Object value = options.get(index);
    if (value == null) throw new IllegalArgumentException();
    if (!value.getClass().equals(Integer.class)) throw new IllegalArgumentException();
    return ((Integer) value).intValue();
  }

  private int optionInt(ArrayList<Object> options, int index, int defaultValue) {
    if (options.size() > index) {
      Object value = options.get(index);
      if (value == null) {
        return defaultValue;
      }
      return ((Integer) value).intValue();
    }
    return defaultValue;
  }

  @Override
  public void close() throws IOException {
    write(REST_CODE);
    flush();
    super.close();
  }
}
