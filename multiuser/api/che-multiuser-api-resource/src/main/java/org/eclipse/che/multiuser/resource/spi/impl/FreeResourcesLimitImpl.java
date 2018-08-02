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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.multiuser.resource.model.FreeResourcesLimit;
import org.eclipse.che.multiuser.resource.model.Resource;

/**
 * Data object for {@link FreeResourcesLimit}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "FreeResourcesLimit")
@NamedQueries({
  @NamedQuery(
    name = "FreeResourcesLimit.get",
    query = "SELECT limit FROM FreeResourcesLimit limit WHERE limit.accountId= :accountId"
  ),
  @NamedQuery(
    name = "FreeResourcesLimit.getAll",
    query = "SELECT limit FROM FreeResourcesLimit limit"
  ),
  @NamedQuery(
    name = "FreeResourcesLimit.getTotalCount",
    query = "SELECT COUNT(limit) FROM FreeResourcesLimit limit"
  )
})
@Table(name = "che_free_resources_limit")
public class FreeResourcesLimitImpl implements FreeResourcesLimit {
  @Id
  @Column(name = "account_id")
  private String accountId;

  @PrimaryKeyJoinColumn private AccountImpl account;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinTable(
    name = "che_free_resources_limit_resource",
    joinColumns = @JoinColumn(name = "free_resources_limit_account_id"),
    inverseJoinColumns = @JoinColumn(name = "resources_id")
  )
  private List<ResourceImpl> resources;

  public FreeResourcesLimitImpl() {}

  public FreeResourcesLimitImpl(FreeResourcesLimit freeResourcesLimit) {
    this(freeResourcesLimit.getAccountId(), freeResourcesLimit.getResources());
  }

  public FreeResourcesLimitImpl(String accountId, List<? extends Resource> resources) {
    this.accountId = accountId;
    if (resources != null) {
      this.resources = resources.stream().map(ResourceImpl::new).collect(Collectors.toList());
    }
  }

  @Override
  public String getAccountId() {
    return accountId;
  }

  @Override
  public List<ResourceImpl> getResources() {
    if (resources == null) {
      resources = new ArrayList<>();
    }
    return resources;
  }

  public void setResources(List<ResourceImpl> resources) {
    this.resources = resources;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FreeResourcesLimitImpl)) return false;
    FreeResourcesLimitImpl that = (FreeResourcesLimitImpl) o;
    return Objects.equals(accountId, that.accountId)
        && Objects.equals(getResources(), that.getResources());
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, getResources());
  }

  @Override
  public String toString() {
    return "FreeResourcesLimitImpl{"
        + "accountId='"
        + accountId
        + '\''
        + ", resources="
        + getResources()
        + '}';
  }
}
