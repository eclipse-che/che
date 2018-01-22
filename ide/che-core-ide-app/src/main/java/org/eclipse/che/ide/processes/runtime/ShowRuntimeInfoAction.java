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
package org.eclipse.che.ide.processes.runtime;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.MACHINE_NODE;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.ProcessTreeNode;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Activates server list part on the UI.
 *
 * @author Vlad Zhukovskyi
 * @since 5.18.0
 */
@Singleton
public class ShowRuntimeInfoAction extends AbstractPerspectiveAction {

  private final Provider<ProcessesPanelPresenter> processesPanelPresenter;

  @Inject
  public ShowRuntimeInfoAction(
      Provider<ProcessesPanelPresenter> processesPanelPresenter,
      MachineResources resources,
      RuntimeInfoLocalization locale) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.showInfoActionTitle(),
        locale.showInfoActionDescription(),
        resources.remote());
    this.processesPanelPresenter = processesPanelPresenter;
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    ProcessTreeNode node = processesPanelPresenter.get().getContextTreeNode();

    event.getPresentation().setEnabledAndVisible(node != null && node.getType() == MACHINE_NODE);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ProcessTreeNode node = processesPanelPresenter.get().getContextTreeNode();

    if (node != null && node.getType() == MACHINE_NODE) {
      String machineName = (String) node.getData();
      processesPanelPresenter.get().onPreviewServers(machineName);
    }
  }
}
