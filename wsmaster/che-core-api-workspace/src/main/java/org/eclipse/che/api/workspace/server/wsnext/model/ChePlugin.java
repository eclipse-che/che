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
package org.eclipse.che.api.workspace.server.wsnext.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents Che plugin in sidecar-powered workspace. */
public class ChePlugin extends PluginBase {
  private List<EditorCompatibility> editors = new ArrayList<>();

  public ChePlugin editors(List<EditorCompatibility> editors) {
    this.editors = editors;
    return this;
  }

  public List<EditorCompatibility> getEditors() {
    return editors;
  }

  public void setEditors(List<EditorCompatibility> editors) {
    this.editors = editors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChePlugin)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ChePlugin plugin = (ChePlugin) o;
    return Objects.equals(getEditors(), plugin.getEditors());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getEditors());
  }

  @Override
  public String toString() {
    return "ChePlugin{"
        + "name='"
        + getName()
        + '\''
        + ", id='"
        + getId()
        + '\''
        + ", version='"
        + getVersion()
        + '\''
        + ", containers="
        + getContainers()
        + ", endpoints="
        + getEndpoints()
        + ", editors="
        + getEditors()
        + '}';
  }
}
