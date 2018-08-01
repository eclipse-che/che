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
package org.eclipse.che.multiuser.organization.spi.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.organization.spi.OrganizationDistributedResourcesDao;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationDistributedResourcesImpl;

/**
 * JPA based implementation of {@link OrganizationDistributedResourcesDao}.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class JpaOrganizationDistributedResourcesDao implements OrganizationDistributedResourcesDao {
  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public void store(OrganizationDistributedResourcesImpl distributedResources)
      throws ServerException {
    requireNonNull(distributedResources, "Required non-null distributed resources");
    try {
      doStore(distributedResources);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public OrganizationDistributedResourcesImpl get(String organizationId)
      throws NotFoundException, ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    try {
      OrganizationDistributedResourcesImpl distributedResources =
          managerProvider.get().find(OrganizationDistributedResourcesImpl.class, organizationId);
      if (distributedResources == null) {
        throw new NotFoundException(
            "There are no distributed resources for organization with id '"
                + organizationId
                + "'.");
      }

      return new OrganizationDistributedResourcesImpl(distributedResources);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public Page<OrganizationDistributedResourcesImpl> getByParent(
      String organizationId, int maxItems, long skipCount) throws ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    checkArgument(
        skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be greater than " + Integer.MAX_VALUE);
    try {
      final EntityManager manager = managerProvider.get();
      final List<OrganizationDistributedResourcesImpl> distributedResources =
          manager
              .createNamedQuery(
                  "OrganizationDistributedResources.getByParent",
                  OrganizationDistributedResourcesImpl.class)
              .setParameter("parent", organizationId)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList();
      final Long distributedResourcesCount =
          manager
              .createNamedQuery("OrganizationDistributedResources.getCountByParent", Long.class)
              .setParameter("parent", organizationId)
              .getSingleResult();
      return new Page<>(
          distributedResources
              .stream()
              .map(OrganizationDistributedResourcesImpl::new)
              .collect(Collectors.toList()),
          skipCount,
          maxItems,
          distributedResourcesCount);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  public void remove(String organizationId) throws ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    try {
      doRemove(organizationId);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Transactional
  protected void doRemove(String id) {
    EntityManager manager = managerProvider.get();
    OrganizationDistributedResourcesImpl distributedResources =
        manager.find(OrganizationDistributedResourcesImpl.class, id);
    if (distributedResources != null) {
      manager.remove(distributedResources);
      manager.flush();
    }
  }

  @Transactional
  protected void doStore(OrganizationDistributedResourcesImpl distributedResources)
      throws ServerException {
    EntityManager manager = managerProvider.get();
    final OrganizationDistributedResourcesImpl existingDistributedResources =
        manager.find(
            OrganizationDistributedResourcesImpl.class, distributedResources.getOrganizationId());
    if (existingDistributedResources == null) {
      manager.persist(distributedResources);
    } else {
      existingDistributedResources.getResourcesCap().clear();
      existingDistributedResources.getResourcesCap().addAll(distributedResources.getResourcesCap());
    }
    manager.flush();
  }
}
