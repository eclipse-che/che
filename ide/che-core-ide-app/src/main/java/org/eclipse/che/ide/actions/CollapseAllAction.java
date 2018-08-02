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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Action to collapse all opened nodes in Project Explorer.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CollapseAllAction extends BaseAction implements ActivePartChangedHandler {

  private ProjectExplorerPresenter projectExplorer;
  private PartPresenter activePart;

  @Inject
  public CollapseAllAction(
      ProjectExplorerPresenter projectExplorer,
      CoreLocalizationConstant localizationConstant,
      EventBus eventBus) {
    super(
        localizationConstant.collapseAllActionTitle(),
        localizationConstant.collapseAllActionDescription());
    this.projectExplorer = projectExplorer;

    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    activePart = event.getActivePart();
  }

  @Override
  public void update(ActionEvent e) {
    if (!(activePart instanceof ProjectExplorerPresenter)) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }

    e.getPresentation().setEnabledAndVisible(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    projectExplorer.collapseAll();
  }
}
