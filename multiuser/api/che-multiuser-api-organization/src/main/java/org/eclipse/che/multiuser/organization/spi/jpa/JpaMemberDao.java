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
package org.eclipse.che.multiuser.organization.spi.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.jpa.AbstractJpaPermissionsDao;
import org.eclipse.che.multiuser.organization.spi.MemberDao;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;

/**
 * JPA based implementation of {@link MemberDao}.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class JpaMemberDao extends AbstractJpaPermissionsDao<MemberImpl> implements MemberDao {

  @Inject
  public JpaMemberDao(AbstractPermissionsDomain<MemberImpl> supportedDomain) throws IOException {
    super(supportedDomain);
  }

  @Override
  public MemberImpl get(String userId, String instanceId)
      throws ServerException, NotFoundException {
    return getMember(instanceId, userId);
  }

  @Override
  public Page<MemberImpl> getByInstance(String instanceId, int maxItems, long skipCount)
      throws ServerException {
    return getMembers(instanceId, maxItems, skipCount);
  }

  @Override
  public List<MemberImpl> getByUser(String userId) throws ServerException {
    return getMemberships(userId);
  }

  @Override
  public void remove(String userId, String organizationId) throws ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    requireNonNull(userId, "Required non-null user id");
    try {
      doRemove(organizationId, userId);
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public MemberImpl getMember(String organizationId, String userId)
      throws NotFoundException, ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    requireNonNull(userId, "Required non-null user id");
    try {
      return new MemberImpl(getEntity(wildcardToNull(userId), organizationId));
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  @Transactional
  public Page<MemberImpl> getMembers(String organizationId, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    checkArgument(
        skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be greater than " + Integer.MAX_VALUE);
    try {
      final EntityManager manager = managerProvider.get();
      final List<MemberImpl> members =
          manager
              .createNamedQuery("Member.getByOrganization", MemberImpl.class)
              .setParameter("organizationId", organizationId)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList()
              .stream()
              .map(MemberImpl::new)
              .collect(toList());
      final Long membersCount =
          manager
              .createNamedQuery("Member.getCountByOrganizationId", Long.class)
              .setParameter("organizationId", organizationId)
              .getSingleResult();
      return new Page<>(members, skipCount, maxItems, membersCount);
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  @Transactional
  public List<MemberImpl> getMemberships(String userId) throws ServerException {
    requireNonNull(userId, "Required non-null user id");
    try {
      final EntityManager manager = managerProvider.get();
      return manager
          .createNamedQuery("Member.getByUser", MemberImpl.class)
          .setParameter("userId", userId)
          .getResultList()
          .stream()
          .map(MemberImpl::new)
          .collect(toList());
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  @Transactional
  public Page<OrganizationImpl> getOrganizations(String userId, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(userId, "Required non-null user id");
    checkArgument(
        skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be greater than " + Integer.MAX_VALUE);
    try {
      final EntityManager manager = managerProvider.get();
      final List<OrganizationImpl> result =
          manager
              .createNamedQuery("Member.getOrganizations", OrganizationImpl.class)
              .setParameter("userId", userId)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList();
      final Long organizationsCount =
          manager
              .createNamedQuery("Member.getOrganizationsCount", Long.class)
              .setParameter("userId", userId)
              .getSingleResult();

      return new Page<>(result, skipCount, maxItems, organizationsCount);
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  @Transactional
  protected void doRemove(String organizationId, String userId) {
    final EntityManager manager = managerProvider.get();
    List<MemberImpl> members =
        manager
            .createNamedQuery("Member.getMember", MemberImpl.class)
            .setParameter("userId", userId)
            .setParameter("organizationId", organizationId)
            .getResultList();
    if (!members.isEmpty()) {
      manager.remove(members.get(0));
      manager.flush();
    }
  }

  @Override
  protected MemberImpl getEntity(String userId, String instanceId) throws NotFoundException {
    try {
      return doGet(userId, instanceId);
    } catch (NoResultException e) {
      throw new NotFoundException(
          String.format(
              "Membership of user %s in organization %s was not found", userId, instanceId));
    }
  }

  @Transactional
  protected MemberImpl doGet(String userId, String instanceId) {
    return managerProvider
        .get()
        .createNamedQuery("Member.getMember", MemberImpl.class)
        .setParameter("userId", userId)
        .setParameter("organizationId", instanceId)
        .getSingleResult();
  }
}
