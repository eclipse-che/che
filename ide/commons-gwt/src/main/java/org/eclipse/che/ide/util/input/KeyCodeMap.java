// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util.input;

import elemental.events.KeyboardEvent.KeyCode;
import elemental.js.util.JsArrayOfInt;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.SignalEvent.KeySignalType;

/**
 * Provides a consistent map from developer-defined strings and web browser event.keyCode values to
 * an internal representation.
 *
 * <p>The internal representation is to map key codes to 7-bit ascii where possible, and map
 * non-ascii keys like page up to the Unicode Private Use Area U+E000...U+F8FF
 *
 * <p>NOTE: a...z are returned as uppercase A...Z, and only unshifted versions of symbols are
 * returned from the keyboard.
 */
public class KeyCodeMap {

  /** Map from keyCode to upper case UTF-16 */
  private static final JsArrayOfInt lowerToUpper;

  static {
    lowerToUpper = JsArrayOfInt.create();

    // special characters
    lowerToUpper.set('1', '!');
    lowerToUpper.set('2', '@');
    lowerToUpper.set('3', '#');
    lowerToUpper.set('4', '$');
    lowerToUpper.set('5', '%');
    lowerToUpper.set('6', '^');
    lowerToUpper.set('7', '&');
    lowerToUpper.set('8', '*');
    lowerToUpper.set('9', '(');
    lowerToUpper.set('0', ')');
    lowerToUpper.set('`', '~');
    lowerToUpper.set('-', '_');
    lowerToUpper.set('=', '+');
    lowerToUpper.set('[', '{');
    lowerToUpper.set(']', '}');
    lowerToUpper.set('\\', '|');
    lowerToUpper.set(';', ':');
    lowerToUpper.set('\'', '"');
    lowerToUpper.set(',', '<');
    lowerToUpper.set('.', '>');
    lowerToUpper.set('/', '?');
  }

  /** Internal representation of non-ascii characters */
  public static final int UNICODE_PRIVATE_START = 0xE000;

  public static final int UNICODE_PRIVATE_END = 0xF8FF;
  public static final int ARROW_UP = UNICODE_PRIVATE_START + 0;
  public static final int ARROW_DOWN = UNICODE_PRIVATE_START + 1;
  public static final int ARROW_LEFT = UNICODE_PRIVATE_START + 2;
  public static final int ARROW_RIGHT = UNICODE_PRIVATE_START + 3;
  public static final int INSERT = UNICODE_PRIVATE_START + 4;
  public static final int DELETE = UNICODE_PRIVATE_START + 5;
  public static final int HOME = UNICODE_PRIVATE_START + 6;
  public static final int END = UNICODE_PRIVATE_START + 7;
  public static final int PAGE_UP = UNICODE_PRIVATE_START + 8;
  public static final int PAGE_DOWN = UNICODE_PRIVATE_START + 9;
  public static final int MAC_META = UNICODE_PRIVATE_START + 10;
  public static final int F1 = UNICODE_PRIVATE_START + 11;
  public static final int F2 = F1 + 1;
  public static final int F3 = F1 + 2;
  public static final int F4 = F1 + 3;
  public static final int F5 = F1 + 4;
  public static final int F6 = F1 + 5;
  public static final int F7 = F1 + 6;
  public static final int F8 = F1 + 7;
  public static final int F9 = F1 + 8;
  public static final int F10 = F1 + 9;
  public static final int F11 = F1 + 10;
  public static final int F12 = F1 + 11;

  /** Bind browser field events */
  public static final int EVENT_COPY = UNICODE_PRIVATE_START + 100;

  public static final int EVENT_CUT = UNICODE_PRIVATE_START + 101;
  public static final int EVENT_PASTE = UNICODE_PRIVATE_START + 102;

  /** These map to ascii, but aren't easily written in a string representation */
  public static final int ENTER = 10;

  public static final int TAB = 9;
  public static final int ESC = 27;
  public static final int BACKSPACE = 8;

  /** Map from keyCode to ascii (for characters like :?><'[]) */
  private static final JsArrayOfInt keyCodeToAscii;

  static {
    keyCodeToAscii = JsArrayOfInt.create();
    keyCodeToAscii.set(KeyCode.SEMICOLON, ';');
    keyCodeToAscii.set(KeyCode.EQUALS, '=');
    keyCodeToAscii.set(KeyCode.COMMA, ',');
    keyCodeToAscii.set(KeyCode.DASH, '-');
    keyCodeToAscii.set(KeyCode.PERIOD, '.');
    keyCodeToAscii.set(KeyCode.SLASH, '/');
    keyCodeToAscii.set(KeyCode.APOSTROPHE, '`');
    keyCodeToAscii.set(KeyCode.OPEN_SQUARE_BRACKET, '[');
    keyCodeToAscii.set(KeyCode.CLOSE_SQUARE_BRACKET, ']');
    keyCodeToAscii.set(KeyCode.BACKSLASH, '\\');
    keyCodeToAscii.set(KeyCode.SINGLE_QUOTE, '\'');
    keyCodeToAscii.set(KeyCode.UP, ARROW_UP);
    keyCodeToAscii.set(KeyCode.DOWN, ARROW_DOWN);
    keyCodeToAscii.set(KeyCode.LEFT, ARROW_LEFT);
    keyCodeToAscii.set(KeyCode.RIGHT, ARROW_RIGHT);
    keyCodeToAscii.set(KeyCode.INSERT, INSERT);
    keyCodeToAscii.set(KeyCode.DELETE, DELETE);
    keyCodeToAscii.set(KeyCode.HOME, HOME);
    keyCodeToAscii.set(KeyCode.END, END);
    keyCodeToAscii.set(KeyCode.PAGE_UP, PAGE_UP);
    keyCodeToAscii.set(KeyCode.PAGE_DOWN, PAGE_DOWN);
    keyCodeToAscii.set(KeyCode.META, MAC_META); // left meta
    keyCodeToAscii.set(KeyCode.META + 1, MAC_META); // right meta
    keyCodeToAscii.set(KeyCode.CONTEXT_MENU, MAC_META);
  }

