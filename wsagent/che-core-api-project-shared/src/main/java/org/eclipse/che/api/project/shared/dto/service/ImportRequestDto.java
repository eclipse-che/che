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
package org.eclipse.che.api.project.shared.dto.service;

import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ImportRequestDto {
  String getWsPath();

  void setWsPath(String wsPath);

  ImportRequestDto withWsPath(String wsPath);

  SourceStorageDto getSourceStorage();

  void setSourceStorage(SourceStorageDto sourceStorage);

  ImportRequestDto withSourceStorage(SourceStorageDto sourceStorage);
}
