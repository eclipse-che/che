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
package org.eclipse.che.plugin.gdb.server.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;

/**
 * 'print' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbPrint {

  private static final Pattern GDB_PRINT = Pattern.compile("\\$([0-9]*) = (.*)\n");

  private final String value;

  public GdbPrint(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /** Factory method. */
  public static GdbPrint parse(GdbOutput gdbOutput) throws GdbParseException {
    String output = gdbOutput.getOutput();

    Matcher matcher = GDB_PRINT.matcher(output);
    if (matcher.find()) {
      String value = matcher.group(2);
      return new GdbPrint(value);
    }

    throw new GdbParseException(GdbPrint.class, output);
  }
}
