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

import org.eclipse.che.api.core.model.project.SourceStorage;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    @CollectionTable(name = "sourcestorage_parameters", joinColumns = @JoinColumn(name = "sourcestorage_id"))
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SourceStorageImpl)) return false;
        final SourceStorageImpl other = (SourceStorageImpl)o;
        return Objects.equals(type, other.type) &&
               Objects.equals(location, other.location) &&
               getParameters().equals(other.getParameters());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(type);
        hash = hash * 31 + Objects.hashCode(location);
        hash = hash * 31 + getParameters().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "SourceStorageImpl{" +
               "type='" + type + '\'' +
               ", location='" + location + '\'' +
               ", parameters=" + parameters +
               '}';
    }
}
