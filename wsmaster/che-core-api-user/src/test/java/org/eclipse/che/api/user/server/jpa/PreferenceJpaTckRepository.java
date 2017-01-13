/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Implementation of {@link TckRepository}.
 *
 * @author Anton Korneta
 */
@Transactional
public class PreferenceJpaTckRepository implements TckRepository<Pair<String, Map<String, String>>> {

    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public void createAll(Collection<? extends Pair<String, Map<String, String>>> entities) throws TckRepositoryException {
        final EntityManager manager = managerProvider.get();
        for (Pair<String, Map<String, String>> pair : entities) {
            manager.persist(new PreferenceEntity(pair.first, pair.second));
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        final EntityManager manager = managerProvider.get();
        manager.createQuery("SELECT prefs FROM Preference prefs", PreferenceEntity.class)
               .getResultList()
               .forEach(manager::remove);
    }
}
