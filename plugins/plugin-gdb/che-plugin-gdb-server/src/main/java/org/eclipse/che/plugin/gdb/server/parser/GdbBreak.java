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
package org.eclipse.che.plugin.gdb.server.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;

/**
 * 'break' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbBreak {

  private static final Pattern GDB_BREAK =
      Pattern.compile(".*Breakpoint ([0-9]*) at (.*): file (.*), line ([0-9]*).*");

  private final String address;
  private final String file;
  private final String lineNumber;

  private GdbBreak(String address, String file, String lineNumber) {
    this.address = address;
    this.file = file;
    this.lineNumber = lineNumber;
  }

  public String getAddress() {
    return address;
  }

  public String getFile() {
    return file;
  }

  public String getLineNumber() {
    return lineNumber;
  }

  /** Factory method. */
  public static GdbBreak parse(GdbOutput gdbOutput) throws GdbParseException {
    String output = gdbOutput.getOutput();

    Matcher matcher = GDB_BREAK.matcher(output);
    if (matcher.find()) {
      String address = matcher.group(2);
      String file = matcher.group(3);
      String lineNumber = matcher.group(4);
      return new GdbBreak(address, file, lineNumber);
    }

    throw new GdbParseException(GdbBreak.class, output);
  }
}