  private KeyCodeMap() {}

  /** Is this a letter/symbol that changes if the SHIFT key is pressed? */
  public static boolean needsShift(int keycode) {
    if ('A' <= keycode && keycode <= 'Z') {
      // upper case letter
      return true;
    }

    if (lowerToUpper.contains(keycode)) {
      // special character !@#$%...
      return true;
    }

    return false;
  }

  /**
   * Map from event.keyCode to internal representation (ascii+special keys)
   *
   * <p>NOTE(wetherbeei): SignalEvent tends to return correct ascii values from keyPress events
   * where possible, then keyCodes when they aren't available
   */
  public static int getKeyFromEvent(SignalEvent event) {
    int ascii = event.getKeyCode();

    if (ascii > 255) {
      // out of handling range - all keycodes handled are under 256
      return ascii;
    }

    KeySignalType type = event.getKeySignalType();
    if (type != KeySignalType.INPUT) {
      // convert these non-ascii characters to new unicode private area
      if (KeyCode.F1 <= ascii && ascii <= KeyCode.F12) {
        ascii = (ascii - KeyCode.F1) + F1;
      }

      if (keyCodeToAscii.isSet(ascii)) {
        ascii = keyCodeToAscii.get(ascii);
      }
    }

    // map enter \r (0x0D) to \n (0x0A)
    if (ascii == 0x0D) {
      ascii = 0x0A;
    }

    /*
     * Platform/browser specific modifications
     *
     * Firefox captures combos using keyPress, which returns the correct case of
     * the pressed key in ascii. Other browsers are captured on keyDown and only
     * return the keyCode value (upper case or 0-9) of the pressed key
     *
     * TODO: test on other browsers.
     */
    if (UserAgent.isFirefox() && event.getType().equals("keypress")) {
      // this is a combo event, leave it alone in firefox
    } else if (event.getType().equals("keydown")) {
      // other browsers keydown combo captured, need to convert keyCode to ascii

      // upper case letters to lower if no shift key
      if (!event.getShiftKey()) {
        if ('A' <= ascii && ascii <= 'Z') {
          ascii = ascii - 'A' + 'a';
        }
      } else {
        // shift key, check for additional symbol changes
        if (lowerToUpper.isSet(ascii)) {
          ascii = lowerToUpper.get(ascii);
        }
      }
    }

    // anything in other ranges will be passed through
    return ascii;
  }

  /**
   * Is this a printable ascii character? Includes control flow characters like newline (\n) and
   * carriage return (\r).
   */
  public static boolean isPrintable(int letter) {
    if ((letter < 0x20 && letter != 0x0A && letter != 0x0D) || letter == 0x7F) {
      // control characters less than 0x20, but not \n 0x0A, \r 0x0D
      // or delete key 0x7F
      return false; // not printable
    }
    if (UNICODE_PRIVATE_START <= letter && letter <= UNICODE_PRIVATE_END) {
      // reserved internal range for events, not printable
      return false;
    }
    // everything else is printable
    return true;
  }

  /**
   * Returns a String describing the keyCode, such as "HOME", "F1" or "A".
   *
   * @return a string containing a text description for a physical key, identified by its keyCode
   */
  public static String getKeyText(int keyCode) {
    if ((keyCode >= '0' && keyCode <= '9')
        || (keyCode >= 'A' && keyCode <= 'Z')
        || (keyCode >= 'a' && keyCode <= 'z')) {
      return String.valueOf((char) keyCode);
    }
    switch (keyCode) {
      case F1:
        return "F1";
      case F2:
        return "F2";
      case F3:
        return "F3";
      case F4:
        return "F4";
      case F5:
        return "F5";
      case F6:
        return "F6";
      case F7:
        return "F7";
      case F8:
        return "F8";
      case F9:
        return "F9";
      case F10:
        return "F10";
      case F11:
        return "F11";
      case F12:
        return "F12";
      case INSERT:
        return "Insert";
      case ENTER:
        return "Enter";
      case ARROW_LEFT:
        return "←";
      case ARROW_RIGHT:
        return "→";
      case ARROW_UP:
        return "↑";
      case ARROW_DOWN:
        return "↓";
      case DELETE:
        return "Delete";
        // todo add others keys
    }

    return "";
  }
}
