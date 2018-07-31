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

import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.MACHINE_NODE;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.DisplayMachineOutputEvent;
import org.eclipse.che.ide.processes.ProcessTreeNode;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Action to display machine output.
 *
 * @author Vlad Zhukovskyi
 * @since 6.0.0
 */
@Singleton
public class DisplayMachineOutputAction extends BaseAction {

  private final Provider<ProcessesPanelPresenter> processesPanelPresenter;
  private final EventBus eventBus;

  @Inject
  public DisplayMachineOutputAction(
      MachineResources resources,
      CoreLocalizationConstant constant,
      Provider<ProcessesPanelPresenter> processesPanelPresenter,
      EventBus eventBus) {
    super(
        constant.machineOutputActionTitle(),
        constant.machineOutputActionDescription(),
        resources.output());
    this.processesPanelPresenter = processesPanelPresenter;
    this.eventBus = eventBus;
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
      eventBus.fireEvent(new DisplayMachineOutputEvent(machineName));
    }
  }
}
