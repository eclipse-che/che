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
 * GDB version.
 *
 * @author Anatoliy Bazko
 */
public class GdbVersion {

  private static final Pattern GDB_VERSION = Pattern.compile("(GNU gdb \\(.*\\)) (.*)\n.*");

  private final String name;
  private final String version;

  public GdbVersion(String name, String version) {
    this.name = name;
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  /** Factory method. */
  public static GdbVersion parse(GdbOutput gdbOutput) throws GdbParseException {
    String output = gdbOutput.getOutput();

    Matcher matcher = GDB_VERSION.matcher(output);
    if (matcher.find()) {
      String name = matcher.group(1);
      String version = matcher.group(2);
      return new GdbVersion(name, version);
    }

    throw new GdbParseException(GdbVersion.class, output);
  }
}
