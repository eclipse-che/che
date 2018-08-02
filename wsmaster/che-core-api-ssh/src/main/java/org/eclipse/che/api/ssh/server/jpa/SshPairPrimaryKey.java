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
package org.eclipse.che.api.ssh.server.jpa;

import java.io.Serializable;
import java.util.Objects;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;

/**
 * Primary key for {@link SshPairImpl} entity
 *
 * @author Mihail Kuznyetsov
 */
public class SshPairPrimaryKey implements Serializable {
  private String owner;
  private String service;
  private String name;

  public SshPairPrimaryKey() {}

  public SshPairPrimaryKey(String owner, String service, String name) {
    this.owner = owner;
    this.service = service;
    this.name = name;
  }

  public String getOwner() {
    return owner;
  }

  public String getService() {
    return service;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof SshPairPrimaryKey)) return false;
    final SshPairPrimaryKey other = (SshPairPrimaryKey) obj;
    return Objects.equals(owner, other.owner)
        && Objects.equals(service, other.service)
        && Objects.equals(name, other.name);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(owner);
    hash = 31 * hash + Objects.hashCode(service);
    hash = 31 * hash + Objects.hashCode(name);
    return hash;
  }
}
