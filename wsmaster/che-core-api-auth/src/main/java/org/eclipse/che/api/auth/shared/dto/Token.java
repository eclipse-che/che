/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.auth.shared.dto;

import org.eclipse.che.dto.shared.DTO;
import io.swagger.annotations.ApiModelProperty;

/**
 * Authentication token.
 *
 * @author gazarenkov
 */
@DTO
public interface Token {
    @ApiModelProperty(value = "Authentication token obtained after login", required = true)
    String getValue();

    void setValue(String value);

    Token withValue(String value);
}
