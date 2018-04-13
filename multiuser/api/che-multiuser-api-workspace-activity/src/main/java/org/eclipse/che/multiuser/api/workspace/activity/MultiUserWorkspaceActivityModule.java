/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.workspace.activity;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.workspace.activity.JpaWorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityManager;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityService;

/**
 * Implementation of
 *
 * @author Mykhailo Kuznietsov
 */
public class MultiUserWorkspaceActivityModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(WorkspaceActivityService.class);
    bind(WorkspaceActivityDao.class).to(JpaWorkspaceActivityDao.class);
    bind(WorkspaceActivityManager.class).to(MultiUserWorkspaceActivityManager.class);
  }
}
