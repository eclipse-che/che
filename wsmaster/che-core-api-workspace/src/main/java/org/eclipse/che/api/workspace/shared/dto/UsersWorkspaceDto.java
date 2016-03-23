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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author andrew00x
 */
@DTO
public interface UsersWorkspaceDto extends UsersWorkspace, Hyperlinks {

    @Override
    WorkspaceConfigDto getConfig();

    UsersWorkspaceDto withConfig(WorkspaceConfigDto config);

    UsersWorkspaceDto withId(String id);

    UsersWorkspaceDto withOwner(String owner);

    UsersWorkspaceDto withStatus(WorkspaceStatus status);

    UsersWorkspaceDto withTemporary(boolean isTemporary);

    @Override
    UsersWorkspaceDto withLinks(List<Link> links);
}
