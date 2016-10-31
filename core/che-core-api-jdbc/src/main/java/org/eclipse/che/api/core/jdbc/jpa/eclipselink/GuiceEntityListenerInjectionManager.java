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
import com.google.inject.Injector;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.cdi.EntityListenerInjectionManager;

import javax.naming.NamingException;

/**
 * Allows to use dependency injection in entity listeners.
 *
 * <p>Example:
 * <pre>
 * class WorkspaceEntityListener {
 *
 *      &#064;Inject EventBus bus; <- EventBus will be injected by Guice
 *
 *      &#064;PreRemove
 *      public void preRemove(Workspace workspace) {
 *          bus.post(new BeforeWorkspaceRemovedEvent(workspace));
 *      }
 * }
 *
 * &#064;Entity
 * &#064;EntityListeners(WorkspaceEntityListener.class)
 * class Workspace {
 *      // ...
 * }
 * </pre>
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class GuiceEntityListenerInjectionManager implements EntityListenerInjectionManager {

    @Inject
    private JpaInitializer jpaInitializer;

    @Inject
    private Injector injector;

    @Override
    public Object createEntityListenerAndInjectDependancies(Class entityListenerClass) throws NamingException {
        try {
            return injector.getInstance(entityListenerClass);
        } catch (RuntimeException x) {
            throw new NamingException(x.getLocalizedMessage());
        }
    }

    @Override
    public void cleanUp(AbstractSession session) {
        // EntityListener objects are managed by Guice, nothing to cleanup
    }
}
