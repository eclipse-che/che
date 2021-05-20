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

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.dto.shared.DTO;

/** @author gazarenkov */
@DTO
public interface RuntimeIdentityDto extends RuntimeIdentity {

  @Override
  String getWorkspaceId();

  RuntimeIdentityDto withWorkspaceId(String workspaceId);

  @Override
  String getEnvName();

  RuntimeIdentityDto withEnvName(String envName);

  @Override
  String getOwnerId();

  RuntimeIdentityDto withOwnerId(String ownerId);

  @Override
  String getInfrastructureNamespace();

  RuntimeIdentityDto withInfrastructureNamespace(String infrastructureNamespace);
}
