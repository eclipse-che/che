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
 * 'clear' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbClear {

  private static final Pattern GDB_CLEAR = Pattern.compile(".*Deleted breakpoint ([0-9]*).*");

  private GdbClear() {}

  /** Factory method. */
  public static GdbClear parse(GdbOutput gdbOutput) throws GdbParseException {
    String output = gdbOutput.getOutput();

    Matcher matcher = GDB_CLEAR.matcher(output);
    if (matcher.find()) {
      return new GdbClear();
    }

    throw new GdbParseException(GdbClear.class, output);
  }
}
