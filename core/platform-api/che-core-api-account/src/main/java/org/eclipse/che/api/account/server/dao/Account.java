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
package org.eclipse.che.api.account.server.dao;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Defines account data object.
 *
 * @author Yevhenii Voevodin
 */
public class Account {

    private final String id;

    private String              name;
    private List<Workspace>     workspaces;
    private Map<String, String> attributes;

    public Account(String id) {
        this.id = id;
    }

    public Account(Account account) {
        this(account.id,
             account.name,
             account.workspaces,
             account.attributes);
    }

    public Account(String id, String name) {
        this(id, name, null, null);
    }

    public Account(String id,
                   String name,
                   @Nullable List<Workspace> workspaces,
                   @Nullable Map<String, String> attributes) {
        this(id);
        this.name = requireNonNull(name, "Required non-null account name");
        this.workspaces = workspaces;
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Workspace> getWorkspaces() {
        if (workspaces == null) {
            workspaces = new ArrayList<>();
        }
        return workspaces;
    }

    public void setWorkspaces(@Nullable List<Workspace> workspaces) {
        this.workspaces = workspaces;
    }

    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    public void setAttributes(@Nullable Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Account)) {
            return false;
        }
        final Account other = (Account)obj;
        return Objects.equals(id, other.id)
               && Objects.equals(name, other.name)
               && getAttributes().equals(other.getAttributes())
               && getWorkspaces().equals(other.getWorkspaces());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + getAttributes().hashCode();
        hash = 31 * hash + getWorkspaces().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "Account{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", workspaces=" + workspaces +
               ", attributes=" + attributes +
               '}';
    }
}
