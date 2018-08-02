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
package org.eclipse.che.ide.workspace;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.app.AppContext;

/**
 * Action to start workspace.
 *
 * @author Vitaliy Gulyy
 */
public class StartWorkspaceAction extends BaseAction {

  private final Provider<CurrentWorkspaceManager> currentWorkspaceManagerProvider;
  private final AppContext appContext;

  @Inject
  public StartWorkspaceAction(
      CoreLocalizationConstant locale,
      AppContext appContext,
      Provider<CurrentWorkspaceManager> currentWorkspaceManagerProvider) {
    super(locale.startWsTitle(), locale.startWsDescription());
    this.appContext = appContext;
    this.currentWorkspaceManagerProvider = currentWorkspaceManagerProvider;
  }

  @Override
  public void update(ActionEvent e) {
    e.getPresentation()
        .setEnabledAndVisible(WorkspaceStatus.STOPPED == appContext.getWorkspace().getStatus());
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    currentWorkspaceManagerProvider.get().startWorkspace();
  }
}
