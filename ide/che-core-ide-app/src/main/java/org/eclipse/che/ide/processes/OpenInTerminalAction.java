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

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
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
 * Action to open new terminal and navigate to selected directory.
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
      ProcessesPanelPresenter processesPanelPresenter,
      EventBus eventBus) {
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

    if (!(resource instanceof Container)) {
      final Container parent = resource.getParent();

      checkState(parent != null, "Parent should be a container");

      resource = parent;
    }

    Path path = resource.getLocation().makeRelative();

    String command = locale.openInTerminalCommand(path.toString());
    processesPanelPresenter.newTerminal(
        TerminalOptionsJso.createDefault().withCommand(command).withFocusOnOpen(true));
  }
}
