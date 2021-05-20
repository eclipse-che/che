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
package org.eclipse.che.api.workspace.server.model.impl;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;

/**
 * Data object for {@link SourceStorage}.
 *
 * @author Yevhenii Voevodin
 */
@Entity(name = "SourceStorage")
@Table(name = "sourcestorage")
public class SourceStorageImpl implements SourceStorage {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "type")
  private String type;

  @Column(name = "location", columnDefinition = "TEXT")
  private String location;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "sourcestorage_parameters",
      joinColumns = @JoinColumn(name = "sourcestorage_id"))
  @MapKeyColumn(name = "parameters_key")
  @Column(name = "parameters")
  private Map<String, String> parameters;

  public SourceStorageImpl() {}

  public SourceStorageImpl(String type, String location, Map<String, String> parameters) {
    this.type = type;
    this.location = location;
    if (parameters != null) {
      this.parameters = new HashMap<>(parameters);
    }
  }

  @PrePersist
  @PreUpdate
  public void validate() {
    if (parameters != null) {
      for (Map.Entry<String, String> e : parameters.entrySet()) {
        if (e.getValue() == null) {
          throw new IllegalStateException(
              format(
                  "Parameter '%s' of the source %s is null. This is illegal.", e.getKey(), this));
        }
      }
    }
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public Map<String, String> getParameters() {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SourceStorageImpl)) {
      return false;
    }
    final SourceStorageImpl that = (SourceStorageImpl) obj;
    return Objects.equals(id, that.id)
        && Objects.equals(type, that.type)
        && Objects.equals(location, that.location)
        && getParameters().equals(that.getParameters());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(type);
    hash = 31 * hash + Objects.hashCode(location);
    hash = 31 * hash + getParameters().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "SourceStorageImpl{"
        + "id="
        + id
        + ", type='"
        + type
        + '\''
        + ", location='"
        + location
        + '\''
        + ", parameters="
        + parameters
        + '}';
  }
}
