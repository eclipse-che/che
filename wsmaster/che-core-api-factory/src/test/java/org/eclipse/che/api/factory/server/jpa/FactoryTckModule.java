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
package org.eclipse.che.api.factory.server.jpa;

import com.google.inject.TypeLiteral;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.factory.server.FactoryImage;
import org.eclipse.che.api.factory.server.model.impl.ActionImpl;
import org.eclipse.che.api.factory.server.model.impl.AuthorImpl;
import org.eclipse.che.api.factory.server.model.impl.ButtonAttributesImpl;
import org.eclipse.che.api.factory.server.model.impl.ButtonImpl;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.model.impl.IdeImpl;
import org.eclipse.che.api.factory.server.model.impl.OnAppClosedImpl;
import org.eclipse.che.api.factory.server.model.impl.OnAppLoadedImpl;
import org.eclipse.che.api.factory.server.model.impl.OnProjectsLoadedImpl;
import org.eclipse.che.api.factory.server.model.impl.PoliciesImpl;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.db.PersistTestModuleBuilder;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.h2.jpa.eclipselink.H2ExceptionHandler;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.h2.Driver;

/**
 * Tck module for factory test.
 *
 * @author Yevhenii Voevodin
 */
public class FactoryTckModule extends TckModule {

    @Override
    protected void configure() {
        H2DBTestServer server = H2DBTestServer.startDefault();
        install(new PersistTestModuleBuilder().setDriver(Driver.class)
                                              .runningOn(server)
                                              .addEntityClasses(UserImpl.class,
                                                                AccountImpl.class,
                                                                FactoryImpl.class,
                                                                OnAppClosedImpl.class,
                                                                OnProjectsLoadedImpl.class,
                                                                OnAppLoadedImpl.class,
                                                                ActionImpl.class,
                                                                ButtonImpl.class,
                                                                IdeImpl.class,
                                                                WorkspaceConfigImpl.class,
                                                                ProjectConfigImpl.class,
                                                                EnvironmentImpl.class,
                                                                EnvironmentRecipeImpl.class,
                                                                ExtendedMachineImpl.class,
                                                                SourceStorageImpl.class,
                                                                ServerConf2Impl.class,
                                                                CommandImpl.class)
                                              .addEntityClass("org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl$Attribute")
                                              .setExceptionHandler(H2ExceptionHandler.class)
                                              .build());
        bind(DBInitializer.class).asEagerSingleton();
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
        bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server));

        bind(FactoryDao.class).to(JpaFactoryDao.class);

        bind(new TypeLiteral<TckRepository<FactoryImpl>>() {}).toInstance(new JpaTckRepository<>(FactoryImpl.class));
        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).toInstance(new JpaTckRepository<>(UserImpl.class));
    }
}
