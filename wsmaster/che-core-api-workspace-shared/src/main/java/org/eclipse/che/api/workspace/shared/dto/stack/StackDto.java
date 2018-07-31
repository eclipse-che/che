/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.dto.stack;

import java.util.List;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Andrienko */
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

  @Override
  List<StackComponentDto> getComponents();

  void setComponents(List<StackComponentDto> components);

  StackDto withComponents(List<StackComponentDto> components);

  StackDto withLinks(List<Link> links);
}
