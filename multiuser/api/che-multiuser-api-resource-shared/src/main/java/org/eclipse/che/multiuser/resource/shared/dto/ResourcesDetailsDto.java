/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.resource.model.ResourcesDetails;

/** @author Sergii Leschenko */
@DTO
public interface ResourcesDetailsDto extends ResourcesDetails {
  @Override
  String getAccountId();

  void setAccountId(String accountId);

  ResourcesDetailsDto withAccountId(String accountId);

  @Override
  List<ProvidedResourcesDto> getProvidedResources();

  void setProvidedResources(List<ProvidedResourcesDto> providedResources);

  ResourcesDetailsDto withProvidedResources(List<ProvidedResourcesDto> providedResources);

  @Override
  List<ResourceDto> getTotalResources();

  void setTotalResources(List<ResourceDto> totalResources);

  ResourcesDetailsDto withTotalResources(List<ResourceDto> totalResources);
}
