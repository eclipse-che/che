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
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Implementation of {@link TckRepository}.
 *
 * @author Anton Korneta
 */
public class PreferenceJpaTckRepository implements TckRepository<Pair<String, Map<String, String>>> {

    @Inject
    private EntityManagerFactory factory;

    @Override
    public void createAll(Collection<? extends Pair<String, Map<String, String>>> entities) throws TckRepositoryException {
        final EntityManager manager = factory.createEntityManager();

        manager.getTransaction().begin();

        int i = 0;
        for (Pair<String, Map<String, String>> pair : entities) {
            manager.persist(new UserImpl(pair.first, "email_" + i, "name_" + i, "password", Collections.emptyList()));
            manager.persist(new PreferenceEntity(pair.first, pair.second));
            i++;
        }
        manager.getTransaction().commit();
        manager.close();
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();
        manager.createQuery("DELETE FROM Preference").executeUpdate();
        manager.createQuery("DELETE FROM User").executeUpdate();
        manager.getTransaction().commit();
        manager.close();
    }
}
