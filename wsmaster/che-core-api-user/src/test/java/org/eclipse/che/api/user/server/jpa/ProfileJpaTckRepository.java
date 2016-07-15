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

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.Collection;

import static java.util.Collections.emptyList;

@Transactional
public class ProfileJpaTckRepository implements TckRepository<ProfileImpl> {

    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public void createAll(Collection<? extends ProfileImpl> entities) throws TckRepositoryException {
        final EntityManager manager = managerProvider.get();
        for (ProfileImpl profile : entities) {
            manager.persist(new UserImpl(profile.getUserId(),
                                         profile.getUserId() + "@eclipse.org",
                                         profile.getUserId(),
                                         "password",
                                         emptyList()));
            manager.persist(profile);
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        final EntityManager manager = managerProvider.get();
        manager.createQuery("DELETE FROM Profile").executeUpdate();
        manager.createQuery("DELETE FROM \"User\"").executeUpdate();
    }
}
