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

import java.util.Map;

/**
 * @author andrew00x
 */
@DTO
public interface AccountUpdate {
    @ApiModelProperty(value = "Account name", required = true)
    String getName();

    void setName(String name);

    AccountUpdate withName(String name);

    @ApiModelProperty("Account attributes are optional. They are used to store random info about an account")
    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    AccountUpdate withAttributes(Map<String, String> attributes);
}
