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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;

/**
 * 'backtrace' command parser.
 *
 * @author Roman Nikitenko
 */
public class GdbBacktrace {

  private static final Pattern GDB_FILE_LOCATION =
      Pattern.compile("^([0-9]*) .* at (.*):([0-9]*).*", Pattern.DOTALL);
  private static final Pattern GDB_LIBRARY_LOCATION =
      Pattern.compile("^([0-9]*) .* from (.*)", Pattern.DOTALL);

  private final Map<Integer, Location> frames;

  public GdbBacktrace(Map<Integer, Location> frames) {
    this.frames = frames;
  }

  public Map<Integer, Location> getFrames() {
    return frames;
  }

  /** Factory method. */
  public static GdbBacktrace parse(GdbOutput gdbOutput) throws GdbParseException {
    Matcher matcher;
    final String output = gdbOutput.getOutput();
    final String[] framesInfo = output.split("#");
    final Map<Integer, Location> frames = new HashMap<>(framesInfo.length);

    for (String frame : framesInfo) {
      try {
        matcher = GDB_FILE_LOCATION.matcher(frame);
        if (matcher.find()) {
          final String fileLocation = matcher.group(2);
          final int lineNumber = Integer.parseInt(matcher.group(3));
          final int frameNumber = Integer.parseInt(matcher.group(1));

          final Location location = new LocationImpl(fileLocation, lineNumber);
          frames.put(frameNumber, location);
          continue;
        }

        matcher = GDB_LIBRARY_LOCATION.matcher(frame);
        if (matcher.find()) {
          final int frameNumber = Integer.parseInt(matcher.group(1));
          final String libraryLocation = matcher.group(2);
          final Location location = new LocationImpl(libraryLocation, -1);
          frames.put(frameNumber, location);
        }

      } catch (NumberFormatException e) {
        // we can't get info about current frame, but we are trying to get info about another frames
      }
    }

    if (!frames.isEmpty()) {
      return new GdbBacktrace(frames);
    }

    throw new GdbParseException(GdbBacktrace.class, output);
  }
}
