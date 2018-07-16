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
package org.eclipse.che.ide.processes;

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.terminal.TerminalOptionsJso;

/**
 * Action to open new terminal and navigate to selected directory. E.g. if in Project Explorer will
 * be selected 'project1/src/main/java' and action will be called MUST be opened new terminal with
 * working dir '{workspace-root-folder}/project1/src/main/java'. (bash command cd
 * project1/src/main/java)
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class OpenInTerminalAction extends AbstractPerspectiveAction {

  private CoreLocalizationConstant locale;
  private final ProcessesPanelPresenter processesPanelPresenter;

  @Inject
  public OpenInTerminalAction(
      CoreLocalizationConstant locale,
      MachineResources machineResources,
      ProcessesPanelPresenter processesPanelPresenter) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        locale.openInTerminalAction(),
        null,
        machineResources.addTerminalIcon());
    this.locale = locale;

    this.processesPanelPresenter = processesPanelPresenter;
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    event.getPresentation().setEnabled(true);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    Resource resource = appContext.get().getResource();
    if (resource.isFile()) {
      final Container parent = resource.getParent();
      resource = parent;
    }
    Path path = resource.getLocation().makeRelative();
    String command = locale.openInTerminalCommand(path.toString());
    processesPanelPresenter.newTerminal(
        TerminalOptionsJso.createDefault().withCommand(command).withFocusOnOpen(true));
  }
}
