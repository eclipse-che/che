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
package org.eclipse.che.api.user.shared.dto;


import com.google.common.annotations.Beta;
import io.swagger.annotations.ApiModelProperty;

import org.eclipse.che.dto.shared.DTO;

// TODO should be removed by CODENVY-480

/**
 * Defines if the current user is or not in a current role
 * @author Florent Benoit
 */
@DTO
@Beta
public interface UserInRoleDescriptor {

    @ApiModelProperty(value = "Is in Role")
    boolean getIsInRole();

    void setIsInRole(boolean value);

    UserInRoleDescriptor withIsInRole(boolean value);

    @ApiModelProperty(value = "Name of the role")
    String getRoleName();

    void setRoleName(String roleName);

    UserInRoleDescriptor withRoleName(String roleName);

    @ApiModelProperty(value = "Scope of the role")
    String getScope();

    void setScope(String scope);

    UserInRoleDescriptor withScope(String scope);


    @ApiModelProperty(value = "ScopeID of the role")
    String getScopeId();

    void setScopeId(String scopeId);

    UserInRoleDescriptor withScopeId(String scope);


}
