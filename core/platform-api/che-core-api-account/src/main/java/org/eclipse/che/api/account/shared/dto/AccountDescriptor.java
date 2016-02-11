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

import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.dto.shared.DTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * @author andrew00x
 */
@DTO
public interface AccountDescriptor {

    @ApiModelProperty(value = "Account attributes")
    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    AccountDescriptor withAttributes(Map<String, String> attributes);

    @ApiModelProperty(value = "Account name")
    String getName();

    void setName(String name);

    AccountDescriptor withName(String name);

    @ApiModelProperty(value = "Account ID")
    String getId();

    void setId(String id);

    AccountDescriptor withId(String id);

    List<UsersWorkspaceDto> getWorkspaces();

    void setWorkspaces(List<UsersWorkspaceDto> workspaces);

    AccountDescriptor withWorkspaces(List<UsersWorkspaceDto> workspaces);

    List<Link> getLinks();

    void setLinks(List<Link> links);

    AccountDescriptor withLinks(List<Link> links);
}
