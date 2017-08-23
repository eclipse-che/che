/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.installer.server.exception.InstallerAlreadyExistsException;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.server.impl.InstallerFqn;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.server.spi.InstallerDao;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;

/** @author Anatolii Bazko */
@Singleton
public class JpaInstallerDao implements InstallerDao {

  private final Provider<EntityManager> managerProvider;

  @Inject
  public JpaInstallerDao(
      @SuppressWarnings("unused") DBInitializer dbInitializer,
      Provider<EntityManager> managerProvider) {
    this.managerProvider = managerProvider;
  }

  @Override
  public void create(InstallerImpl installer) throws InstallerException {
    requireNonNull(installer, "Required non-null installer");
    try {
      doCreate(installer);
    } catch (DuplicateKeyException x) {
      throw new InstallerAlreadyExistsException(
          format(
              "Installer with such fqn '%s:%s' already exists",
              installer.getId(), installer.getVersion()));
    } catch (RuntimeException x) {
      throw new InstallerException(x.getMessage(), x);
    }
  }

  @Override
  public void update(InstallerImpl installer) throws InstallerException {
    requireNonNull(installer, "Required non-null update");
    try {
      doUpdate(installer);
    } catch (NoResultException e) {
      throw new InstallerNotFoundException(
          format("Installer with fqn '%s' doesn't exist", InstallerFqn.of(installer)));
    } catch (RuntimeException x) {
      throw new InstallerException(x.getMessage(), x);
    }
  }

  @Override
  public void remove(InstallerFqn fqn) throws InstallerException {
    requireNonNull(fqn, "Required non-null fqn");
    try {
      doRemove(fqn);
    } catch (NoResultException e) {
    } catch (RuntimeException x) {
      throw new InstallerException(x.getMessage(), x);
    }
  }

  @Override
  @Transactional
  public InstallerImpl getByFqn(InstallerFqn fqn) throws InstallerException {
    requireNonNull(fqn, "Required non-null fqn");

    try {
      InstallerImpl installer =
          managerProvider
              .get()
              .createNamedQuery("Inst.getByKey", InstallerImpl.class)
              .setParameter("id", fqn.getId())
              .setParameter("version", fqn.getVersion())
              .getSingleResult();
      return new InstallerImpl(installer);
    } catch (NoResultException e) {
      throw new InstallerNotFoundException(format("Installer with fqn '%s' doesn't exist", fqn));
    } catch (RuntimeException e) {
      throw new InstallerException(e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public List<String> getVersions(String id) throws InstallerException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("Inst.getAllById", InstallerImpl.class)
          .setParameter("id", id)
          .getResultList()
          .stream()
          .map(InstallerImpl::getVersion)
          .collect(Collectors.toList());
    } catch (RuntimeException x) {
      throw new InstallerException(x.getMessage(), x);
    }
  }

  @Override
  @Transactional
  public Page<InstallerImpl> getAll(int maxItems, long skipCount) throws InstallerException {
    checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
    checkArgument(
        skipCount >= 0 && skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be negative or greater than " + Integer.MAX_VALUE);
    try {
      final List<InstallerImpl> list =
          managerProvider
              .get()
              .createNamedQuery("Inst.getAll", InstallerImpl.class)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList();
      return new Page<>(list, skipCount, maxItems, getTotalCount());
    } catch (RuntimeException x) {
      throw new InstallerException(x.getMessage(), x);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, InstallerException.class})
  protected void doCreate(InstallerImpl installer) throws InstallerException {
    EntityManager manage = managerProvider.get();
    manage.persist(installer);
    manage.flush();
  }

  @Transactional
  protected void doUpdate(InstallerImpl update) throws InstallerNotFoundException {
    InstallerFqn fqn = InstallerFqn.of(update);

    final EntityManager manager = managerProvider.get();
    InstallerImpl installer =
        manager
            .createNamedQuery("Inst.getByKey", InstallerImpl.class)
            .setParameter("id", fqn.getId())
            .setParameter("version", fqn.getVersion())
            .getSingleResult();
    update.setInternalId(installer.getInternalId());

    manager.merge(update);
    manager.flush();
  }

  @Transactional
  protected void doRemove(InstallerFqn fqn) {
    final EntityManager manager = managerProvider.get();
    InstallerImpl installer =
        manager
            .createNamedQuery("Inst.getByKey", InstallerImpl.class)
            .setParameter("id", fqn.getId())
            .setParameter("version", fqn.getVersion())
            .getSingleResult();
    manager.remove(installer);
    manager.flush();
  }

  @Override
  @Transactional
  public long getTotalCount() throws InstallerException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("Inst.getTotalCount", Long.class)
          .getSingleResult();
    } catch (RuntimeException x) {
      throw new InstallerException(x.getMessage(), x);
    }
  }
}
