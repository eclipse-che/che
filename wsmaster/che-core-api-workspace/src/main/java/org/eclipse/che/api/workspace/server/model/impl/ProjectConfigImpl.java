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
package org.eclipse.che.api.workspace.server.model.impl;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.workspace.shared.ProjectProblemImpl;

/**
 * Data object for {@link ProjectConfig}.
 *
 * @author Eugene Voevodin
 * @author Dmitry Shnurenko
 */
@Entity(name = "ProjectConfig")
@Table(name = "projectconfig")
public class ProjectConfigImpl implements ProjectConfig {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "name")
  private String name;

  @Column(name = "type")
  private String type;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "source_id")
  private SourceStorageImpl source;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "projectconfig_mixins",
    joinColumns = @JoinColumn(name = "projectconfig_id")
  )
  @Column(name = "mixins")
  private List<String> mixins;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "dbattributes_id")
  @MapKey(name = "name")
  private Map<String, Attribute> dbAttributes;

  // Mapping delegated to 'dbAttributes' field
  // as it is impossible to map nested list directly
  @Transient private Map<String, List<String>> attributes;

  @Transient private List<ProjectProblemImpl> problems;

  public ProjectConfigImpl() {}

  public ProjectConfigImpl(ProjectConfig projectConfig) {
    name = projectConfig.getName();
    path = projectConfig.getPath();
    description = projectConfig.getDescription();
    type = projectConfig.getType();
    mixins = new ArrayList<>(projectConfig.getMixins());
    attributes =
        projectConfig
            .getAttributes()
            .entrySet()
            .stream()
            .collect(toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));

    SourceStorage sourceStorage = projectConfig.getSource();

    if (sourceStorage != null) {
      source =
          new SourceStorageImpl(
              sourceStorage.getType(), sourceStorage.getLocation(), sourceStorage.getParameters());
    }
    if (projectConfig.getProblems() != null) {
      problems =
          projectConfig
              .getProblems()
              .stream()
              .map(problem -> new ProjectProblemImpl(problem.getCode(), problem.getMessage()))
              .collect(Collectors.toList());
    } else {
      problems = Collections.emptyList();
    }
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public List<String> getMixins() {
    if (mixins == null) {
      mixins = new ArrayList<>();
    }
    return mixins;
  }

  public void setMixins(List<String> mixins) {
    this.mixins = mixins;
  }

  @Override
  public Map<String, List<String>> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public void setAttributes(Map<String, List<String>> attributes) {
    this.attributes = attributes;
  }

  @Override
  public SourceStorageImpl getSource() {
    return source;
  }

  @Override
  public List<ProjectProblemImpl> getProblems() {
    return problems;
  }

  public void setSource(SourceStorageImpl sourceStorage) {
    this.source = sourceStorage;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ProjectConfigImpl)) {
      return false;
    }
    final ProjectConfigImpl that = (ProjectConfigImpl) obj;
    return Objects.equals(id, that.id)
        && Objects.equals(path, that.path)
        && Objects.equals(name, that.name)
        && Objects.equals(type, that.type)
        && Objects.equals(description, that.description)
        && Objects.equals(source, that.source)
        && getMixins().equals(that.getMixins())
        && getAttributes().equals(that.getAttributes());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(path);
    hash = 31 * hash + Objects.hashCode(name);
    hash = 31 * hash + Objects.hashCode(type);
    hash = 31 * hash + Objects.hashCode(description);
    hash = 31 * hash + Objects.hashCode(source);
    hash = 31 * hash + getMixins().hashCode();
    hash = 31 * hash + getAttributes().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "ProjectConfigImpl{"
        + "id="
        + id
        + ", path='"
        + path
        + '\''
        + ", name='"
        + name
        + '\''
        + ", type='"
        + type
        + '\''
        + ", description='"
        + description
        + '\''
        + ", source="
        + source
        + ", mixins="
        + mixins
        + ", attributes="
        + attributes
        + '}';
  }

  /**
   * Synchronizes instance attributes with db attributes, should be called by internal components in
   * needed places, this can't be done neither by {@link PrePersist} nor by {@link PreUpdate} as
   * when the entity is merged the transient attribute won't be passed to event handlers.
   */
  public void prePersistAttributes() {
    if (dbAttributes == null) {
      dbAttributes = new HashMap<>();
    }
    final Map<String, Attribute> dbAttrsCopy = new HashMap<>(dbAttributes);
    dbAttributes.clear();
    for (Map.Entry<String, List<String>> entry : getAttributes().entrySet()) {
      Attribute attribute = dbAttrsCopy.get(entry.getKey());
      if (attribute == null) {
        attribute = new Attribute(entry.getKey(), entry.getValue());
      } else if (!Objects.equals(attribute.values, entry.getValue())) {
        attribute.values = entry.getValue();
      }
      dbAttributes.put(entry.getKey(), attribute);
    }
  }

  @PostLoad
  @PostUpdate
  @PostPersist
  private void postLoadAttributes() {
    if (dbAttributes != null) {
      attributes =
          dbAttributes.values().stream().collect(toMap(attr -> attr.name, attr -> attr.values));
    }
  }

  @Entity(name = "ProjectAttribute")
  @Table(name = "projectattribute")
  private static class Attribute {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
      name = "projectattribute_values",
      joinColumns = @JoinColumn(name = "projectattribute_id")
    )
    @Column(name = "values")
    private List<String> values;

    public Attribute() {}

    public Attribute(String name, List<String> values) {
      this.name = name;
      this.values = values;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Attribute)) {
        return false;
      }
      final Attribute that = (Attribute) obj;
      return Objects.equals(id, that.id)
          && Objects.equals(name, that.name)
          && values.equals(that.values);
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 31 * hash + Objects.hashCode(id);
      hash = 31 * hash + Objects.hashCode(name);
      hash = 31 * hash + values.hashCode();
      return hash;
    }

    @Override
    public String toString() {
      return "Attribute{" + "values=" + values + ", name='" + name + '\'' + ", id=" + id + '}';
    }
  }
}
