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

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.security.PasswordEncryptor;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Collection;

public class UserJpaTckRepository implements TckRepository<UserImpl> {

    @Inject
    private EntityManagerFactory factory;

    @Inject
    private PasswordEncryptor encryptor;

    @Override
    public void createAll(Collection<? extends UserImpl> entities) throws TckRepositoryException {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();
        entities.stream()
                .map(user -> new UserImpl(user.getId(),
                                          user.getEmail(),
                                          user.getName(),
                                          encryptor.encrypt(user.getPassword()),
                                          user.getAliases()))
                .forEach(manager::persist);
        manager.getTransaction().commit();
        manager.close();
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();
        manager.createQuery("DELETE FROM User").executeUpdate();
        manager.getTransaction().commit();
        manager.close();
    }
}
