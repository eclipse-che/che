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
package org.eclipse.che.core.db.jpa.eclipselink;

import com.google.inject.Inject;
import com.google.inject.Injector;
import javax.naming.NamingException;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.cdi.InjectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to use dependency injection in entity listeners.
 *
 * <p>Example:
 *
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
public class GuiceEntityListenerInjectionManager implements InjectionManager {

  private static final Logger LOG =
      LoggerFactory.getLogger(GuiceEntityListenerInjectionManager.class);

  @Inject private Injector injector;

  @Override
  public Object createManagedBeanAndInjectDependencies(Class entityListenerClass)
      throws NamingException {
    try {
      return injector.getInstance(entityListenerClass);
    } catch (RuntimeException x) {
      LOG.error(x.getLocalizedMessage(), x);
      throw new NamingException(x.getLocalizedMessage());
    }
  }

  @Override
  public void cleanUp(AbstractSession session) {
    // EntityListener objects are managed by Guice, nothing to cleanup
  }
}
