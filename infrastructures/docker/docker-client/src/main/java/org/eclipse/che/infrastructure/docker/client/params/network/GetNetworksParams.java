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

import java.util.Objects;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.Filters;

/**
 * Arguments holder for {@link DockerConnector#getNetworks(GetNetworksParams)}.
 *
 * @author Alexander Garagatyi
 */
public class GetNetworksParams {

  private Filters filters;

  /** Creates arguments holder. */
  public static GetNetworksParams create() {
    return new GetNetworksParams();
  }

  private GetNetworksParams() {}

  /**
   * Adds filters to this parameters.
   *
   * @param filters filter of needed networks. Available filters: {@code name=<string>}, {@code
   *     id=<string>}, {@code type=<string>}
   * @return this params instance
   */
  public GetNetworksParams withFilters(Filters filters) {
    this.filters = filters;
    return this;
  }

  public Filters getFilters() {
    return filters;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GetNetworksParams)) {
      return false;
    }
    final GetNetworksParams that = (GetNetworksParams) obj;
    return Objects.equals(filters, that.filters);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(filters);
    return hash;
  }

  @Override
  public String toString() {
    return "GetNetworksParams{" + "filters=" + filters + '}';
  }
}
