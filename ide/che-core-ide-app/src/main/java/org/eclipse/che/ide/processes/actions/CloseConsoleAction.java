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
package org.eclipse.che.ide.processes.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.processes.ProcessTreeNode;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Stop selected process and close the console action.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class CloseConsoleAction extends AbstractPerspectiveAction {

  private final ProcessesPanelPresenter processesPanelPresenter;

  @Inject
  public CloseConsoleAction(
      ProcessesPanelPresenter processesPanelPresenter,
      CoreLocalizationConstant locale,
      PartStackUIResources partStackUIResources) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.closeControlTitle(),
        locale.closeControlDescription(),
        partStackUIResources.closeIcon());
    this.processesPanelPresenter = processesPanelPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (processesPanelPresenter.getContextTreeNode() == null) {
      return;
    }

    if (ProcessTreeNode.ProcessNodeType.COMMAND_NODE
        == processesPanelPresenter.getContextTreeNode().getType()) {
      processesPanelPresenter.onCloseCommandOutputClick(
          processesPanelPresenter.getContextTreeNode());
    } else if (ProcessTreeNode.ProcessNodeType.TERMINAL_NODE
        == processesPanelPresenter.getContextTreeNode().getType()) {
      processesPanelPresenter.onCloseTerminal(processesPanelPresenter.getContextTreeNode());
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    ProcessTreeNode processTreeNode = processesPanelPresenter.getContextTreeNode();

    if (processTreeNode == null) {
      event.getPresentation().setEnabled(false);
      event.getPresentation().setVisible(false);
      return;
    }

    if (ProcessTreeNode.ProcessNodeType.COMMAND_NODE == processTreeNode.getType()
        || ProcessTreeNode.ProcessNodeType.TERMINAL_NODE == processTreeNode.getType()) {
      event.getPresentation().setEnabled(true);
      event.getPresentation().setVisible(true);
      return;
    }

    event.getPresentation().setEnabled(false);
    event.getPresentation().setVisible(false);
  }
}
