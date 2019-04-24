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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.net.URI;
import java.util.Objects;

/**
 * Represents full information about plugin, including registry address, id and version.
 *
 * @author Max Shaposhnyk
 */
@JsonInclude(Include.NON_NULL)
public class PluginFQN {

  private URI registry;
  private String id;
  private String version;

  public PluginFQN(URI registry, String id, String version) {
    this.registry = registry;
    this.id = id;
    this.version = version;
  }

  public URI getRegistry() {
    return registry;
  }

  public void setRegistry(URI registry) {
    this.registry = registry;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRegistry(), getId(), getVersion());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    PluginFQN other = (PluginFQN) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(version, other.version)
        && Objects.equals(registry, other.registry);
  }

  @Override
  public String toString() {
    return String.format("{id:%s, version:%s, registry:%s}", this.id, this.version, this.registry);
  }
}
