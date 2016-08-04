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
package org.eclipse.che.api.workspace.server.jpa;

import com.google.inject.TypeLiteral;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.Collection;

/**
 * @author Yevhenii Voevodin
 */
public class WorkspaceTckModule extends TckModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("main"));
        bind(JpaInitializer.class).asEagerSingleton();
        bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();
        bind(org.eclipse.che.api.core.h2.jdbc.jpa.eclipselink.H2ExceptionHandler.class);

        bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).toInstance(new JpaTckRepository<>(WorkspaceImpl.class));
        bind(new TypeLiteral<TckRepository<StackImpl>>() {}).to(StackTckRepository.class);

        bind(WorkspaceDao.class).to(JpaWorkspaceDao.class);
        bind(StackDao.class).to(JpaStackDao.class);
    }

    @Transactional
    public static class StackTckRepository implements TckRepository<StackImpl> {

        @Inject
        private Provider<EntityManager> managerProvider;

        @Override
        public void createAll(Collection<? extends StackImpl> entities) throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            entities.forEach(manager::persist);
        }

        @Override
        public void removeAll() throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            manager.createNamedQuery("Stack.getAll", StackImpl.class).getResultList().forEach(manager::remove);
        }
    }
}
