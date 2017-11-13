/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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
 * Gdb 'pwd' command output parser.
 *
 * @author Mykola Morhun
 */
public class GdbWorkDir {
  private static final Pattern GDB_WORK_DIR = Pattern.compile("Working directory (.*).\n");

  private final String workDir;

  private GdbWorkDir(String workDir) {
    this.workDir = workDir;
  }

  public String getWorkDir() {
    return workDir;
  }

  public static GdbWorkDir parse(GdbOutput gdbOutput) throws GdbParseException {
    String output = gdbOutput.getOutput();

    Matcher matcher = GDB_WORK_DIR.matcher(output);
    if (matcher.find()) {
      String workDir = matcher.group(1);
      return new GdbWorkDir(workDir);
    }

    throw new GdbParseException(GdbWorkDir.class, output);
  }
}
