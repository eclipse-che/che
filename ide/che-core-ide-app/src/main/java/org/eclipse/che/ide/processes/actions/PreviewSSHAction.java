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
package org.eclipse.che.ide.processes.actions;

import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.MACHINE_NODE;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.ProcessTreeNode;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/** Action to show preview SSH panel. */
@Singleton
public class PreviewSSHAction extends BaseAction {

  private final Provider<ProcessesPanelPresenter> processesPanelPresenter;

  @Inject
  public PreviewSSHAction(
      MachineResources resources,
      CoreLocalizationConstant constant,
      Provider<ProcessesPanelPresenter> processesPanelPresenter) {
    super(
        constant.machineSSHActionTitle(),
        constant.machineSSHActionDescription(),
        resources.output());
    this.processesPanelPresenter = processesPanelPresenter;
  }

  @Override
  public void update(ActionEvent event) {
    ProcessTreeNode node = processesPanelPresenter.get().getContextTreeNode();
    event.getPresentation().setEnabledAndVisible(node != null && node.getType() == MACHINE_NODE);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ProcessTreeNode node = processesPanelPresenter.get().getContextTreeNode();

    if (node != null && node.getType() == MACHINE_NODE) {
      String machineName = (String) node.getData();
      processesPanelPresenter.get().onPreviewSsh(machineName);
    }
  }
}
