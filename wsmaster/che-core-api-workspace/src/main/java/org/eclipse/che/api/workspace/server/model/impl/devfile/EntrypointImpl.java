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
package org.eclipse.che.api.workspace.server.model.impl.devfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.devfile.Entrypoint;

/** @author Sergii Leshchenko */
public class EntrypointImpl implements Entrypoint {

  private String parentName;
  private Map<String, String> parentSelector;
  private String containerName;
  private List<String> command;
  private List<String> args;

  public EntrypointImpl() {}

  public EntrypointImpl(
      String parentName,
      Map<String, String> parentSelector,
      String containerName,
      List<String> command,
      List<String> args) {
    this.parentName = parentName;
    this.parentSelector = parentSelector;
    this.containerName = containerName;
    this.command = command;
    this.args = args;
  }

  public EntrypointImpl(Entrypoint entrypoint) {
    this(
        entrypoint.getParentName(),
        entrypoint.getParentSelector(),
        entrypoint.getContainerName(),
        entrypoint.getCommand(),
        entrypoint.getArgs());
  }

  @Override
  public String getParentName() {
    return parentName;
  }

  public void setParentName(String parentName) {
    this.parentName = parentName;
  }

  @Override
  public Map<String, String> getParentSelector() {
    if (parentSelector == null) {
      parentSelector = new HashMap<>();
    }
    return parentSelector;
  }

  public void setParentSelector(Map<String, String> parentSelector) {
    this.parentSelector = parentSelector;
  }

  @Override
  public String getContainerName() {
    return containerName;
  }

  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }

  @Override
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
  public List<String> getArgs() {
    if (args == null) {
      args = new ArrayList<>();
    }
    return args;
  }

  public void setArgs(List<String> args) {
    this.args = args;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EntrypointImpl)) {
      return false;
    }
    EntrypointImpl that = (EntrypointImpl) o;
    return Objects.equals(getParentName(), that.getParentName())
        && Objects.equals(getParentSelector(), that.getParentSelector())
        && Objects.equals(getContainerName(), that.getContainerName())
        && Objects.equals(getCommand(), that.getCommand())
        && Objects.equals(getArgs(), that.getArgs());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getParentName(), getParentSelector(), getContainerName(), getCommand(), getArgs());
  }
}
