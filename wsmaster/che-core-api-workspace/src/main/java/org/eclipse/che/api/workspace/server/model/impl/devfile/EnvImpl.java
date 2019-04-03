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
import org.eclipse.che.api.core.model.workspace.devfile.Env;

/** @author Sergii Leshchenko */
public class EnvImpl implements Env {

  private String name;
  private String value;

  public EnvImpl() {}

  public EnvImpl(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public EnvImpl(Env env) {
    this(env.getName(), env.getValue());
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EnvImpl)) {
      return false;
    }
    EnvImpl env = (EnvImpl) o;
    return Objects.equals(getName(), env.getName()) && Objects.equals(getValue(), env.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getValue());
  }

  @Override
  public String toString() {
    return "EnvImpl{" + "name='" + name + '\'' + ", value='" + value + '\'' + '}';
  }
}
