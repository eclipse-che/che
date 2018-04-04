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
package org.eclipse.che.api.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Container for system command arguments.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public class CommandLine {
  private final List<String> line;

  public CommandLine(CommandLine other) {
    line = new ArrayList<>(other.line);
  }

  public CommandLine(String... args) {
    line = new ArrayList<>();
    if (args != null && args.length > 0) {
      Collections.addAll(line, args);
    }
  }

  public CommandLine() {
    line = new ArrayList<>();
  }

  /**
   * Adds list of arguments in command line.
   *
   * @param args arguments
   * @return this {@code CommandLine}
   */
  public CommandLine add(String... args) {
    if (args != null && args.length > 0) {
      Collections.addAll(line, args);
    }
    return this;
  }

  /**
   * Adds list of arguments in command line.
   *
   * @param args arguments
   * @return this {@code CommandLine}
   */
  public CommandLine add(List<String> args) {
    if (args != null && !args.isEmpty()) {
      line.addAll(args);
    }
    return this;
  }

  /**
   * Adds set of options in command line.
   *
   * @param options options
   * @return this {@code CommandLine}
   * @see #addPair(String, String)
   */
  public CommandLine add(Map<String, String> options) {
    if (options != null && !options.isEmpty()) {
      for (Map.Entry<String, String> entry : options.entrySet()) {
        addPair(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  /**
   * Adds option in command line. If {@code value != null} then adds {@code name=value} in command
   * line. If {@code value} is {@code null} adds only {@code name} in command line.
   *
   * @param name option's name
   * @param value option's value
   * @return this {@code CommandLine}
   */
  public CommandLine addPair(String name, String value) {
    if (name != null) {
      if (value != null) {
        line.add(name + '=' + value);
      } else {
        line.add(name);
      }
    }
    return this;
  }

  /**
   * Removes all command line arguments.
   *
   * @return this {@code CommandLine}
   */
  public CommandLine clear() {
    line.clear();
    return this;
  }

  public String[] asArray() {
    return line.toArray(new String[line.size()]);
  }

  /** Create shell command. */
  public String[] toShellCommand() {
    return ShellFactory.getShell().createShellCommand(this);
  }

  @Override
  public String toString() {
    final String[] str = asArray();
    final StringBuilder sb = new StringBuilder();
    for (String s : str) {
      if (sb.length() > 1) {
        sb.append(' ');
      }
      sb.append(s);
    }
    return sb.toString();
  }
}
