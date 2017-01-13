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
package org.eclipse.che.api.workspace.shared.dto.stack;

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Alexander Andrienko
 */
@DTO
public interface StackDto extends Stack, Hyperlinks {

    void setId(String id);

    StackDto withId(String id);

    void setName(String name);

    StackDto withName(String name);

    void setDescription(String description);

    StackDto withDescription(String description);

    void setScope(String scope);

    StackDto withScope(String scope);

    void setCreator(String creator);

    StackDto withCreator(String creator);

    void setTags(List<String> tags);

    StackDto withTags(List<String> tags);

    @Override
    WorkspaceConfigDto getWorkspaceConfig();

    void setWorkspaceConfig(WorkspaceConfigDto workspaceConfigDto);

    StackDto withWorkspaceConfig(WorkspaceConfigDto workspaceConfigDto);

    StackSourceDto getSource();

    void setSource(StackSourceDto source);

    StackDto withSource(StackSourceDto source);

    @Override
    List<StackComponentDto> getComponents();

    void setComponents(List<StackComponentDto> components);

    StackDto withComponents(List<StackComponentDto> components);

    StackDto withLinks(List<Link> links);
}
