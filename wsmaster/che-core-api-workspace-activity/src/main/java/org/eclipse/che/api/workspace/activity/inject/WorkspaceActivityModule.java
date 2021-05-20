/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.activity.inject;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.workspace.activity.JpaWorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityChecker;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityManager;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityService;

public class WorkspaceActivityModule extends AbstractModule {

  @Override
  protected void configure() {
    // make sure any changes here are reflected in the MultiUserWorkspaceActivityModule
    // appropriately
    bind(WorkspaceActivityService.class);
    bind(WorkspaceActivityManager.class);
    bind(WorkspaceActivityChecker.class);
    bind(WorkspaceActivityDao.class).to(JpaWorkspaceActivityDao.class);
  }
}
