/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.activity.inject;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.workspace.activity.JpaWorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.JpaWorkspaceActivityDao.RemoveExpirationBeforeWorkspaceRemovedEventSubscriber;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityManager;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityService;

public class WorkspaceActivityModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(WorkspaceActivityService.class);
    bind(WorkspaceActivityManager.class);
    bind(WorkspaceActivityDao.class).to(JpaWorkspaceActivityDao.class);
    bind(RemoveExpirationBeforeWorkspaceRemovedEventSubscriber.class).asEagerSingleton();
  }
}
