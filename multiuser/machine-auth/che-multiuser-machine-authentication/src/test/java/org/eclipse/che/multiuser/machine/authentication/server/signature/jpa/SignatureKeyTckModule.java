/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server.signature.jpa;

import com.google.inject.TypeLiteral;
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
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.spi.SignatureKeyDao;

/** @author Anton Korneta */
public class SignatureKeyTckModule extends TckModule {

  @Override
  protected void configure() {
    H2DBTestServer server = H2DBTestServer.startDefault();
    install(
        new PersistTestModuleBuilder()
            .setDriver("org.h2.Driver")
            .runningOn(server)
            .addEntityClasses(SignatureKeyImpl.class, SignatureKeyPairImpl.class)
            .setExceptionHandler(H2ExceptionHandler.class)
            .build());

    bind(DBInitializer.class).asEagerSingleton();
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
    bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server));

    bind(SignatureKeyDao.class).to(JpaSignatureKeyDao.class);
    bind(new TypeLiteral<TckRepository<SignatureKeyPairImpl>>() {})
        .toInstance(new JpaTckRepository<>(SignatureKeyPairImpl.class));
  }
}
