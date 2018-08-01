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
 * 'info program' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbInfoProgram {

  private static final Pattern GDB_PROGRAM_STOPPED =
      Pattern.compile(".*Program stopped at (.*)[.]\n.*");
  private static final Pattern GDB_PROGRAM_FINISHED =
      Pattern.compile(".*The program being debugged is not being run.*");

  private final String address;

  public GdbInfoProgram(String address) {
    this.address = address;
  }

  public String getStoppedAddress() {
    return address;
  }

  /** Factory method. */
  public static GdbInfoProgram parse(GdbOutput gdbOutput) throws GdbParseException {
    String output = gdbOutput.getOutput();

    Matcher matcher = GDB_PROGRAM_FINISHED.matcher(output);
    if (matcher.find()) {
      return new GdbInfoProgram(null);
    }

    matcher = GDB_PROGRAM_STOPPED.matcher(output);
    if (matcher.find()) {
      String address = matcher.group(1);
      return new GdbInfoProgram(address);
    }

    throw new GdbParseException(GdbInfoProgram.class, output);
  }
}
