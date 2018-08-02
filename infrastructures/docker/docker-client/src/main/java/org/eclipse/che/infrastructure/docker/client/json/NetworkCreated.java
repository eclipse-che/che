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
package org.eclipse.che.infrastructure.docker.client.json;

import java.util.Objects;

/**
 * Represents response from docker API on successful creation of network.
 *
 * @author Alexander Garagatyi
 */
public class NetworkCreated {
  private String id;
  private String warning;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public NetworkCreated withId(String id) {
    this.id = id;
    return this;
  }

  public String getWarning() {
    return warning;
  }

  public void setWarning(String warning) {
    this.warning = warning;
  }

  public NetworkCreated withWarning(String warning) {
    this.warning = warning;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NetworkCreated)) {
      return false;
    }
    final NetworkCreated that = (NetworkCreated) obj;
    return Objects.equals(id, that.id) && Objects.equals(warning, that.warning);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(warning);
    return hash;
  }

  @Override
  public String toString() {
    return "NetworkCreated{" + "id='" + id + '\'' + ", warning='" + warning + '\'' + '}';
  }
}
