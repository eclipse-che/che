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
import org.eclipse.che.ide.api.action.ToggleAction;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Action to show / hide console tree.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class ShowConsoleTreeAction extends ToggleAction implements ActivePartChangedHandler {

  private final ProcessesPanelPresenter processesPanelPresenter;

  private PartPresenter activePart;

  @Inject
  public ShowConsoleTreeAction(
      final EventBus eventBus,
      final ProcessesPanelPresenter processesPanelPresenter,
      final CoreLocalizationConstant machineLocalizationConstant) {
    super(machineLocalizationConstant.actionShowConsoleTreeTitle());

    this.processesPanelPresenter = processesPanelPresenter;

    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  @Override
  public boolean isSelected(ActionEvent e) {
    return processesPanelPresenter.isProcessesTreeVisible();
  }

  @Override
  public void setSelected(ActionEvent e, boolean state) {
    processesPanelPresenter.setProcessesTreeVisible(state);
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    activePart = event.getActivePart();
  }

  @Override
  public void update(ActionEvent e) {
    e.getPresentation().setEnabledAndVisible(activePart instanceof ProcessesPanelPresenter);
  }
}
