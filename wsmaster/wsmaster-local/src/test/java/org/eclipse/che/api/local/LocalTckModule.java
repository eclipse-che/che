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
package org.eclipse.che.api.local;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.local.storage.stack.StackLocalStorage;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.TckRepository;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yevhenii Voevodin
 */
public class LocalTckModule extends TckModule {

    @Override
    public void configure() {
        // configuring local storage to deal with mocks
        final LocalStorage storage = mock(LocalStorage.class);
        when(storage.loadMap(any())).thenReturn(Collections.emptyMap());
        final LocalStorageFactory factory = mock(LocalStorageFactory.class);
        try {
            when(factory.create(any())).thenReturn(storage);
        } catch (IOException x) {
            throw new RuntimeException(x.getMessage(), x);
        }
        bind(LocalStorageFactory.class).toInstance(factory);

        // Configure stack local storage to deal with mocks
        final StackLocalStorage stackStorage = mock(StackLocalStorage.class);
        when(stackStorage.loadMap()).thenReturn(Collections.emptyMap());
        bind(StackLocalStorage.class).toInstance(stackStorage);

        bind(new TypeLiteral<Set<UserImpl>>() {}).annotatedWith(Names.named("codenvy.local.infrastructure.users")).toInstance(emptySet());

        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).to(LocalUserTckRepository.class);
        bind(new TypeLiteral<TckRepository<ProfileImpl>>() {}).to(LocalProfileTckRepository.class);
        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).to(LocalRecipeTckRepository.class);
        bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).to(LocalWorkspaceTckRepository.class);
        bind(new TypeLiteral<TckRepository<Pair<String, Map<String, String>>>>() {}).to(LocalPreferenceTckRepository.class);
        bind(new TypeLiteral<TckRepository<StackImpl>>() {}).to(LocalStackTckRepository.class);
        bind(new TypeLiteral<TckRepository<SnapshotImpl>>() {}).to(SnapshotTckRepository.class);

        bind(UserDao.class).to(LocalUserDaoImpl.class);
        bind(ProfileDao.class).to(LocalProfileDaoImpl.class);
        bind(RecipeDao.class).to(LocalRecipeDaoImpl.class);
        bind(WorkspaceDao.class).to(LocalWorkspaceDaoImpl.class);
        bind(PreferenceDao.class).to(LocalPreferenceDaoImpl.class);
        bind(StackDao.class).to(LocalStackDaoImpl.class);
        bind(SnapshotDao.class).to(LocalSnapshotDaoImpl.class);
    }

    @Singleton
    private static class SnapshotTckRepository extends LocalTckRepository<SnapshotImpl> {
        @Inject
        public SnapshotTckRepository(LocalSnapshotDaoImpl snapshotDao) {
            super(snapshotDao.snapshots, SnapshotImpl::getId);
        }
    }

    @Singleton
    private static class LocalUserTckRepository extends LocalTckRepository<UserImpl> {
        @Inject
        public LocalUserTckRepository(LocalUserDaoImpl userDao) {
            super(userDao.users, UserImpl::getId);
        }
    }

    @Singleton
    private static class LocalProfileTckRepository extends LocalTckRepository<ProfileImpl> {
        @Inject
        public LocalProfileTckRepository(LocalProfileDaoImpl profileDao) {
            super(profileDao.profiles, ProfileImpl::getUserId);
        }
    }

    @Singleton
    private static class LocalRecipeTckRepository extends LocalTckRepository<RecipeImpl> {
        @Inject
        public LocalRecipeTckRepository(LocalRecipeDaoImpl recipeDao) {
            super(recipeDao.recipes, RecipeImpl::getId);
        }
    }

    @Singleton
    private static class LocalWorkspaceTckRepository extends LocalTckRepository<WorkspaceImpl> {
        @Inject
        public LocalWorkspaceTckRepository(LocalWorkspaceDaoImpl workspaceDao) {
            super(workspaceDao.workspaces, WorkspaceImpl::getId);
        }
    }

    @Singleton
    private static class LocalStackTckRepository extends LocalTckRepository<StackImpl> {
        @Inject
        public LocalStackTckRepository(LocalStackDaoImpl stackDao) {
            super(stackDao.stacks, StackImpl::getId);
        }
    }
}
