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
