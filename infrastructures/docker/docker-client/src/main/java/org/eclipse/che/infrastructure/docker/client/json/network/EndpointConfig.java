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
package org.eclipse.che.infrastructure.docker.client.json.network;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents description of network needed to connect container into.
 *
 * @author Alexander Garagatyi
 */
public class EndpointConfig {
  private NewIpamConfig iPAMConfig;
  private String[] links;
  private String[] aliases;

  public NewIpamConfig getIPAMConfig() {
    return iPAMConfig;
  }

  public void setIPAMConfig(NewIpamConfig iPAMConfig) {
    this.iPAMConfig = iPAMConfig;
  }

  public EndpointConfig withIPAMConfig(NewIpamConfig iPAMConfig) {
    this.iPAMConfig = iPAMConfig;
    return this;
  }

  public String[] getLinks() {
    return links;
  }

  public void setLinks(String[] links) {
    this.links = links;
  }

  public EndpointConfig withLinks(String[] links) {
    this.links = links;
    return this;
  }

  public String[] getAliases() {
    return aliases;
  }

  public void setAliases(String... aliases) {
    this.aliases = aliases;
  }

  public EndpointConfig withAliases(String... aliases) {
    this.aliases = aliases;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EndpointConfig)) {
      return false;
    }
    final EndpointConfig that = (EndpointConfig) obj;
    return Objects.equals(iPAMConfig, that.iPAMConfig)
        && Arrays.equals(links, that.links)
        && Arrays.equals(aliases, that.aliases);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(iPAMConfig);
    hash = 31 * hash + Arrays.hashCode(links);
    hash = 31 * hash + Arrays.hashCode(aliases);
    return hash;
  }

  @Override
  public String toString() {
    return "EndpointConfig{"
        + "iPAMConfig="
        + iPAMConfig
        + ", links="
        + Arrays.toString(links)
        + ", aliases="
        + Arrays.toString(aliases)
        + '}';
  }
}
