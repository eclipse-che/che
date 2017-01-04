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
package org.eclipse.che.api.local;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;

public class LocalInfrastructureModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UserDao.class).to(LocalUserDaoImpl.class);
        bind(WorkspaceDao.class).to(LocalWorkspaceDaoImpl.class);
        bind(ProfileDao.class).to(LocalProfileDaoImpl.class);
        bind(PreferenceDao.class).to(LocalPreferenceDaoImpl.class);
        bind(SnapshotDao.class).to(LocalSnapshotDaoImpl.class);
        bind(SshDao.class).to(LocalSshDaoImpl.class);
        bind(TokenValidator.class).to(DummyTokenValidator.class);
        bind(RecipeDao.class).to(LocalRecipeDaoImpl.class);
        bind(StackDao.class).to(LocalStackDaoImpl.class);
        bind(LocalStorageFactory.class);
    }
}
