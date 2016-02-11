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

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author andrew00x
 */
@DTO
public interface UserDescriptor {
    @ApiModelProperty("User ID")
    String getId();

    void setId(String id);

    UserDescriptor withId(String id);


    @ApiModelProperty("User alias which is used for oAuth")
    List<String> getAliases();

    void setAliases(List<String> aliases);

    UserDescriptor withAliases(List<String> aliases);

    @ApiModelProperty("User email")
    String getEmail();

    void setEmail(String email);

    UserDescriptor withEmail(String email);

    @ApiModelProperty("User name")
    String getName();

    void setName(String name);

    UserDescriptor withName(String name);

    @ApiModelProperty("User password")
    String getPassword();

    void setPassword(String password);

    UserDescriptor withPassword(String password);

    List<Link> getLinks();

    void setLinks(List<Link> links);

    UserDescriptor withLinks(List<Link> links);
}
