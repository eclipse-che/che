/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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
 * Represents full information about plugin, including registry address and id, or direct reference
 * to plugin descriptor. When {@link PluginFQN#reference} and {@link PluginFQN#id} are set
 * simultaneously, {@link PluginFQN#reference} should take precedence.
 *
 * @author Max Shaposhnyk
 */
@JsonInclude(Include.NON_NULL)
public class PluginFQN {

  private URI registry;
  private String id;
  private String reference;

  public PluginFQN(URI registry, String id) {
    this.registry = registry;
    this.id = id;
  }

  protected PluginFQN(String reference, String id) {
    this.reference = reference;
    this.id = id;
  }

  public PluginFQN(String reference) {
    this.reference = reference;
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

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRegistry(), getId(), getReference());
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
        && Objects.equals(getRegistry(), other.getRegistry())
        && Objects.equals(getReference(), other.getReference());
  }

  @Override
  public String toString() {
    return String.format(
        "{id:%s, registry:%s, reference:%s}", this.id, this.registry, this.reference);
  }
}
