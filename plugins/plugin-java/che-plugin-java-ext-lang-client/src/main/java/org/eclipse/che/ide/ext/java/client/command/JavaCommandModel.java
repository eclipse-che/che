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
package org.eclipse.che.ide.ext.java.client.command;

import org.eclipse.che.ide.CommandLine;

/**
 * Model of the Java command line.
 *
 * @author Artem Zatsarynnyi
 */
public class JavaCommandModel {

  private String mainClass;
  private String mainClassFQN;
  private String commandLine;

  public JavaCommandModel(String mainClass, String mainClassFQN, String commandLine) {
    this.mainClass = mainClass;
    this.mainClassFQN = mainClassFQN;
    this.commandLine = commandLine;
  }

  /** Crates {@link JavaCommandModel} instance from the given command line. */
  public static JavaCommandModel fromCommandLine(String commandLine) {
    final CommandLine cmd = new CommandLine(commandLine);

    String mainClass = null;
    String mainClassFQN = null;

    if (cmd.hasArgument("-d")) {
      mainClass = cmd.getArgument(cmd.indexOf("-d") + 2);
      mainClassFQN = cmd.getArgument(cmd.getArguments().size() - 1);
    }

    return new JavaCommandModel(mainClass, mainClassFQN, commandLine);
  }

  public String getMainClass() {
    return mainClass;
  }

  public void setMainClass(String mainClass) {
    this.mainClass = mainClass;
  }

  public String getMainClassFQN() {
    return mainClassFQN;
  }

  public String getCommandLine() {
    return commandLine;
  }

  public void setCommandLine(String commandLine) {
    this.commandLine = commandLine;
  }

  public String toCommandLine() {
    return commandLine;
  }
}
