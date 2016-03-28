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
package org.eclipse.che.api.user.server.dao;

import org.eclipse.che.api.user.shared.model.Membership;

import java.util.List;
import java.util.Map;

/**
 * Immutable data object for storing "scoped roles" such as workspace/developer
 *
 * @author gazarenkov
 */
public final class MembershipDo implements Membership {

    private final String scope;
    private final List<String> roles;
    private final String userId;
    private final String userName;
    private final String subjectId;
    private final Map <String, String> subjectProperties;

    public MembershipDo(String scope, List<String> roles, String userId, String userName, String subjectId,
                        Map<String, String> subjectProperties) {
        this.scope = scope;
        this.roles = roles;
        this.userId = userId;
        this.userName = userName;
        this.subjectId = subjectId;
        this.subjectProperties = subjectProperties;
    }

    /**
     * Scope of the role, e.g "workspace"
     * @return scope
     */
    @Override
    public String getScope() {
        return scope;
    }

    /**
     * Name of the role, e.g "developer"
     * @return name
     */
    @Override
    public List<String> getRoles() {
        return roles;
    }

    /**
     * user ID
     * @return userId
     */
    @Override
    public String getUserId() {
        return userId;
    }

    /**
     * user name
     * @return userName
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * ID of scoped subject, i.e. if subject is "workspace" this method returns workspaceId etc.
     * @return subjectId
     */
    @Override
    public String getSubjectId() {
        return subjectId;
    }

    /**
     * Some descriptive properties stored along with subject to not to make service ask for them (duplicates)
     * For example, workspace name etc
     * @return subjectProperties
     */
    @Override
    public Map<String, String> getSubjectProperties() {
        return subjectProperties;
    }

}
