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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Eugene Voevodin
 */
public class Member {

    private String       userId;
    private String       accountId;
    private List<String> roles;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Member withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Member withAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public List<String> getRoles() {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Member withRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Member)) {
            return false;
        }
        final Member other = (Member)obj;
        return Objects.equals(userId, other.userId) &&
               Objects.equals(accountId, other.accountId) &&
               Objects.equals(getRoles(), other.getRoles());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(userId);
        hash = 31 * hash + Objects.hashCode(accountId);
        hash = 31 * hash + Objects.hashCode(getRoles());
        return hash;
    }
}
