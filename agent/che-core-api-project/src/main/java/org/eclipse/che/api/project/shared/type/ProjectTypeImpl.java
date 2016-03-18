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
package org.eclipse.che.api.project.shared.type;

import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.ProjectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Immutable implementation of Project Type interface
 * intended to use in client side
 *
 * @author gazarenkov
 */
public class ProjectTypeImpl implements ProjectType {


    protected final String id;
    protected final   boolean                persisted;
    protected final   boolean                mixable;
    protected final   boolean                primaryable;
    protected final String displayName;
    protected final Map<String, Attribute> attributes;
    protected final List<String> parents;
    protected final List<String> ancestors = new ArrayList<>();

    public ProjectTypeImpl(String id, boolean persisted, boolean mixable, boolean primaryable,
                           String displayName, Map<String, Attribute> attributes, List<String> parents) {
        this.id = id;
        this.persisted = persisted;
        this.mixable = mixable;
        this.primaryable = primaryable;
        this.displayName = displayName;
        this.attributes = attributes;
        this.parents = parents;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isPersisted() {
        return persisted;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public List<Attribute> getAttributes() {
        return new ArrayList<>(attributes.values());
    }

    @Override
    public List<String> getParents() {
        return parents;
    }

    @Override
    public boolean isMixable() {
        return mixable;
    }

    @Override
    public boolean isPrimaryable() {
        return primaryable;
    }


    /**
     * @return ids of ancestors
     */
    public List<String> getAncestors() {
        return ancestors;
    }

    /**
     * whether this type is subtype of typeId
     * @param typeId
     * @return true if it is a subtype
     */
    public boolean isTypeOf(String typeId) {

        return this.id.equals(typeId) || ancestors.contains(typeId);
    }

    /**
     * @param name
     * @return attribute by name
     */
    public Attribute getAttribute(String name) {
        return attributes.get(name);
    }



}
