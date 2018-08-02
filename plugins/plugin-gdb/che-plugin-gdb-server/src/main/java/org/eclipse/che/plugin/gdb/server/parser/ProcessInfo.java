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
 * Parser to get info about running process. Output for parsing is supposed in the following format:
 * "<pid> <process name>", for example: "436 /projects/cpp/a.out"
 *
 * @author Roman Nikitenko
 */
public class ProcessInfo {

  private static final Pattern PROCESS_INFO = Pattern.compile("\\s*([0-9]*) (.*)", Pattern.DOTALL);

  private final int pid;
  private final String name;

  public ProcessInfo(String name, int pid) {
    this.name = name;
    this.pid = pid;
  }

  public String getProcessName() {
    return name;
  }

  public int getProcessId() {
    return pid;
  }

  /** Factory method. */
  public static ProcessInfo parse(String output) throws GdbParseException {
    final Matcher matcher = PROCESS_INFO.matcher(output);
    if (matcher.find()) {
      try {
        final int processId = Integer.parseInt(matcher.group(1));
        final String processName = matcher.group(2).replaceAll("\\s+", "");

        return new ProcessInfo(processName, processId);
      } catch (NumberFormatException e) {
        throw new GdbParseException(ProcessInfo.class, output);
      }
    }
    throw new GdbParseException(ProcessInfo.class, output);
  }
}
