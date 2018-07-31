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
package org.eclipse.che.ide.ext.git.client.action;

import javax.inject.Inject;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.ext.git.client.panel.GitPanelPresenter;

/** @author Mykola Morhun */
public class ToggleGitPanelAction extends BaseAction {
  private final GitPanelPresenter gitPanelPresenter;

  @Inject
  public ToggleGitPanelAction(GitPanelPresenter gitPanelPresenter) {
    this.gitPanelPresenter = gitPanelPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO uncomment to enable shortcut to show git panel
    // if (gitPanelPresenter.isOpened()) {
    //   gitPanelPresenter.hide();
    // } else {
    //   gitPanelPresenter.show();
    // }
  }
}
