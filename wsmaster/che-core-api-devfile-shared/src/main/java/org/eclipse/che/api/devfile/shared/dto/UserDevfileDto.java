/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.shared.dto;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.shared.dto.devfile.ComponentDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileCommandDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.MetadataDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.ProjectDto;
import org.eclipse.che.dto.shared.DTO;

@DTO
@Beta
public interface UserDevfileDto extends UserDevfile, Hyperlinks {

  void setId(String id);

  UserDevfileDto withId(String id);

  @Override
  String getApiVersion();

  void setApiVersion(String apiVersion);

  UserDevfileDto withApiVersion(String apiVersion);

  @Override
  List<ProjectDto> getProjects();

  void setProjects(List<ProjectDto> projects);

  UserDevfileDto withProjects(List<ProjectDto> projects);

  @Override
  List<ComponentDto> getComponents();

  void setComponents(List<ComponentDto> components);

  UserDevfileDto withComponents(List<ComponentDto> components);

  @Override
  List<DevfileCommandDto> getCommands();

  void setCommands(List<DevfileCommandDto> commands);

  UserDevfileDto withCommands(List<DevfileCommandDto> commands);

  @Override
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  UserDevfileDto withAttributes(Map<String, String> attributes);

  @Override
  MetadataDto getMetadata();

  void setMetadata(MetadataDto metadata);

  UserDevfileDto withMetadata(MetadataDto metadata);

  @Override
  UserDevfileDto withLinks(List<Link> links);
}
