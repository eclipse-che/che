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
package org.eclipse.che.api.workspace.shared.dto.devfile;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leshchenko */
@DTO
public interface DevfileDto extends Devfile {
  @Override
  String getApiVersion();

  void setApiVersion(String apiVersion);

  DevfileDto withApiVersion(String apiVersion);

  @Override
  List<ProjectDto> getProjects();

  void setProjects(List<ProjectDto> projects);

  DevfileDto withProjects(List<ProjectDto> projects);

  @Override
  List<ComponentDto> getComponents();

  void setComponents(List<ComponentDto> components);

  DevfileDto withComponents(List<ComponentDto> components);

  @Override
  List<DevfileCommandDto> getCommands();

  void setCommands(List<DevfileCommandDto> commands);

  DevfileDto withCommands(List<DevfileCommandDto> commands);

  @Override
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  DevfileDto withAttributes(Map<String, String> attributes);

  @Override
  MetadataDto getMetadata();

  void setMetadata(MetadataDto metadata);

  DevfileDto withMetadata(MetadataDto metadata);
}
