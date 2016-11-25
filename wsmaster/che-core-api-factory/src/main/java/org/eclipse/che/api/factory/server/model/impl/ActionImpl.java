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
package org.eclipse.che.api.factory.server.model.impl;

import org.eclipse.che.api.core.model.factory.Action;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data object for {@link Action}.
 *
 * @author Anton Korneta
 */
@Entity(name = "Action")
@Table(name = "action")
public class ActionImpl implements Action {

    @Id
    @GeneratedValue
    @Column(name = "entityid")
    private Long entityId;

    @Column(name = "id")
    private String id;

    @ElementCollection
    @CollectionTable(name = "action_properties", joinColumns = @JoinColumn(name = "action_entityid"))
    @MapKeyColumn(name = "properties_key")
    @Column(name = "properties")
    private Map<String, String> properties;

    public ActionImpl() {}

    public ActionImpl(String id, Map<String, String> properties) {
        this.id = id;
        this.properties = properties;
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
            return new HashMap<>();
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ActionImpl)) return false;
        final ActionImpl other = (ActionImpl)obj;
        return Objects.equals(id, other.getId())
               && getProperties().equals(other.getProperties());
    }

    @Override
    public int hashCode() {
        int result = 7;
        result = 31 * result + Objects.hashCode(id);
        result = 31 * result + getProperties().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ActionImpl{" +
               "id='" + id + '\'' +
               ", properties=" + properties +
               '}';
    }
}
