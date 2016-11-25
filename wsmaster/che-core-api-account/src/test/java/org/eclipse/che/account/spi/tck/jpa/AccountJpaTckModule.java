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
package org.eclipse.che.account.spi.tck.jpa;

import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.account.spi.jpa.JpaAccountDao;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;

import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;

/**
 * @author Sergii Leschenko
 * @author Yevhenii Voevodin
 */
public class AccountJpaTckModule extends TckModule {
    @Override
    protected void configure() {
        install(new JpaPersistModule("main"));
        bind(DBInitializer.class).asEagerSingleton();
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema"));
        bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);

        bind(new TypeLiteral<TckRepository<AccountImpl>>() {}).toInstance(new JpaTckRepository<>(AccountImpl.class));

        bind(AccountDao.class).to(JpaAccountDao.class);
    }
}
