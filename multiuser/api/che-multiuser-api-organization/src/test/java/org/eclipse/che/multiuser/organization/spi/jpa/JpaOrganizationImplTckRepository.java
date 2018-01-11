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
package org.eclipse.che.multiuser.organization.spi.jpa;

import com.google.inject.Inject;
import com.google.inject.persist.UnitOfWork;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;

/**
 * Organizations require to have own repository because it is important to delete organization in
 * reverse order that they were stored. It allows to resolve problems with removing suborganization
 * before parent organization removing.
 *
 * @author Sergii Leschenko
 */
public class JpaOrganizationImplTckRepository extends JpaTckRepository<OrganizationImpl> {
  @Inject protected Provider<EntityManager> managerProvider;

  @Inject protected UnitOfWork uow;

  private final List<OrganizationImpl> createdOrganizations = new ArrayList<>();

  public JpaOrganizationImplTckRepository() {
    super(OrganizationImpl.class);
  }

  @Override
  public void createAll(Collection<? extends OrganizationImpl> entities)
      throws TckRepositoryException {
    super.createAll(entities);
    // It's important to save organization to remove them in the reverse order
    createdOrganizations.addAll(entities);
  }

  @Override
  public void removeAll() throws TckRepositoryException {
    uow.begin();
    final EntityManager manager = managerProvider.get();
    try {
      manager.getTransaction().begin();

      for (int i = createdOrganizations.size() - 1; i > -1; i--) {
        // The query 'DELETE FROM ....' won't be correct as it will ignore orphanRemoval
        // and may also ignore some configuration options, while EntityManager#remove won't
        try {
          final OrganizationImpl organizationToRemove =
              manager
                  .createQuery(
                      "SELECT o FROM Organization o " + "WHERE o.id = :id", OrganizationImpl.class)
                  .setParameter("id", createdOrganizations.get(i).getId())
                  .getSingleResult();
          manager.remove(organizationToRemove);
        } catch (NoResultException ignored) {
          // it is already removed
        }
      }
      createdOrganizations.clear();

      manager.getTransaction().commit();
    } catch (RuntimeException x) {
      if (manager.getTransaction().isActive()) {
        manager.getTransaction().rollback();
      }
      throw new TckRepositoryException(x.getLocalizedMessage(), x);
    } finally {
      uow.end();
    }

    // remove all objects that was created in tests
    super.removeAll();
  }
}
