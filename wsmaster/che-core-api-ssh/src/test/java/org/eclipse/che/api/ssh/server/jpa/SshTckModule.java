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

import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.db.H2TestHelper;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;

/**
 * @author Mihail Kuznyetsov
 * @author Yevhenii Voevodin
 */
public class SshTckModule extends TckModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("main"));
        bind(DBInitializer.class).asEagerSingleton();
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(H2TestHelper.inMemoryDefault(), "che-schema"));
        bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);

        bind(SshDao.class).to(JpaSshDao.class);
        bind(new TypeLiteral<TckRepository<SshPairImpl>>(){}).toInstance(new JpaTckRepository<>(SshPairImpl.class));
        bind(new TypeLiteral<TckRepository<UserImpl>>(){}).toInstance(new JpaTckRepository<>(UserImpl.class));
    }
}
