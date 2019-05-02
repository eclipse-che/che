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
 * Represents full information about plugin, including registry address and id.
 *
 * @author Max Shaposhnyk
 */
@JsonInclude(Include.NON_NULL)
public class PluginFQN {

  private URI registry;
  private String id;

  public PluginFQN(URI registry, String id) {
    this.registry = registry;
    this.id = id;
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

  @Override
  public int hashCode() {
    return Objects.hash(getRegistry(), getId());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PluginFQN)) {
      return false;
    }
    PluginFQN other = (PluginFQN) obj;
    return Objects.equals(getId(), other.getId())
        && Objects.equals(getRegistry(), other.getRegistry());
  }

  @Override
  public String toString() {
    return String.format("{id:%s, registry:%s}", this.id, this.registry);
  }
}
