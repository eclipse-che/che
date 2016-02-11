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
package org.eclipse.che.api.account.shared.dto;

import io.swagger.annotations.ApiModelProperty;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Describes new account membership
 *
 * @author Eugene Voevodin
 */
@DTO
public interface NewMembership {

    @ApiModelProperty(value = "User ID to be added to an account", required = true)
    String getUserId();

    void setUserId(String id);

    NewMembership withUserId(String id);

    @ApiModelProperty(value = "User roles in the account", required = true, allowableValues = "account/owner, account/member")
    List<String> getRoles();

    void setRoles(List<String> roles);

    NewMembership withRoles(List<String> roles);
}
