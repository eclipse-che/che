/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.toolbar.processes;

import java.util.Objects;

/** Data object for {@link Process}. */
public class ProcessImpl implements Process {

  private final String commandName;
  private final String commandLine;
  private final int pid;
  private final boolean alive;
  private final String machineName;

  public ProcessImpl(
      String commandName, String commandLine, int pid, boolean alive, String machineName) {
    this.commandName = commandName;
    this.commandLine = commandLine;
    this.pid = pid;
    this.alive = alive;
    this.machineName = machineName;
  }

  @Override
  public String getName() {
    return commandName;
  }

  @Override
  public String getCommandLine() {
    return commandLine;
  }

  @Override
  public int getPid() {
    return pid;
  }

  @Override
  public boolean isAlive() {
    return alive;
  }

  @Override
  public String getMachineName() {
    return machineName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProcessImpl process = (ProcessImpl) o;

    return pid == process.pid
        && alive == process.alive
        && Objects.equals(commandName, process.commandName)
        && Objects.equals(commandLine, process.commandLine)
        && Objects.equals(machineName, process.machineName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(commandName, commandLine, pid, alive, machineName);
  }
}
