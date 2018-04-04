/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
