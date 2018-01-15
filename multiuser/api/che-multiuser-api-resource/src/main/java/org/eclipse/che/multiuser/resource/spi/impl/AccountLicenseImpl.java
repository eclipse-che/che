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
package org.eclipse.che.multiuser.resource.spi.impl;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.multiuser.resource.model.AccountLicense;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;
import org.eclipse.che.multiuser.resource.model.Resource;

/** @author Sergii Leschenko */
public class AccountLicenseImpl implements AccountLicense {
  private String accountId;
  private List<ProvidedResourcesImpl> resourcesDetails;
  private List<ResourceImpl> totalResources;

  public AccountLicenseImpl(AccountLicense license) {
    this(license.getAccountId(), license.getResourcesDetails(), license.getTotalResources());
  }

  public AccountLicenseImpl(
      String owner,
      List<? extends ProvidedResources> resourcesDetails,
      List<? extends Resource> totalResources) {
    this.accountId = owner;
    if (resourcesDetails != null) {
      this.resourcesDetails =
          resourcesDetails.stream().map(ProvidedResourcesImpl::new).collect(Collectors.toList());
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
  public List<ProvidedResourcesImpl> getResourcesDetails() {
    if (resourcesDetails == null) {
      resourcesDetails = new ArrayList<>();
    }
    return resourcesDetails;
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
    if (!(o instanceof AccountLicenseImpl)) return false;
    AccountLicenseImpl license = (AccountLicenseImpl) o;
    return Objects.equal(accountId, license.accountId)
        && Objects.equal(resourcesDetails, license.resourcesDetails)
        && Objects.equal(totalResources, license.totalResources);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(accountId, resourcesDetails, totalResources);
  }

  @Override
  public String toString() {
    return "AccountLicenseImpl{"
        + "accountId='"
        + accountId
        + '\''
        + ", resourcesDetails="
        + resourcesDetails
        + ", totalResources="
        + totalResources
        + '}';
  }
}
