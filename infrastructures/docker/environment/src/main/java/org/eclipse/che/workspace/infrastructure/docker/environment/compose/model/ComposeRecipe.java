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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description of docker compose services file.
 *
 * @author Alexander Garagatyi
 */
public class ComposeRecipe {
  private String version;
  private Map<String, ComposeService> services;

  public ComposeRecipe() {}

  public ComposeRecipe(ComposeRecipe environment) {
    version = environment.getVersion();
    if (environment.getServices() != null) {
      services =
          environment
              .getServices()
              .entrySet()
              .stream()
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, entry -> new ComposeService(entry.getValue())));
    }
  }

  /** Version of compose syntax. */
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public ComposeRecipe withVersion(String version) {
    this.version = version;
    return this;
  }

  /** Mapping of compose services names to services configuration. */
  public Map<String, ComposeService> getServices() {
    if (services == null) {
      services = new HashMap<>();
    }
    return services;
  }

  public void setServices(Map<String, ComposeService> services) {
    this.services = services;
  }

  public ComposeRecipe withServices(Map<String, ComposeService> services) {
    this.services = services;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ComposeRecipe)) return false;
    ComposeRecipe that = (ComposeRecipe) o;
    return Objects.equals(version, that.version) && Objects.equals(services, that.services);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, services);
  }

  @Override
  public String toString() {
    return "ComposeRecipe{" + "version='" + version + '\'' + ", services=" + services + '}';
  }
}
