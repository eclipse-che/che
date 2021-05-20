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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import java.util.Objects;

/**
 * Represents extended information about plugin identification.
 *
 * @author Oleksandr Garagatyi
 * @see PluginFQN
 */
public class ExtendedPluginFQN extends PluginFQN {

  private String name;
  private String version;
  private String publisher;

  public ExtendedPluginFQN(URI registry, String id, String publisher, String name, String version) {
    super(registry, id);
    this.publisher = publisher;
    this.name = name;
    this.version = version;
  }

  /** In this constructor, id is composed from given params */
  public ExtendedPluginFQN(String reference, String publisher, String name, String version) {
    super(reference, publisher + "/" + name + "/" + version);
    this.publisher = publisher;
    this.name = name;
    this.version = version;
  }

  public ExtendedPluginFQN(String reference) {
    super(reference);
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @JsonIgnore
  public String getPublisherAndName() {
    return publisher + "/" + name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExtendedPluginFQN)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ExtendedPluginFQN that = (ExtendedPluginFQN) o;
    return Objects.equals(getName(), that.getName())
        && Objects.equals(getVersion(), that.getVersion())
        && Objects.equals(getPublisher(), that.getPublisher());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getName(), getVersion(), getPublisher());
  }

  @Override
  public String toString() {
    return String.format(
        "{id:%s, registry:%s, publisher:%s, name:%s, version:%s, reference:%s}",
        getId(), getRegistry(), getPublisher(), getName(), getVersion(), getReference());
  }
}
