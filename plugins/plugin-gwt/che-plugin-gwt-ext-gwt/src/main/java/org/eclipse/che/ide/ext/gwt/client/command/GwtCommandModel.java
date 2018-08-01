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
package org.eclipse.che.ide.ext.gwt.client.command;

import org.eclipse.che.ide.CommandLine;

/**
 * Model of the command line for launching GWT Code Server.
 *
 * @author Artem Zatsarynnyi
 */
class GwtCommandModel {

  private String workingDirectory;
  private String gwtModule;
  private String codeServerAddress;

  GwtCommandModel(String workingDirectory, String gwtModule, String codeServerAddress) {
    this.workingDirectory = workingDirectory;
    this.gwtModule = gwtModule;
    this.codeServerAddress = codeServerAddress;
  }

  /** Crates {@link GwtCommandModel} instance from the given command line. */
  static GwtCommandModel fromCommandLine(String commandLine) {
    final CommandLine cmd = new CommandLine(commandLine);

    String workingDirectory = null;
    String gwtModule = null;
    String codeServerAddress = null;

    if (cmd.hasArgument("-f")) {
      workingDirectory = cmd.getArgument(cmd.indexOf("-f") + 1);
    }

    for (String arg : cmd.getArguments()) {
      if (arg.startsWith("-Dgwt.module=")) {
        gwtModule = arg.split("=")[1];
      } else if (arg.startsWith("-Dgwt.bindAddress=")) {
        codeServerAddress = arg.split("=")[1];
      }
    }

    return new GwtCommandModel(
        workingDirectory, gwtModule != null ? gwtModule : "", codeServerAddress);
  }

  String getWorkingDirectory() {
    return workingDirectory;
  }

  void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  String getGwtModule() {
    return gwtModule;
  }

  void setGwtModule(String gwtModule) {
    this.gwtModule = gwtModule;
  }

  String getCodeServerAddress() {
    return codeServerAddress;
  }

  void setCodeServerAddress(String codeServerAddress) {
    this.codeServerAddress = codeServerAddress;
  }

  String toCommandLine() {
    final StringBuilder cmd = new StringBuilder(GwtCommandType.COMMAND_TEMPLATE);
    if (!workingDirectory.trim().isEmpty()) {
      cmd.append(" -f ").append(workingDirectory.trim());
    }
    if (!gwtModule.trim().isEmpty()) {
      cmd.append(" -Dgwt.module=").append(gwtModule.trim());
    }
    if (!codeServerAddress.trim().isEmpty()) {
      cmd.append(" -Dgwt.bindAddress=").append(codeServerAddress.trim());
    }

    return cmd.toString();
  }
}
