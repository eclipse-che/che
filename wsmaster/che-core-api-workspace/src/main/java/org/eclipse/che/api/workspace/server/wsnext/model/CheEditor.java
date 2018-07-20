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
