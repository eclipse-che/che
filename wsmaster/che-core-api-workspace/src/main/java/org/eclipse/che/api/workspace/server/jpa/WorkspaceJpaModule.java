/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.jpa;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao.RemoveWorkspaceBeforeAccountRemovedEventSubscriber;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;

/** @author Yevhenii Voevodin */
public class WorkspaceJpaModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(StackDao.class).to(JpaStackDao.class);
    bind(WorkspaceDao.class).to(JpaWorkspaceDao.class);
    bind(RemoveWorkspaceBeforeAccountRemovedEventSubscriber.class).asEagerSingleton();
  }
}
