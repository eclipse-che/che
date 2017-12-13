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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Action to collapse all opened nodes in Project Explorer.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CollapseAllAction extends BaseAction {
  private ProjectExplorerPresenter projectExplorer;

  @Inject
  public CollapseAllAction(
      ProjectExplorerPresenter projectExplorer, CoreLocalizationConstant localizationConstant) {
    super(
        localizationConstant.collapseAllActionTitle(),
        localizationConstant.collapseAllActionDescription());
    this.projectExplorer = projectExplorer;
  }

  @Override
  public void update(ActionEvent e) {
    e.getPresentation().setEnabledAndVisible(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    projectExplorer.collapseAll();
  }
}
