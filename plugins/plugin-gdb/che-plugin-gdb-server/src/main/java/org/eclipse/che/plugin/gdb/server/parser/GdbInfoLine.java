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
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;

/**
 * 'info line' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbInfoLine {

  private static final Pattern GDB_INFO_LINE = Pattern.compile("Line ([0-9]*) of \"(.*)\"\\s.*");
  private static final Pattern GDB_LINE_OUT_OF_RANGE =
      Pattern.compile("Line number ([0-9]*) is out of range for \"(.*)\".*");

  private final Location location;

  public GdbInfoLine(Location location) {
    this.location = location;
  }

  public Location getLocation() {
    return location;
  }

  /** Factory method. */
  public static GdbInfoLine parse(GdbOutput gdbOutput) throws GdbParseException {
    String output = gdbOutput.getOutput();

    Matcher matcher = GDB_INFO_LINE.matcher(output);
    if (matcher.find()) {
      String lineNumber = matcher.group(1);
      String file = matcher.group(2);
      return new GdbInfoLine(new LocationImpl(file, Integer.parseInt(lineNumber)));
    }

    matcher = GDB_LINE_OUT_OF_RANGE.matcher(output);
    if (matcher.find()) {
      String lineNumber = matcher.group(1);
      String file = matcher.group(2);
      return new GdbInfoLine(new LocationImpl(file, Integer.parseInt(lineNumber)));
    }

    throw new GdbParseException(GdbInfoLine.class, output);
  }
}
