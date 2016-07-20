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
package org.eclipse.che.api.ssh.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.DuplicateKeyException;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.model.impl.SshPairPrimaryKey;
import org.eclipse.che.api.ssh.server.spi.SshDao;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Mihail Kuznyetsov
 */
@Singleton
public class JpaSshDao implements SshDao {

    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public void create(SshPairImpl sshPair) throws ServerException, ConflictException {
        requireNonNull(sshPair);
        try {
            doCreate(sshPair);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(String.format("Ssh pair with service '%s' and name '%s' already exists",
                                                      sshPair.getService(),
                                                      sshPair.getName()));
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    @Transactional
    public List<SshPairImpl> get(String owner, String service) throws ServerException {
        requireNonNull(owner);
        requireNonNull(service);
        try {
            EntityManager manager = managerProvider.get();
            List<SshPairImpl> resultList = manager.createQuery("SELECT pair " +
                                                               "FROM SshKeyPair pair " +
                                                               "WHERE pair.owner = :owner " +
                                                               "  AND pair.service = :service", SshPairImpl.class)
                                                  .setParameter("owner", owner)
                                                  .setParameter("service", service)
                                                  .getResultList();
            return resultList;

        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public SshPairImpl get(String owner, String service, String name) throws ServerException, NotFoundException {
        requireNonNull(owner);
        requireNonNull(service);
        requireNonNull(name);
        try {
            EntityManager manager = managerProvider.get();
            SshPairImpl result =
                    manager.find(SshPairImpl.class, new SshPairPrimaryKey(owner, service, name));
            if (result == null) {
                throw new NotFoundException(String.format("Ssh pair with service '%s' and name '%s' was not found.", service, name));
            }
            return result;

        } catch (RuntimeException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void remove(String owner, String service, String name) throws ServerException, NotFoundException {
        requireNonNull(owner);
        requireNonNull(service);
        requireNonNull(name);
        try {
            doRemove(owner, service, name);
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Transactional
    protected void doCreate(SshPairImpl entity) {
        managerProvider.get().persist(entity);
    }

    @Transactional
    protected void doRemove(String owner, String service, String name) throws NotFoundException {
        EntityManager manager = managerProvider.get();
        SshPairImpl entity = manager.find(SshPairImpl.class, new SshPairPrimaryKey(owner, service, name));
        if (entity != null) {
            manager.remove(entity);
        } else {
            throw new NotFoundException(String.format("Ssh pair with service '%s' and name '%s' was not found.", service, name));
        }

    }

}
