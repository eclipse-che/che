/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.dto;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.dto.shared.DTO;

/** @author Yevhenii Voevodin */
@DTO
public interface WorkspaceDto extends Workspace {

  @Override
  WorkspaceConfigDto getConfig();

  void setConfig(WorkspaceConfigDto config);

  WorkspaceDto withConfig(WorkspaceConfigDto config);

  @Override
  RuntimeDto getRuntime();

  void setRuntime(RuntimeDto runtime);

  WorkspaceDto withRuntime(RuntimeDto runtime);

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

  Map<String, String> getLinks();

  void setLinks(Map<String, String> links);

  WorkspaceDto withLinks(Map<String, String> links);
}
