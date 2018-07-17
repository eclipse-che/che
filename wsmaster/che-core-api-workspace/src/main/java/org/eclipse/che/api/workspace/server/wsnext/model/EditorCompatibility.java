/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.wsnext.model;

import java.util.List;
import java.util.Objects;

/** Specifies compatibility with a specific Che editor and plugins needed for the compatibility. */
public class EditorCompatibility {
  private String name;
  private List<String> plugins;

  public String getName() {
    return name;
  }

  public EditorCompatibility name(String name) {
    this.name = name;
    return this;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EditorCompatibility plugins(List<String> plugins) {
    this.plugins = plugins;
    return this;
  }

  public List<String> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<String> plugins) {
    this.plugins = plugins;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EditorCompatibility)) {
      return false;
    }
    EditorCompatibility that = (EditorCompatibility) o;
    return Objects.equals(getName(), that.getName())
        && Objects.equals(getPlugins(), that.getPlugins());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getPlugins());
  }

  @Override
  public String toString() {
    return "EditorCompatibility{" + "name='" + name + '\'' + ", plugins=" + plugins + '}';
  }
}
