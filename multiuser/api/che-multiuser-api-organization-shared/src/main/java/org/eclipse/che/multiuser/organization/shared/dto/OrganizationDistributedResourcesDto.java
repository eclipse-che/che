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
package org.eclipse.che.multiuser.organization.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.organization.shared.model.OrganizationDistributedResources;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;

/** @author Sergii Leschenko */
@DTO
public interface OrganizationDistributedResourcesDto extends OrganizationDistributedResources {
  @Override
  String getOrganizationId();

  void setOrganizationId(String organizationId);

  OrganizationDistributedResourcesDto withOrganizationId(String organizationId);

  @Override
  List<ResourceDto> getResourcesCap();

  void setResourcesCap(List<ResourceDto> resourcesCap);

  OrganizationDistributedResourcesDto withResourcesCap(List<ResourceDto> resourcesCap);
}
