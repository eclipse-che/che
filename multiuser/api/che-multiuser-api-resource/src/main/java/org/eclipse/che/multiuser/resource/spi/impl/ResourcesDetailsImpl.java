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
package org.eclipse.che.multiuser.resource.spi.impl;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.model.ResourcesDetails;

/** @author Sergii Leschenko */
public class ResourcesDetailsImpl implements ResourcesDetails {
  private String accountId;
  private List<ProvidedResourcesImpl> providedResources;
  private List<ResourceImpl> totalResources;

  public ResourcesDetailsImpl(ResourcesDetails resourcesDetails) {
    this(
        resourcesDetails.getAccountId(),
        resourcesDetails.getProvidedResources(),
        resourcesDetails.getTotalResources());
  }

  public ResourcesDetailsImpl(
      String owner,
      List<? extends ProvidedResources> providedResources,
      List<? extends Resource> totalResources) {
    this.accountId = owner;
    if (providedResources != null) {
      this.providedResources =
          providedResources.stream().map(ProvidedResourcesImpl::new).collect(Collectors.toList());
    }
    if (totalResources != null) {
      this.totalResources =
          totalResources.stream().map(ResourceImpl::new).collect(Collectors.toList());
    }
  }

  @Override
  public String getAccountId() {
    return accountId;
  }

  @Override
  public List<ProvidedResourcesImpl> getProvidedResources() {
    if (providedResources == null) {
      providedResources = new ArrayList<>();
    }
    return providedResources;
  }

  @Override
  public List<ResourceImpl> getTotalResources() {
    if (totalResources == null) {
      totalResources = new ArrayList<>();
    }
    return totalResources;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ResourcesDetailsImpl)) return false;
    ResourcesDetailsImpl resourceDetails = (ResourcesDetailsImpl) o;
    return Objects.equal(accountId, resourceDetails.accountId)
        && Objects.equal(providedResources, resourceDetails.providedResources)
        && Objects.equal(totalResources, resourceDetails.totalResources);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(accountId, providedResources, totalResources);
  }

  @Override
  public String toString() {
    return "ResourcesDetailsImpl{"
        + "accountId='"
        + accountId
        + '\''
        + ", providedResources="
        + providedResources
        + ", totalResources="
        + totalResources
        + '}';
  }
}
