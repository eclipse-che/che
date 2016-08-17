/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.account.spi.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

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
            return manager.createNamedQuery("Account.getByName",
                                            AccountImpl.class)
                          .setParameter("name", name)
                          .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException(String.format("Account with name '%s' was not found", name));
        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }
}
