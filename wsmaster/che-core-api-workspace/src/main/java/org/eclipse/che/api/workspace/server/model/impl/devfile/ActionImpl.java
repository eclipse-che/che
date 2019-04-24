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

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.devfile.Action;

/** @author Sergii Leshchenko */
@Entity(name = "DevfileAction")
@Table(name = "devfile_action")
public class ActionImpl implements Action {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "component", nullable = false)
  private String component;

  @Column(name = "command", nullable = false)
  private String command;

  @Column(name = "workdir")
  private String workdir;

  public ActionImpl() {}

  public ActionImpl(String type, String component, String command, String workdir) {
    this.type = type;
    this.component = component;
    this.command = command;
    this.workdir = workdir;
  }

  public ActionImpl(Action action) {
    this(action.getType(), action.getComponent(), action.getCommand(), action.getWorkdir());
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getComponent() {
    return component;
  }

  public void setComponent(String component) {
    this.component = component;
  }

  @Override
  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  @Override
  public String getWorkdir() {
    return workdir;
  }

  public void setWorkdir(String workdir) {
    this.workdir = workdir;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ActionImpl)) {
      return false;
    }
    ActionImpl action = (ActionImpl) o;
    return Objects.equals(id, action.id)
        && Objects.equals(type, action.type)
        && Objects.equals(component, action.component)
        && Objects.equals(command, action.command)
        && Objects.equals(workdir, action.workdir);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, component, command, workdir);
  }

  @Override
  public String toString() {
    return "ActionImpl{"
        + "id='"
        + id
        + '\''
        + ", type='"
        + type
        + '\''
        + ", component='"
        + component
        + '\''
        + ", command='"
        + command
        + '\''
        + ", workdir='"
        + workdir
        + '\''
        + '}';
  }
}
