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
package org.eclipse.che.commons.user;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base implementation of User interface.
 *
 * @author andrew00x
 */
public class UserImpl implements User {
    private final String      name;
    private final Set<String> roles;
    private final String      token;
    private final String      id;
    private final boolean     isTemporary;

    public UserImpl(String name, String id, String token, Collection<String> roles, boolean isTemporary) {
        this.name = name;
        this.id = id;
        this.token = token;
        this.isTemporary = isTemporary;
        this.roles = roles == null ? Collections.<String>emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(roles));
    }

    @Deprecated
    public UserImpl(String name, String id, String token, Collection<String> roles) {
        this(name, id, token, roles, false);
    }

    @Deprecated
    public UserImpl(String name, String token, Collection<String> roles) {
        this(name, null, token, roles);
    }

    @Deprecated
    public UserImpl(String name) {
        this(name, null, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isMemberOf(String role) {
        return roles.contains(role);
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isTemporary() {
        return isTemporary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserImpl user = (UserImpl)o;

        if (isTemporary != user.isTemporary) return false;
        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (roles != null ? !roles.equals(user.roles) : user.roles != null) return false;
        if (token != null ? !token.equals(user.token) : user.token != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (isTemporary ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserImpl{");
        sb.append("name='").append(name).append('\'');
        sb.append(", roles=").append(roles);
        sb.append(", token='").append(token).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", isTemporary=").append(isTemporary);
        sb.append('}');
        return sb.toString();
    }
}
