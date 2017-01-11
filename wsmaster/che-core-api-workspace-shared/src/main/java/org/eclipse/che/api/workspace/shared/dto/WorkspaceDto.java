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

    void setConfig(WorkspaceConfigDto config);

    WorkspaceDto withConfig(WorkspaceConfigDto config);

    @Override
    WorkspaceRuntimeDto getRuntime();

    void setRuntime(WorkspaceRuntimeDto runtime);

    WorkspaceDto withRuntime(WorkspaceRuntimeDto runtime);

    void setId(String id);

    WorkspaceDto withId(String id);

    void setNamespace(String namespace);

    WorkspaceDto withNamespace(String owner);

    void setStatus(WorkspaceStatus status);

    WorkspaceDto withStatus(WorkspaceStatus status);

    void setTemporary(boolean isTemporary);

    WorkspaceDto withTemporary(boolean isTemporary);

    void setAttributes(Map<String, String> attributes);

    WorkspaceDto withAttributes(Map<String, String> attributes);

    @Override
    WorkspaceDto withLinks(List<Link> links);
}
