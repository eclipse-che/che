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
package org.eclipse.che.multiuser.resource.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;

/** @author Sergii Leschenko */
@DTO
public interface ProvidedResourcesDto extends ProvidedResources {
  @Override
  String getProviderId();

  void setProviderId(String providerId);

  ProvidedResourcesDto withProviderId(String providerId);

  @Override
  String getId();

  void setId(String id);

  ProvidedResourcesDto withId(String id);

  @Override
  String getOwner();

  void setOwner(String owner);

  ProvidedResourcesDto withOwner(String owner);

  @Override
  Long getStartTime();

  void setStartTime(Long startTime);

  ProvidedResourcesDto withStartTime(Long startTime);

  @Override
  Long getEndTime();

  void setEndTime(Long endTime);

  ProvidedResourcesDto withEndTime(Long endTime);

  @Override
  List<ResourceDto> getResources();

  void setResources(List<ResourceDto> resources);

  ProvidedResourcesDto withResources(List<ResourceDto> resources);
}
