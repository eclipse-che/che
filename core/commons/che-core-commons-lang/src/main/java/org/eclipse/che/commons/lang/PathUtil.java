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
package org.eclipse.che.commons.lang;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/** Set of util methods for path strings. */
public class PathUtil {

  /**
   * Converts path to canonical form via traversing '..' and eliminating '.' and removing duplicate
   * separators. <b>Note:</b> This method works for Unix paths only.
   */
  public static String toCanonicalPath(String path, boolean removeLastSlash) {
    if (path == null || path.isEmpty()) {
      return path;
    }
    if (path.charAt(0) == '.') {
      if (path.length() == 1) {
        return "";
      }

      char c = path.charAt(1);
      if (c == '/') {
        path = path.substring(2);
      }
    }

    int index = -1;
    do {
      index = path.indexOf('/', index + 1);
      char next = index == path.length() - 1 ? 0 : path.charAt(index + 1);
      if (next == '.' || next == '/') {
        break;
      }

    } while (index != -1);

    if (index == -1) {
      if (removeLastSlash) {
        int start = processRoot(path, NullWriter.INSTANCE);
        int slashIndex = path.lastIndexOf('/');
        return slashIndex != -1 && slashIndex > start ? StringUtils.trimEnd(path, '/') : path;
      }
      return path;
    }
    String finalPath = path;
    Supplier<String> canonicalPath =
        () -> {
          try {
            return new File(finalPath).getCanonicalPath();
          } catch (IOException ignore) {
            return toCanonicalPath(finalPath, removeLastSlash);
          }
        };

    StringBuilder result = new StringBuilder(path.length());
    int start = processRoot(path, result);
    int dots = 0;
    boolean separator = true;

    for (int i = start; i < path.length(); ++i) {
      char c = path.charAt(i);
      if (c == '/') {
        if (!separator) {
          if (!processDots(result, dots, start)) {
            return canonicalPath.get();
          }
          dots = 0;
        }
        separator = true;
      } else if (c == '.') {
        if (separator || dots > 0) {
          ++dots;
        } else {
          result.append('.');
        }
        separator = false;
      } else {
        if (dots > 0) {
          StringUtils.repeatSymbol(result, '.', dots);
          dots = 0;
        }
        result.append(c);
        separator = false;
      }
    }

    if (dots > 0) {
      if (!processDots(result, dots, start)) {
        return canonicalPath.get();
      }
    }

    int lastChar = result.length() - 1;
    if (removeLastSlash && lastChar >= 0 && result.charAt(lastChar) == '/' && lastChar > start) {
      result.deleteCharAt(lastChar);
    }
    return result.toString();
  }

  private static boolean processDots(StringBuilder result, int dots, int start) {
    if (dots == 2) {
      int pos = -1;
      if (!StringUtils.endWith(result, "/../") && !StringUtils.equals(result, "../")) {
        pos = StringUtils.lastIndexOf(result, '/', start, result.length() - 1);
        if (pos >= 0) {
          ++pos;
        } else if (start > 0) {
          pos = start;
        } else if (result.length() > 0) {
          pos = 0;
        }
      }
      if (pos >= 0) {
        result.delete(pos, result.length());
      } else {
        result.append("../");
      }
    } else if (dots != 1) {
      StringUtils.repeatSymbol(result, '.', dots);
      result.append('/');
    }
    return true;
  }

  private static int processRoot(String path, Appendable appendable) {
    try {
      if (!path.isEmpty() && path.charAt(0) == '/') {
        appendable.append('/');
        return 1;
      }
      if (path.length() > 2 && path.charAt(1) == ':' && path.charAt(2) == '/') {
        appendable.append(path, 0, 3);
        return 3;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return 0;
  }

  private static final class NullWriter implements Appendable {

    public static final NullWriter INSTANCE = new NullWriter();

    @Override
    public Appendable append(CharSequence csq) throws IOException {
      return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
      return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
      return this;
    }
  }
}
