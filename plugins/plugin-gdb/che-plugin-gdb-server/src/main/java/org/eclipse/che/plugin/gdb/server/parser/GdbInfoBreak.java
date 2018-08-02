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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;

/**
 * 'info b' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbInfoBreak {

  private static final Pattern GDB_INFO_B =
      Pattern.compile("([0-9]*)\\s*breakpoint.*at\\s*(.*):([0-9]*).*");

  private final List<Breakpoint> breakpoints;

  private GdbInfoBreak(List<Breakpoint> breakpoints) {
    this.breakpoints = breakpoints;
  }

  public List<Breakpoint> getBreakpoints() {
    return breakpoints;
  }

  /** Factory method. */
  public static GdbInfoBreak parse(GdbOutput gdbOutput) throws GdbParseException {
    String output = gdbOutput.getOutput();

    List<Breakpoint> breakpoints = new ArrayList<>();

    for (String line : output.split("\n")) {
      Matcher matcher = GDB_INFO_B.matcher(line);
      if (matcher.find()) {
        String file = matcher.group(2);
        String lineNumber = matcher.group(3);

        Location location = new LocationImpl(file, Integer.parseInt(lineNumber));
        breakpoints.add(new BreakpointImpl(location));
      }
    }

    return new GdbInfoBreak(breakpoints);
  }
}
