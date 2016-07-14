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

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.TckRepository;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
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

        bind(new TypeLiteral<Set<UserImpl>>() {}).annotatedWith(Names.named("codenvy.local.infrastructure.users")).toInstance(emptySet());

        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).to(LocalUserTckRepository.class).in(Singleton.class);
        bind(new TypeLiteral<TckRepository<ProfileImpl>>() {}).to(LocalProfileTckRepository.class).in(Singleton.class);
        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).to(LocalRecipeTckRepository.class).in(Singleton.class);
        bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).to(LocalWorkspaceTckRepository.class).in(Singleton.class);

        bind(UserDao.class).to(LocalUserDaoImpl.class).in(Singleton.class);
        bind(ProfileDao.class).to(LocalProfileDaoImpl.class).in(Singleton.class);
        bind(RecipeDao.class).to(LocalRecipeDaoImpl.class).in(Singleton.class);
        bind(WorkspaceDao.class).to(LocalWorkspaceDaoImpl.class).in(Singleton.class);
    }
}
