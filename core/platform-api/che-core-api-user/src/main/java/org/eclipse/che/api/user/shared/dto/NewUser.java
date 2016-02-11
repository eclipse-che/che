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

import io.swagger.annotations.ApiModelProperty;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes new user
 *
 * @author Eugene Voevodin
 */
@DTO
public interface NewUser {

    @ApiModelProperty(value = "User name. This can be both name or email", required = true)
    String getName();

    void setName(String name);

    NewUser withName(String name);

    @ApiModelProperty("User password. Only system admin can create a new user with email/password, otherwise an auth token is required")
    String getPassword();

    void setPassword(String password);

    NewUser withPassword(String password);
}
