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
package org.eclipse.che.infrastructure.docker.client.json.network;

import java.util.Objects;

/**
 * Represents configuration that should be passed to docker API to disconnect container from
 * network.
 *
 * @author Alexander Garagatyi
 */
public class DisconnectContainer {
  private String container;
  private boolean force;

  public String getContainer() {
    return container;
  }

  public void setContainer(String container) {
    this.container = container;
  }

  public DisconnectContainer withContainer(String container) {
    this.container = container;
    return this;
  }

  public boolean isForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  public DisconnectContainer withForce(boolean force) {
    this.force = force;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DisconnectContainer)) {
      return false;
    }
    final DisconnectContainer that = (DisconnectContainer) obj;
    return force == that.force && Objects.equals(container, that.container);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(container);
    hash = 31 * hash + Boolean.hashCode(force);
    return hash;
  }

  @Override
  public String toString() {
    return "DisconnectContainer{" + "container='" + container + '\'' + ", force=" + force + '}';
  }
}
