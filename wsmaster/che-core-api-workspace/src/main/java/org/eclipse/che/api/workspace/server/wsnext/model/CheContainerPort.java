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

import java.util.Objects;

public class CheContainerPort {
  public int exposedPort = 0;

  public CheContainerPort exposedPort(int exposedPort) {
    this.exposedPort = exposedPort;
    return this;
  }

  public int getExposedPort() {
    return exposedPort;
  }

  public void setExposedPort(int exposedPort) {
    this.exposedPort = exposedPort;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CheContainerPort)) {
      return false;
    }
    CheContainerPort that = (CheContainerPort) o;
    return Objects.equals(getExposedPort(), that.getExposedPort());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getExposedPort());
  }

  @Override
  public String toString() {
    return "CheContainerPort{" + "exposedPort='" + exposedPort + '\'' + '}';
  }
}
