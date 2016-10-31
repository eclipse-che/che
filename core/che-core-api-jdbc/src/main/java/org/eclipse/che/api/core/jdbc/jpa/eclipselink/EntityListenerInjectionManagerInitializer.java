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
package org.eclipse.che.api.core.jdbc.jpa.eclipselink;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.persistence.sessions.server.ServerSession;

import javax.persistence.EntityManagerFactory;

/**
 * Sets up {@link GuiceEntityListenerInjectionManager}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class EntityListenerInjectionManagerInitializer {

    @Inject
    public EntityListenerInjectionManagerInitializer(GuiceEntityListenerInjectionManager injManager, EntityManagerFactory emFactory) {
        final ServerSession session = emFactory.unwrap(ServerSession.class);
        session.setEntityListenerInjectionManager(injManager);
    }
}
