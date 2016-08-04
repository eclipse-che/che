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

import com.google.inject.TypeLiteral;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.Collection;

/**
 * @author Anton Korneta
 */
public class JpaTckModule extends TckModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("main"));
        bind(JpaInitializer.class).asEagerSingleton();

        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).to(RecipeJpaTckRepository.class);
        bind(new TypeLiteral<TckRepository<SnapshotImpl>>() {}).toInstance(new JpaTckRepository<>(SnapshotImpl.class));

        bind(RecipeDao.class).to(JpaRecipeDao.class);
        bind(SnapshotDao.class).to(JpaSnapshotDao.class);
    }

    @Transactional
    public static class RecipeJpaTckRepository implements TckRepository<RecipeImpl> {

        @Inject
        private Provider<EntityManager> managerProvider;

        @Override
        public void createAll(Collection<? extends RecipeImpl> entities) throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            entities.stream().forEach(manager::persist);
        }

        @Override
        public void removeAll() throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            manager.createQuery("SELECT recipe FROM Recipe recipe", RecipeImpl.class)
                   .getResultList()
                   .forEach(manager::remove);
        }
    }
}
