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
package org.eclipse.che.ide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container for command line arguments.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandLine {

  private final List<String> arguments;

  /** Create empty container for a command line. */
  public CommandLine() {
    arguments = new ArrayList<>();
  }

  /** Create container for command line with the specified {@code args}. */
  public CommandLine(String... args) {
    arguments = new ArrayList<>();
    if (args != null && args.length > 0) {
      Collections.addAll(arguments, args);
    }
  }

  /** Create container for command line based on the specified {@code commandLine}. */
  public CommandLine(String commandLine) {
    final String[] args = commandLine.split(" +");
    arguments = new ArrayList<>();
    if (args.length > 0) {
      Collections.addAll(arguments, args);
    }
  }

  /** Get list of command line arguments. */
  public List<String> getArguments() {
    return new ArrayList<>(arguments);
  }

  /** Get command line argument by index. */
  public String getArgument(int index) {
    return arguments.get(index);
  }

  /** Get index of the specified command line argument. */
  public int indexOf(String arg) {
    return arguments.indexOf(arg);
  }

  /**
   * Adds list of arguments to command line.
   *
   * @param args arguments
   * @return this {@code CommandLine}
   */
  public CommandLine add(String... args) {
    if (args != null && args.length > 0) {
      Collections.addAll(arguments, args);
    }
    return this;
  }

  /**
   * Adds list of arguments to command line.
   *
   * @param args arguments
   * @return this {@code CommandLine}
   */
  public CommandLine add(List<String> args) {
    if (args != null && !args.isEmpty()) {
      arguments.addAll(args);
    }
    return this;
  }

  /** Checks whether the specified argument is present in this command line or not. */
  public boolean hasArgument(String arg) {
    return arguments.contains(arg);
  }

  /** Remove the specified argument from this command line. */
  public boolean removeArgument(String arg) {
    return arguments.remove(arg);
  }

  public String[] asArray() {
    return arguments.toArray(new String[arguments.size()]);
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
