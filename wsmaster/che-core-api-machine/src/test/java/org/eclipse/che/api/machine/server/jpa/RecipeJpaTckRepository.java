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
package org.eclipse.che.api.machine.server.jpa;

import org.eclipse.che.api.machine.server.model.impl.AclEntryImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Collection;
import java.util.Collections;

/**
 * Implementation of {@link TckRepository}.
 *
 * @author Anton Korneta
 */
public class RecipeJpaTckRepository implements TckRepository<RecipeImpl> {

    @Inject
    private EntityManagerFactory factory;

    @Override
    public void createAll(Collection<? extends RecipeImpl> entities) throws TckRepositoryException {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();
        int i = 0;
        for (RecipeImpl entity : entities) {
            for (AclEntryImpl acl : entity.getAcl()) {
                manager.persist(new UserImpl(acl.getUser(), "email_" + i, "name_" + i, "password", Collections.emptyList()));
                i++;
            }
        }
        entities.stream().forEach(manager::persist);
        manager.getTransaction().commit();
        manager.close();
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        final EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();
        manager.createQuery("delete from Recipe").executeUpdate();
        manager.createQuery("delete from Acl").executeUpdate();
        manager.createQuery("delete from \"User\"").executeUpdate();
        manager.getTransaction().commit();
        manager.close();
    }
}
