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

/**
 * Represents an editor inside of Che workspace.
 *
 * <p>It may be classic GWT IDE, Eclipse Theia or something else.
 */
public class CheEditor extends PluginBase {
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CheEditor)) {
      return false;
    }
    CheEditor cheEditor = (CheEditor) o;
    return super.equals(cheEditor);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return "CheEditor{"
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
        + '}';
  }
}
