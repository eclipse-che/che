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
package org.eclipse.che.api.factory.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.Collection;

/**
 * @author Anton Korneta
 */
@Transactional
public class FactoryJpaTckRepository implements TckRepository<FactoryImpl> {

    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public void createAll(Collection<? extends FactoryImpl> factories) throws TckRepositoryException {
        final EntityManager manager = managerProvider.get();
        for (FactoryImpl factory : factories) {
            final String id = factory.getCreator().getUserId();
            manager.persist(new UserImpl(id, "email_" + id, "name_" + id));
            manager.persist(factory);
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        final EntityManager manager = managerProvider.get();
        manager.createQuery("SELECT factory FROM Factory factory", FactoryImpl.class)
               .getResultList()
               .forEach(manager::remove);
    }
}
