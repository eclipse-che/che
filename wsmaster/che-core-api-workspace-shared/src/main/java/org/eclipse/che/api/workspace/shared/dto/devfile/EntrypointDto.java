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
import org.eclipse.che.api.core.model.workspace.devfile.Entrypoint;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leshchenko */
@DTO
public interface EntrypointDto extends Entrypoint {

  @Override
  String getParentName();

  void setParentName(String parentName);

  EntrypointDto withParentName(String parentName);

  @Override
  String getContainerName();

  void setContainerName(String containerName);

  EntrypointDto withContainerName(String containerName);

  @Override
  Map<String, String> getParentSelector();

  void setParentSelector(Map<String, String> parentSelector);

  EntrypointDto withParentSelector(Map<String, String> parentSelector);

  @Override
  List<String> getCommand();

  void setCommand(List<String> command);

  EntrypointDto withCommand(List<String> command);

  @Override
  List<String> getArgs();

  void setArgs(List<String> args);

  EntrypointDto withArgs(List<String> args);
}
