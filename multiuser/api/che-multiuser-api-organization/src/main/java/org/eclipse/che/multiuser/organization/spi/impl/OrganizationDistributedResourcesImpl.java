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
package org.eclipse.che.multiuser.organization.spi.impl;

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
import org.eclipse.che.multiuser.organization.shared.model.OrganizationDistributedResources;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Data object for {@link OrganizationDistributedResources}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "OrganizationDistributedResources")
@NamedQueries({
  @NamedQuery(
    name = "OrganizationDistributedResources.get",
    query =
        "SELECT r "
            + "FROM OrganizationDistributedResources r "
            + "WHERE r.organizationId = :organizationId"
  ),
  @NamedQuery(
    name = "OrganizationDistributedResources.getByParent",
    query =
        "SELECT r "
            + "FROM OrganizationDistributedResources r "
            + "WHERE r.organization.parent = :parent"
  ),
  @NamedQuery(
    name = "OrganizationDistributedResources.getCountByParent",
    query =
        "SELECT COUNT(r) "
            + "FROM OrganizationDistributedResources r "
            + "WHERE r.organization.parent = :parent"
  )
})
@Table(name = "che_organization_distributed_resources")
public class OrganizationDistributedResourcesImpl implements OrganizationDistributedResources {
  @Id
  @Column(name = "organization_id")
  private String organizationId;

  @PrimaryKeyJoinColumn private OrganizationImpl organization;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinTable(
    name = "che_organization_distributed_resources_resource",
    joinColumns = @JoinColumn(name = "organization_distributed_resources_id"),
    inverseJoinColumns = @JoinColumn(name = "resource_id")
  )
  private List<ResourceImpl> resourcesCap;

  public OrganizationDistributedResourcesImpl() {}

  public OrganizationDistributedResourcesImpl(
      OrganizationDistributedResources organizationDistributedResource) {
    this(
        organizationDistributedResource.getOrganizationId(),
        organizationDistributedResource.getResourcesCap());
  }

  public OrganizationDistributedResourcesImpl(
      String organizationId, List<? extends Resource> resourcesCap) {
    this.organizationId = organizationId;
    if (resourcesCap != null) {
      this.resourcesCap = resourcesCap.stream().map(ResourceImpl::new).collect(Collectors.toList());
    }
  }

  @Override
  public String getOrganizationId() {
    return organizationId;
  }

  @Override
  public List<ResourceImpl> getResourcesCap() {
    if (resourcesCap == null) {
      resourcesCap = new ArrayList<>();
    }
    return resourcesCap;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OrganizationDistributedResourcesImpl)) {
      return false;
    }
    final OrganizationDistributedResourcesImpl that = (OrganizationDistributedResourcesImpl) obj;
    return Objects.equals(organizationId, that.organizationId)
        && Objects.equals(organization, that.organization)
        && getResourcesCap().equals(that.getResourcesCap());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(organizationId);
    hash = 31 * hash + Objects.hashCode(organization);
    hash = 31 * hash + getResourcesCap().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "OrganizationDistributedResourcesImpl{"
        + "organizationId='"
        + organizationId
        + '\''
        + ", organization="
        + organization
        + ", resourcesCaps="
        + getResourcesCap()
        + '}';
  }
}
