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
package org.eclipse.che.core.db;

import org.eclipse.che.core.db.jpa.JpaInitializer;
import org.eclipse.che.core.db.jpa.eclipselink.GuiceEntityListenerInjectionManager;
import org.eclipse.che.core.db.schema.SchemaInitializationException;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.persistence.sessions.server.ServerSession;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;

/**
 * Initializes database components.
 *
 * <p>Those components which require any persistence operations on their bootstrap
 * have to depend on this component. For example:
 * <pre>
 * class StackExistsChecker {
 *
 *     &#064;@Inject
 *     &#064;SuppressWarnings("unused")
 *     private DBInitializer dbInitializer;
 *
 *     &#064;PostConstruct
 *     public void check() {
 *         ....
 *     }
 * }
 * </pre>
 * In this way it is guaranteed that all database related components
 * will be appropriately initialized before {@code check} method is executed.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class DBInitializer {

    @Inject
    public DBInitializer(SchemaInitializer schemaInitializer, JpaInitializer jpaInitializer) throws SchemaInitializationException {
        // schema must be initialized before any other component that may interact with database
        schemaInitializer.init();

        // jpa initialization goes next
        jpaInitializer.init();
    }

    @Inject
    public void setUpInjectionManager(GuiceEntityListenerInjectionManager injManager, EntityManagerFactory emFactory) {
        final ServerSession session = emFactory.unwrap(ServerSession.class);
        session.setEntityListenerInjectionManager(injManager);
    }
}
