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
package org.eclipse.che.account.spi.jpa;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;

/**
 * JPA based implementation of {@link AccountDao}.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class JpaAccountDao implements AccountDao {
  private final Provider<EntityManager> managerProvider;

  @Inject
  public JpaAccountDao(Provider<EntityManager> managerProvider) {
    this.managerProvider = managerProvider;
  }

  @Override
  public void create(AccountImpl account) throws ConflictException, ServerException {
    requireNonNull(account, "Required non-null account");
    try {
      doCreate(account);
    } catch (DuplicateKeyException e) {
      throw new ConflictException("Account with such id or name already exists");
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public void update(AccountImpl account)
      throws NotFoundException, ConflictException, ServerException {
    requireNonNull(account, "Required non-null account");
    try {
      doUpdate(account);
    } catch (DuplicateKeyException x) {
      throw new ConflictException("Account with such name already exists");
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  @Transactional
  public AccountImpl getById(String id) throws NotFoundException, ServerException {
    requireNonNull(id, "Required non-null account id");
    final EntityManager manager = managerProvider.get();
    try {
      AccountImpl account = manager.find(AccountImpl.class, id);
      if (account == null) {
        throw new NotFoundException(format("Account with id '%s' was not found", id));
      }
      return account;
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  @Transactional
  public AccountImpl getByName(String name) throws ServerException, NotFoundException {
    requireNonNull(name, "Required non-null account name");
    final EntityManager manager = managerProvider.get();
    try {
      return manager
          .createNamedQuery("Account.getByName", AccountImpl.class)
          .setParameter("name", name)
          .getSingleResult();
    } catch (NoResultException e) {
      throw new NotFoundException(String.format("Account with name '%s' was not found", name));
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public void remove(String id) throws ServerException {
    requireNonNull(id, "Required non-null account id");
    try {
      doRemove(id);
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Transactional
  protected void doCreate(AccountImpl account) {
    final EntityManager manager = managerProvider.get();
    manager.persist(account);
    manager.flush();
  }

  @Transactional
  protected void doUpdate(AccountImpl update) throws NotFoundException {
    final EntityManager manager = managerProvider.get();
    final AccountImpl account = manager.find(AccountImpl.class, update.getId());
    if (account == null) {
      throw new NotFoundException(
          format("Couldn't update account with id '%s' because it doesn't exist", update.getId()));
    }
    manager.merge(update);
    manager.flush();
  }

  @Transactional
  protected void doRemove(String id) {
    final EntityManager manager = managerProvider.get();
    AccountImpl account = manager.find(AccountImpl.class, id);
    if (account != null) {
      manager.remove(account);
      manager.flush();
    }
  }
}
