/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.factory.Action;

/**
 * Data object for {@link Action}.
 *
 * @author Anton Korneta
 */
@Entity(name = "Action")
@Table(name = "che_factory_action")
public class ActionImpl implements Action {

  @Id
  @GeneratedValue
  @Column(name = "entity_id")
  private Long entityId;

  @Column(name = "id")
  private String id;

  @ElementCollection
  @CollectionTable(
      name = "che_factory_action_properties",
      joinColumns = @JoinColumn(name = "action_entity_id"))
  @MapKeyColumn(name = "property_key")
  @Column(name = "property_value")
  private Map<String, String> properties;

  public ActionImpl() {}

  public ActionImpl(String id, Map<String, String> properties) {
    this.id = id;
    if (properties != null) {
      this.properties = new HashMap<>(properties);
    }
  }

  public ActionImpl(Action action) {
    this(action.getId(), action.getProperties());
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Map<String, String> getProperties() {
    if (properties == null) {
      properties = new HashMap<>();
    }
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ActionImpl)) {
      return false;
    }
    final ActionImpl that = (ActionImpl) obj;
    return Objects.equals(entityId, that.entityId)
        && Objects.equals(id, that.id)
        && getProperties().equals(that.getProperties());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(entityId);
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + getProperties().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "ActionImpl{"
        + "entityId="
        + entityId
        + ", id='"
        + id
        + '\''
        + ", properties="
        + properties
        + '}';
  }
}
