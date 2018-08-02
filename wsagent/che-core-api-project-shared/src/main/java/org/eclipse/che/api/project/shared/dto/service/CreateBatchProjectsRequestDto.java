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

import java.util.List;
import org.eclipse.che.api.project.shared.dto.NewProjectConfigDto;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface CreateBatchProjectsRequestDto {
  boolean isRewrite();

  void setRewrite(boolean rewrite);

  CreateBatchProjectsRequestDto withRewrite(boolean rewrite);

  List<NewProjectConfigDto> getNewProjectConfigs();

  void setNewProjectConfigs(List<NewProjectConfigDto> newProjectConfigs);

  CreateBatchProjectsRequestDto withNewProjectConfigs(List<NewProjectConfigDto> newProjectConfigs);
}
