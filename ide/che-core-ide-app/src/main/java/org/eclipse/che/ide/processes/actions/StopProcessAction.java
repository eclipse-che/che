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
package org.eclipse.che.ide.processes.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.console.CommandOutputConsolePresenter;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.ProcessTreeNode;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Stop selected process action.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class StopProcessAction extends AbstractPerspectiveAction {

  private ProcessesPanelPresenter processesPanelPresenter;

  @Inject
  public StopProcessAction(
      ProcessesPanelPresenter processesPanelPresenter,
      CoreLocalizationConstant locale,
      MachineResources machineResources) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.stopControlTitle(),
        locale.stopControlDescription(),
        machineResources.stopIcon());
    this.processesPanelPresenter = processesPanelPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    OutputConsole outputConsole = processesPanelPresenter.getContextOutputConsole();
    if (outputConsole != null && outputConsole instanceof CommandOutputConsolePresenter) {
      CommandOutputConsolePresenter commandOutputConsolePresenter =
          (CommandOutputConsolePresenter) outputConsole;
      commandOutputConsolePresenter.stopProcessButtonClicked();
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

    if (processesPanelPresenter.getContextOutputConsole() instanceof CommandOutputConsolePresenter
        && !processesPanelPresenter.getContextOutputConsole().isFinished()) {
      event.getPresentation().setEnabled(true);
      event.getPresentation().setVisible(true);
      return;
    }

    event.getPresentation().setEnabled(false);
    event.getPresentation().setVisible(false);
  }
}
