/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leshchenko */
@DTO
public interface ComponentDto extends Component {

  @Override
  String getAlias();

  void setAlias(String alias);

  ComponentDto withAlias(String alias);

  @Override
  String getType();

  void setType(String type);

  ComponentDto withType(String type);

  @Override
  String getId();

  void setId(String id);

  ComponentDto withId(String id);

  @Override
  String getRegistryUrl();

  void setRegistryUrl(String registryUrl);

  ComponentDto withRegistryUrl(String registryUrl);

  @Override
  Map<String, Serializable> getPreferences();

  void setPreferences(Map<String, Serializable> preferences);

  ComponentDto withPreferences(Map<String, Serializable> preferences);

  @Override
  String getReference();

  void setReference(String reference);

  ComponentDto withReference(String reference);

  @Override
  String getReferenceContent();

  void setReferenceContent(String referenceContent);

  ComponentDto withReferenceContent(String referenceContent);

  @Override
  List<EntrypointDto> getEntrypoints();

  void setEntrypoints(List<EntrypointDto> entrypoints);

  ComponentDto withEntrypoints(List<EntrypointDto> entrypoints);

  @Override
  Map<String, String> getSelector();

  void setSelector(Map<String, String> selector);

  ComponentDto withSelector(Map<String, String> selector);

  @Override
  String getImage();

  void setImage(String image);

  ComponentDto withImage(String image);

  @Override
  String getMemoryLimit();

  void setMemoryLimit(String memoryLimit);

  ComponentDto withMemoryLimit(String memoryLimit);

  @Override
  String getMemoryRequest();

  void setMemoryRequest(String memoryRequest);

  ComponentDto withMemoryRequest(String memoryRequest);

  @Override
  String getCpuLimit();

  void setCpuLimit(String cpuLimit);

  ComponentDto withCpuLimit(String cpuLimit);

  @Override
  String getCpuRequest();

  void setCpuRequest(String cpuRequest);

  ComponentDto withCpuRequest(String cpuRequest);

  @Override
  Boolean getMountSources();

  void setMountSources(Boolean mountSources);

  ComponentDto withMountSources(Boolean mountSources);

  @Override
  Boolean getAutomountWorkspaceSecrets();

  void setAutomountWorkspaceSecrets(Boolean automountWorkspaceSecrets);

  ComponentDto withAutomountWorkspaceSecrets(Boolean automountWorkspaceSecrets);

  @Override
  List<String> getCommand();

  void setCommand(List<String> command);

  ComponentDto withCommand(List<String> command);

  @Override
  List<String> getArgs();

  void setArgs(List<String> args);

  ComponentDto withArgs(List<String> args);

  @Override
  List<DevfileVolumeDto> getVolumes();

  void setVolumes(List<DevfileVolumeDto> volumes);

  ComponentDto withVolumes(List<DevfileVolumeDto> volumes);

  @Override
  List<EnvDto> getEnv();

  void setEnv(List<EnvDto> env);

  ComponentDto withEnv(List<EnvDto> env);

  @Override
  List<EndpointDto> getEndpoints();

  void setEndpoints(List<EndpointDto> endpoints);

  ComponentDto withEndpoints(List<EndpointDto> endpoints);
}
