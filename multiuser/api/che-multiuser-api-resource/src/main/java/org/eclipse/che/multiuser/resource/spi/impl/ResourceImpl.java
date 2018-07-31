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
package org.eclipse.che.multiuser.resource.spi.impl;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.eclipse.che.multiuser.resource.model.Resource;

/** @author Sergii Leschenko */
@Entity(name = "Resource")
@Table(name = "che_resource")
public class ResourceImpl implements Resource {
  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "amount", nullable = false)
  private long amount;

  @Column(name = "unit", nullable = false)
  private String unit;

  public ResourceImpl() {}

  public ResourceImpl(String type, long amount, String unit) {
    this.amount = amount;
    this.type = type;
    this.unit = unit;
  }

  public ResourceImpl(Resource resource) {
    this(resource.getType(), resource.getAmount(), resource.getUnit());
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public long getAmount() {
    return amount;
  }

  @Override
  public String getUnit() {
    return unit;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ResourceImpl)) {
      return false;
    }
    final ResourceImpl that = (ResourceImpl) obj;
    return amount == that.amount
        && Objects.equals(id, that.id)
        && Objects.equals(type, that.type)
        && Objects.equals(unit, that.unit);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(type);
    hash = 31 * hash + Long.hashCode(amount);
    hash = 31 * hash + Objects.hashCode(unit);
    return hash;
  }

  @Override
  public String toString() {
    return "ResourceImpl{"
        + "id="
        + id
        + ", type='"
        + type
        + '\''
        + ", amount="
        + amount
        + ", unit='"
        + unit
        + '\''
        + '}';
  }
}
