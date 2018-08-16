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
package org.eclipse.che.ide.processes;

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
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
import org.eclipse.che.ide.terminal.options.TerminalOptionsJso;

/**
 * Action to open new terminal and navigate to selected directory. If on selected folder in Project
 * Explorer call this action MUST be opened new terminal with selected working dir.
 *
 * <p>bash command 'cd {selected-folder}'
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class OpenInTerminalAction extends AbstractPerspectiveAction {

  private CoreLocalizationConstant locale;
  private final ProcessesPanelPresenter processesPanelPresenter;

  interface Command extends SafeHtmlTemplates {

    @Template("cd {0} && clear")
    SafeHtml openInTerminalCommand(String path);
  }

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
    Command cmdTmpl = GWT.create(Command.class);
    String command = cmdTmpl.openInTerminalCommand(path.toString()).asString();
    processesPanelPresenter.newTerminal(TerminalOptionsJso.create().withCommand(command), true);
  }
}
