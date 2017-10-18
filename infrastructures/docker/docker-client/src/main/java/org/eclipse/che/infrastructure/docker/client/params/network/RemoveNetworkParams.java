/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.params.network;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#removeNetwork(RemoveNetworkParams)}.
 *
 * @author Alexander Garagatyi
 */
public class RemoveNetworkParams {
  private String netId;

  private RemoveNetworkParams() {}

  /**
   * Creates arguments holder with required parameters.
   *
   * @param netId network identifier
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code netId} is null
   */
  public static RemoveNetworkParams create(@NotNull String netId) {
    return new RemoveNetworkParams().withNetworkId(netId);
  }

  /**
   * Adds network identifier to this parameters.
   *
   * @param netId network identifier
   * @return this params instance
   * @throws NullPointerException if {@code netId} is null
   */
  public RemoveNetworkParams withNetworkId(@NotNull String netId) {
    requireNonNull(netId);
    this.netId = netId;
    return this;
  }

  public String getNetworkId() {
    return netId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RemoveNetworkParams)) {
      return false;
    }
    final RemoveNetworkParams that = (RemoveNetworkParams) obj;
    return Objects.equals(netId, that.netId);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(netId);
    return hash;
  }
}
