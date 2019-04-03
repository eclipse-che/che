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

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.devfile.Action;
import org.eclipse.che.api.core.model.workspace.devfile.Command;

/** @author Sergii Leshchenko */
public class CommandImpl implements Command {

  private String name;
  private List<ActionImpl> actions;
  private Map<String, String> attributes;

  public CommandImpl() {}

  public CommandImpl(String name, List<? extends Action> actions, Map<String, String> attributes) {
    this.name = name;
    if (actions != null) {
      this.actions = actions.stream().map(ActionImpl::new).collect(toCollection(ArrayList::new));
    }
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
  }

  public CommandImpl(Command command) {
    this(command.getName(), command.getActions(), command.getAttributes());
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public List<ActionImpl> getActions() {
    if (actions == null) {
      actions = new ArrayList<>();
    }
    return actions;
  }

  public void setActions(List<ActionImpl> actions) {
    this.actions = actions;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CommandImpl)) {
      return false;
    }
    CommandImpl command = (CommandImpl) o;
    return Objects.equals(getName(), command.getName())
        && Objects.equals(getActions(), command.getActions())
        && Objects.equals(getAttributes(), command.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getActions(), getAttributes());
  }

  @Override
  public String toString() {
    return "CommandImpl{"
        + "name='"
        + name
        + '\''
        + ", actions="
        + actions
        + ", attributes="
        + attributes
        + '}';
  }
}
