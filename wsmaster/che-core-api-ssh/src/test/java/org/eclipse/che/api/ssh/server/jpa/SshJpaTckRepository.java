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

import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.Collection;

import static java.util.Collections.emptyList;

/**
 * @author Mihail Kuznyetsov
 */
@Transactional
public class SshJpaTckRepository implements TckRepository<SshPairImpl> {
    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public void createAll(Collection<? extends SshPairImpl> sshPairs) throws TckRepositoryException {
        EntityManager manager = this.managerProvider.get();
        for (SshPairImpl sshPair : sshPairs) {
            if (manager.find(UserImpl.class, sshPair.getOwner()) == null) {
                manager.persist(new UserImpl(sshPair.getOwner(),
                                             sshPair.getOwner() + "@eclipse.org",
                                             sshPair.getOwner(),
                                             "password",
                                             emptyList()));
            }
            manager.persist(sshPair);
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {

        EntityManager manager = this.managerProvider.get();
        manager.createQuery("Select pair FROM SshKeyPair pair", SshPairImpl.class).getResultList().forEach(manager::remove);
        manager.createQuery("DELETE FROM \"User\"").executeUpdate();
    }
}
