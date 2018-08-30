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
package org.eclipse.che.api.workspace.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface WorkspaceConfigDto extends WorkspaceConfig, Hyperlinks {

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getName();

  WorkspaceConfigDto withName(String name);

  void setName(String name);

  @Override
  @FactoryParameter(obligation = MANDATORY)
  String getDefaultEnv();

  void setDefaultEnv(String defaultEnvironment);

  WorkspaceConfigDto withDefaultEnv(String defaultEnvironment);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getDescription();

  void setDescription(String description);

  WorkspaceConfigDto withDescription(String description);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  List<CommandDto> getCommands();

  void setCommands(List<CommandDto> commands);

  WorkspaceConfigDto withCommands(List<CommandDto> commands);

  @Override
  @FactoryParameter(obligation = MANDATORY)
  List<ProjectConfigDto> getProjects();

  void setProjects(List<ProjectConfigDto> projects);

  WorkspaceConfigDto withProjects(List<ProjectConfigDto> projects);

  @Override
  @FactoryParameter(obligation = MANDATORY)
  Map<String, EnvironmentDto> getEnvironments();

  void setEnvironments(Map<String, EnvironmentDto> environments);

  WorkspaceConfigDto withEnvironments(Map<String, EnvironmentDto> environments);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  WorkspaceConfigDto withAttributes(Map<String, String> attributes);

  @Override
  WorkspaceConfigDto withLinks(List<Link> links);
}
