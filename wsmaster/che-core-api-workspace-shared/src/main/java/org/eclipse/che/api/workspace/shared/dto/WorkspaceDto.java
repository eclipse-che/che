/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.dto;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.dto.shared.DTO;

/** @author Yevhenii Voevodin */
@DTO
public interface WorkspaceDto extends Workspace {

  @Override
  WorkspaceConfigDto getConfig();

  void setConfig(WorkspaceConfigDto config);

  WorkspaceDto withConfig(WorkspaceConfigDto config);

  @Override
  DevfileDto getDevfile();

  void setDevfile(DevfileDto devfile);

  WorkspaceDto withDevfile(DevfileDto devfile);

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
