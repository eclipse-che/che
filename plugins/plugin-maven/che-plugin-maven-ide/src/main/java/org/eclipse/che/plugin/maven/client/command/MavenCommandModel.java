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
package org.eclipse.che.plugin.maven.client.command;

import org.eclipse.che.ide.CommandLine;

/**
 * Model of the Maven command line.
 *
 * @author Artem Zatsarynnyi
 */
class MavenCommandModel {

  private String workingDirectory;
  private String arguments;

  // Note that Closure Compiler doesn't allow to use 'arguments' as a name of a method argument.
  MavenCommandModel(String workingDirectory, String args) {
    this.workingDirectory = workingDirectory;
    this.arguments = args;
  }

  /** Crates {@link MavenCommandModel} instance from the given command line. */
  static MavenCommandModel fromCommandLine(String commandLine) {
    final CommandLine cmd = new CommandLine(commandLine);

    String workingDirectory = null;

    if (cmd.hasArgument("-f")) {
      workingDirectory = cmd.getArgument(cmd.indexOf("-f") + 1);

      cmd.removeArgument("-f");
      cmd.removeArgument(workingDirectory);
    }

    cmd.removeArgument("mvn");
    String arguments = cmd.toString();

    return new MavenCommandModel(workingDirectory, arguments);
  }

  String getWorkingDirectory() {
    return workingDirectory;
  }

  void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  String getArguments() {
    return arguments;
  }

  /**
   * Set command arguments, e.g. {@code [options] [<goal(s)>] [<phase(s)>]}.
   *
   * <p>Note that Closure Compiler doesn't allow to use 'arguments' as a name of a method argument.
   */
  void setArguments(String args) {
    this.arguments = args;
  }

  String toCommandLine() {
    final StringBuilder cmd = new StringBuilder("mvn");

    if (!workingDirectory.trim().isEmpty()) {
      cmd.append(" -f ").append(workingDirectory.trim());
    }

    if (!arguments.trim().isEmpty()) {
      cmd.append(' ').append(arguments.trim());
    }

    return cmd.toString();
  }
}
