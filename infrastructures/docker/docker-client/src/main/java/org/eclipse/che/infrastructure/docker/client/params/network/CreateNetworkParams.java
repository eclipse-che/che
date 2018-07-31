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
package org.eclipse.che.infrastructure.docker.client.params.network;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.network.NewNetwork;

/**
 * Arguments holder for {@link DockerConnector#createNetwork(CreateNetworkParams)}.
 *
 * @author Alexander Garagatyi
 */
public class CreateNetworkParams {
  // todo consider validation that network config has all required fields
  private NewNetwork network;

  private CreateNetworkParams() {}

  /**
   * Creates arguments holder with required parameters.
   *
   * @param newNetwork network configuration
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code newNetwork} is null
   */
  public static CreateNetworkParams create(@NotNull NewNetwork newNetwork) {
    return new CreateNetworkParams().withNetwork(newNetwork);
  }

  /**
   * Adds network name to this parameters.
   *
   * @param newNetwork network configuration
   * @return this params instance
   * @throws NullPointerException if {@code newNetwork} is null
   */
  public CreateNetworkParams withNetwork(@NotNull NewNetwork newNetwork) {
    requireNonNull(newNetwork);
    this.network = newNetwork;
    return this;
  }

  public NewNetwork getNetwork() {
    return network;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CreateNetworkParams)) {
      return false;
    }
    final CreateNetworkParams that = (CreateNetworkParams) obj;
    return Objects.equals(network, that.network);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(network);
    return hash;
  }

  @Override
  public String toString() {
    return "CreateNetworkParams{" + "network=" + network + '}';
  }
}
