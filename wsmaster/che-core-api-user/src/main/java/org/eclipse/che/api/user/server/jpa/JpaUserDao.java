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
package org.eclipse.che.api.user.server.jpa;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.DuplicateKeyException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.security.PasswordEncryptor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author Yevhenii Voevodin
 */
@Singleton
public class JpaUserDao implements UserDao {

    @Inject
    private EntityManagerFactory factory;
    @Inject
    private PasswordEncryptor    encryptor;

    @Override
    public UserImpl getByAliasAndPassword(String emailOrAliasOrName, String password) throws NotFoundException, ServerException {
        requireNonNull(emailOrAliasOrName, "Required non-null alias");
        requireNonNull(password, "Required non-null password");
        final EntityManager manager = factory.createEntityManager();
        try {
            final UserImpl user = manager.createQuery("SELECT distinct(u) " +
                                                      "FROM User u " +
                                                      "WHERE u.name = :alias OR u.email = :alias OR :alias MEMBER OF u.aliases",
                                                      UserImpl.class)
                                         .setParameter("alias", emailOrAliasOrName)
                                         .getSingleResult();
            if (!encryptor.test(password, user.getPassword())) {
                throw new NotFoundException(format("User with alias '%s' and given password doesn't exist", emailOrAliasOrName));
            }
            return user;
        } catch (NoResultException x) {
            throw new NotFoundException(format("User with alias '%s' and given password doesn't exist", emailOrAliasOrName));
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    public void create(UserImpl user) throws ConflictException, ServerException {
        requireNonNull(user, "Required non-null user");
        final EntityManager manager = factory.createEntityManager();
        try {
            manager.getTransaction().begin();
            manager.persist(user);
            manager.getTransaction().commit();
        } catch (DuplicateKeyException x) {
            // TODO make more concrete
            throw new ConflictException("User with such id/name/email/alias already exists");
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
    }

    @Override
    public void update(UserImpl update) throws NotFoundException, ServerException, ConflictException {
        requireNonNull(update, "Required non-null update");
        final EntityManager manager = factory.createEntityManager();
        try {
            manager.getTransaction().begin();
            if (manager.find(UserImpl.class, update.getId()) == null) {
                throw new NotFoundException(format("Couldn't update user with id '%s' because it doesn't exist", update.getId()));
            }
            manager.merge(update);
            manager.getTransaction().commit();
        } catch (DuplicateKeyException x) {
            // TODO make more concrete
            throw new ConflictException("User with such name/email/alias already exists");
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
    }

    @Override
    public void remove(String id) throws ServerException, ConflictException {
        requireNonNull(id, "Required non-null id");
        final EntityManager manager = factory.createEntityManager();
        try {
            manager.getTransaction().begin();
            final UserImpl user = manager.find(UserImpl.class, id);
            if (user != null) {
                manager.remove(user);
            }
            manager.getTransaction().commit();
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
    }

    @Override
    public UserImpl getByAlias(String alias) throws NotFoundException, ServerException {
        requireNonNull(alias, "Required non-null alias");
        final EntityManager manager = factory.createEntityManager();
        try {
            return manager.createQuery("SELECT u FROM User u WHERE :alias MEMBER OF u.aliases", UserImpl.class)
                          .setParameter("alias", alias)
                          .getSingleResult();
        } catch (NoResultException x) {
            throw new NotFoundException(format("User with alias '%s' doesn't exist", alias));
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            manager.close();
        }
    }

    @Override
    public UserImpl getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Required non-null id");
        final EntityManager manager = factory.createEntityManager();
        try {
            final UserImpl user = manager.find(UserImpl.class, id);
            if (user == null) {
                throw new NotFoundException(format("User with id '%s' doesn't exist", id));
            }
            return user;
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            manager.close();
        }
    }

    @Override
    public UserImpl getByName(String name) throws NotFoundException, ServerException {
        requireNonNull(name, "Required non-null name");
        final EntityManager manager = factory.createEntityManager();
        try {
            return manager.createQuery("SELECT u FROM User u WHERE u.name = :name", UserImpl.class)
                          .setParameter("name", name)
                          .getSingleResult();
        } catch (NoResultException x) {
            throw new NotFoundException(format("User with name '%s' doesn't exist", name));
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            manager.close();
        }
    }

    @Override
    public UserImpl getByEmail(String email) throws NotFoundException, ServerException {
        requireNonNull(email, "Required non-null email");
        final EntityManager manager = factory.createEntityManager();
        try {
            return manager.createQuery("SELECT u FROM User u WHERE u.email = :email", UserImpl.class)
                          .setParameter("email", email)
                          .getSingleResult();
        } catch (NoResultException x) {
            throw new NotFoundException(format("User with email '%s' doesn't exist", email));
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            manager.close();
        }
    }
}
