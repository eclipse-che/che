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
package org.eclipse.che.api.workspace.server.wsplugins.model;

import java.util.List;
import java.util.Objects;

/** Specifies compatibility with a specific Che editor and plugins needed for the compatibility. */
public class EditorCompatibility {
  private String id;
  private List<String> plugins;

  public String getId() {
    return id;
  }

  public EditorCompatibility id(String id) {
    this.id = id;
    return this;
  }

  public void setId(String id) {
    this.id = id;
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
    return Objects.equals(getId(), that.getId()) && Objects.equals(getPlugins(), that.getPlugins());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getPlugins());
  }

  @Override
  public String toString() {
    return "EditorCompatibility{" + "id='" + id + '\'' + ", plugins=" + plugins + '}';
  }
}
