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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//TODO move?

/**
 * Data object for {@link SourceStorage}.
 *
 * @author Eugene Voevodin
 */
public class SourceStorageImpl implements SourceStorage {

    private String              type;
    private String              location;
    private Map<String, String> parameters;

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

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public Map<String, String> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        return parameters;
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
