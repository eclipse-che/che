/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.wsplugins.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Exec describes a "run in container" action. Command is the command line to execute inside the
 * container, the working directory for the command is root ('/') in the container's filesystem. T
 * he command is simply exec'd, it is not run inside a shell, so traditional shell instructions
 * ('|', etc) won't work. To use a shell, you need to explicitly call out to that shell. Exit status
 * of 0 is treated as live/healthy and non-zero is unhealthy.
 */
public class Exec {

  @JsonProperty("command")
  private List<String> command;

  public Exec command(List<String> commands) {
    this.command = commands;
    return this;
  }

  public List<String> getCommand() {
    if (command == null) {
      command = new ArrayList<>();
    }
    return command;
  }

  public void setCommand(List<String> command) {
    this.command = command;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Exec that = (Exec) o;
    return Objects.equals(getCommand(), that.getCommand());
  }

  @Override
  public int hashCode() {
    return Objects.hash(command);
  }

  @Override
  public String toString() {
    return "Exec{" + "command=" + command + '}';
  }
}
