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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.acl.AclEntry;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Sergii Leschenko
 */
@Entity(name = "Acl")
public class AclEntryImpl implements AclEntry {

    @Id
    @GeneratedValue
    private String id;

    @Column
    private String user;

    @ManyToOne
    @JoinColumn(insertable = false,
                updatable = false,
                name = "user",
                referencedColumnName = "id")
    private UserImpl userEntity;

    @ElementCollection
    private List<String> actions;

    public AclEntryImpl() {}

    public AclEntryImpl(String user, List<String> actions) {
        checkArgument(actions != null && !actions.isEmpty(), "Required at least one action");
        this.user = user;
        this.actions = new ArrayList<>(actions);
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public List<String> getActions() {
        if (actions == null) {
            return new ArrayList<>();
        }
        return actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AclEntryImpl)) {
            return false;
        }
        final AclEntryImpl other = (AclEntryImpl)obj;
        return Objects.equals(user, other.user)
               && actions.equals(other.actions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(user);
        hash = 31 * hash + actions.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "AclEntryImpl{" +
               "user='" + user + "'" +
               ", actions=" + actions +
               '}';
    }
}
