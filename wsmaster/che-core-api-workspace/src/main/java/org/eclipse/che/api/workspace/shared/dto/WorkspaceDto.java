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

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 * @author Yevhenii Voevodin
 */
@DTO
public interface WorkspaceDto extends Workspace, Hyperlinks {

    @Override
    WorkspaceConfigDto getConfig();

    WorkspaceDto withConfig(WorkspaceConfigDto config);

    @Override
    WorkspaceRuntimeDto getRuntime();

    WorkspaceDto withRuntime(WorkspaceRuntimeDto runtime);

    WorkspaceDto withId(String id);

    WorkspaceDto withNamespace(String owner);

    WorkspaceDto withStatus(WorkspaceStatus status);

    WorkspaceDto withTemporary(boolean isTemporary);

    WorkspaceDto withAttributes(Map<String, String> attributes);

    @Override
    WorkspaceDto withLinks(List<Link> links);
}
