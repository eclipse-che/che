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
package org.eclipse.che.multiuser.resource.spi.impl;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;
import org.eclipse.che.multiuser.resource.model.Resource;

/** @author Sergii Leschenko */
public class ProvidedResourcesImpl implements ProvidedResources {
  private String provider;
  private String id;
  private String owner;
  private Long startTime;
  private Long endTime;
  private List<ResourceImpl> resources;

  public ProvidedResourcesImpl(ProvidedResources providedResources) {
    this(
        providedResources.getProviderId(),
        providedResources.getId(),
        providedResources.getOwner(),
        providedResources.getStartTime(),
        providedResources.getEndTime(),
        providedResources.getResources());
  }

  public ProvidedResourcesImpl(
      String provider,
      String id,
      String owner,
      Long startTime,
      Long endTime,
      Collection<? extends Resource> resources) {
    this.provider = provider;
    this.id = id;
    this.owner = owner;
    this.startTime = startTime;
    this.endTime = endTime;
    if (resources != null) {
      this.resources = resources.stream().map(ResourceImpl::new).collect(Collectors.toList());
    }
  }

  @Override
  public String getProviderId() {
    return provider;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getOwner() {
    return owner;
  }

  @Override
  public Long getStartTime() {
    return startTime;
  }

  @Override
  public Long getEndTime() {
    return endTime;
  }

  @Override
  public List<ResourceImpl> getResources() {
    if (resources == null) {
      resources = new ArrayList<>();
    }
    return resources;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProvidedResourcesImpl)) return false;
    ProvidedResourcesImpl that = (ProvidedResourcesImpl) o;
    return Objects.equal(provider, that.provider)
        && Objects.equal(id, that.id)
        && Objects.equal(owner, that.owner)
        && Objects.equal(startTime, that.startTime)
        && Objects.equal(endTime, that.endTime)
        && Objects.equal(getResources(), that.getResources());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(provider, id, owner, startTime, endTime, getResources());
  }

  @Override
  public String toString() {
    return "GrantedResourceImpl{"
        + "provider='"
        + provider
        + '\''
        + ", id='"
        + id
        + '\''
        + ", owner='"
        + owner
        + '\''
        + ", startTime="
        + startTime
        + ", endTime="
        + endTime
        + ", resources="
        + getResources()
        + '}';
  }
}
