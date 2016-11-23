/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.model.impl;


import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;

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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

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
    @CollectionTable(name = "projectconfig_mixins", joinColumns = @JoinColumn(name = "projectconfig_id"))
    @Column(name = "mixins")
    private List<String> mixins;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "dbattributes_id")
    @MapKey(name = "name")
    private Map<String, Attribute> dbAttributes;

    // Mapping delegated to 'dbAttributes' field
    // as it is impossible to map nested list directly
    @Transient
    private Map<String, List<String>> attributes;

    public ProjectConfigImpl() {}

    public ProjectConfigImpl(ProjectConfig projectConfig) {
        name = projectConfig.getName();
        path = projectConfig.getPath();
        description = projectConfig.getDescription();
        type = projectConfig.getType();
        mixins = new ArrayList<>(projectConfig.getMixins());
        attributes = projectConfig.getAttributes()
                                  .entrySet()
                                  .stream()
                                  .collect(toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));

        SourceStorage sourceStorage = projectConfig.getSource();

        if (sourceStorage != null) {
            source = new SourceStorageImpl(sourceStorage.getType(), sourceStorage.getLocation(), sourceStorage.getParameters());
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

    public void setSource(SourceStorageImpl sourceStorage) {
        this.source = sourceStorage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectConfigImpl)) return false;
        final ProjectConfigImpl other = (ProjectConfigImpl)o;
        return Objects.equals(name, other.name)
               && Objects.equals(path, other.path)
               && Objects.equals(description, other.description)
               && Objects.equals(type, other.type)
               && getMixins().equals(other.getMixins())
               && getAttributes().equals(other.getAttributes())
               && Objects.equals(source, other.getSource());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(name);
        hash = hash * 31 + Objects.hashCode(path);
        hash = hash * 31 + Objects.hashCode(description);
        hash = hash * 31 + Objects.hashCode(type);
        hash = hash * 31 + getMixins().hashCode();
        hash = hash * 31 + getAttributes().hashCode();
        hash = hash * 31 + Objects.hashCode(source);
        return hash;
    }

    @Override
    public String toString() {
        return "ProjectConfigImpl{" +
               "name='" + name + '\'' +
               ", path='" + path + '\'' +
               ", description='" + description + '\'' +
               ", type='" + type + '\'' +
               ", mixins=" + mixins +
               ", attributes=" + attributes +
               ", source=" + source +
               '}';
    }

    /**
     * Synchronizes instance attributes with db attributes,
     * should be called by internal components in needed places,
     * this can't be done neither by {@link PrePersist} nor by {@link PreUpdate}
     * as when the entity is merged the transient attribute won't be passed
     * to event handlers.
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
    private void postLoadAttributes() {
        attributes = dbAttributes.values()
                                 .stream()
                                 .collect(toMap(attr -> attr.name, attr -> attr.values));
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
        @CollectionTable(name = "projectattribute_values", joinColumns = @JoinColumn(name = "projectattribute_id"))
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
            final Attribute that = (Attribute)obj;
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
            return "Attribute{" +
                   "values=" + values +
                   ", name='" + name + '\'' +
                   ", id=" + id +
                   '}';
        }
    }
}
