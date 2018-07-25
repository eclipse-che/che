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
package org.eclipse.che.ide.actions.common;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartStack;

/**
 * Action to restore active part and corresponding part stack from maximized state.
 *
 * @author Vitaliy Guliy
 */
public class RestorePartAction extends BaseAction implements ActivePartChangedHandler {

  private PartStack activePartStack;

  @Inject
  public RestorePartAction(
      final EventBus eventBus, final CoreLocalizationConstant coreLocalizationConstant) {
    super(
        coreLocalizationConstant.actionRestorePartTitle(),
        coreLocalizationConstant.actionRestorePartDescription());
    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  @Override
  public void update(ActionEvent e) {
    if (activePartStack == null) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }

    e.getPresentation()
        .setEnabledAndVisible(PartStack.State.MAXIMIZED == activePartStack.getPartStackState());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    activePartStack.restore();
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    activePartStack = event.getActivePart().getPartStack();
  }
}
