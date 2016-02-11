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
package org.eclipse.che.api.machine.server.recipe;

import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.Permissions;
import org.eclipse.che.api.machine.shared.dto.recipe.GroupDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.PermissionsDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eugene Voevodin
 */
public class PermissionsImpl implements Permissions {

    public static PermissionsImpl fromDescriptor(PermissionsDescriptor descriptor) {
        final ArrayList<Group> groups = new ArrayList<>(descriptor.getGroups().size());
        for (GroupDescriptor groupDescriptor : descriptor.getGroups()) {
            groups.add(GroupImpl.fromDescriptor(groupDescriptor));
        }
        return new PermissionsImpl(descriptor.getUsers(), groups);
    }

    private Map<String, List<String>> users;
    private List<Group>               groups;

    public PermissionsImpl(Map<String, List<String>> users, List<Group> groups) {
        this.users = users;
        this.groups = groups;
    }

    @Override
    public Map<String, List<String>> getUsers() {
        if (users == null) {
            users = new HashMap<>();
        }
        return users;
    }

    public void setUsers(Map<String, List<String>> users) {
        this.users = users;
    }

    public PermissionsImpl withUsers(Map<String, List<String>> users) {
        this.users = users;
        return this;
    }

    @Override
    public List<Group> getGroups() {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public PermissionsImpl withGroups(List<Group> groups) {
        this.groups = groups;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PermissionsImpl)) {
            return false;
        }
        final PermissionsImpl other = (PermissionsImpl)obj;
        return getGroups().equals(other.getGroups()) && getUsers().equals(other.getUsers());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getGroups().hashCode();
        hash = 31 * hash + getUsers().hashCode();
        return hash;
    }
}
