/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import org.eclipse.che.api.core.model.workspace.devfile.Volume;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leshchenko */
@DTO
public interface DevfileVolumeDto extends Volume {

  @Override
  String getName();

  void setName(String name);

  DevfileVolumeDto withName(String name);

  @Override
  String getContainerPath();

  void setContainerPath(String containerPath);

  DevfileVolumeDto withContainerPath(String containerPath);
}
