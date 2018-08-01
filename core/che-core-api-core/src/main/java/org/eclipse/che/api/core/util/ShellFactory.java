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
package org.eclipse.che.api.core.util;

/** @author andrew00x */
public class ShellFactory {

  public static Shell getShell() {
    if (SystemInfo.isUnix()) {
      return new StandardLinuxShell();
    }
    if (SystemInfo.isWindows()) {
      return new StandardWindowsShell();
    }
    throw new IllegalStateException("Unsupported OS");
  }

  /** Creates command line to run system command from set of arguments. */
  public static interface Shell {
    String[] createShellCommand(CommandLine args);
  }

  /** Creates command line for standard command language interpreter. */
  public static class StandardLinuxShell implements Shell {

    @Override
    public String[] createShellCommand(CommandLine args) {
      final String[] array = args.asArray();
      if (array.length == 0) {
        throw new IllegalArgumentException("Command line is empty");
      }
      StringBuilder buff = new StringBuilder();
      for (String str : array) {
        if (!str.isEmpty()) {
          if (buff.length() > 0) {
            buff.append(' ');
          }
          for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            switch (c) {
              case ' ':
              case '|':
              case '>':
              case '$':
              case '"':
              case '\'':
              case '&':
              case '(':
              case ')':
              case '~':
              case '@':
              case '#':
              case '%':
              case '!':
              case '^':
              case '*':
                buff.append('\\');
                buff.append(c);
                break;
              case '\n':
                buff.append('\\');
                buff.append('n');
                break;
              case '\r':
                buff.append('\\');
                buff.append('r');
                break;
              case '\t':
                buff.append('\\');
                buff.append('t');
                break;
              case '\b':
                buff.append('\\');
                buff.append('b');
                break;
              case '\f':
                buff.append('\\');
                buff.append('f');
                break;
              default:
                buff.append(c);
                break;
            }
          }
        }
      }

      final String[] line = new String[3];
      line[0] = "/bin/bash";
      line[1] = "-cl";
      line[2] = buff.toString();
      return line;
    }
  }

  public static class StandardWindowsShell implements Shell {

    @Override
    public String[] createShellCommand(CommandLine args) {
      final String[] array = args.asArray();
      if (array.length == 0) {
        throw new IllegalArgumentException("Command line is empty");
      }
      StringBuilder buff = new StringBuilder();
      for (String str : array) {
        if (!str.isEmpty()) {
          if (buff.length() > 0) {
            buff.append(' ');
          }
          for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            switch (c) {
              case '|':
              case '\'':
              case '`':
              case '&':
              case ',':
              case ';':
              case '(':
              case ')':
              case '^':
              case '*':
                buff.append('^');
                buff.append(c);
                break;
              default:
                buff.append(c);
                break;
            }
          }
        }
      }

      final String[] line = new String[3];
      line[0] = "cmd.exe";
      line[1] = "/c";
      line[2] = buff.toString();
      return line;
    }
  }

  private ShellFactory() {}
}
