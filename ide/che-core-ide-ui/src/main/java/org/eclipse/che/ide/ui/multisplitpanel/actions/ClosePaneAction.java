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
package org.eclipse.che.ide.ui.multisplitpanel.actions;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.ui.multisplitpanel.WidgetToShow;

/**
 * Action for removing all widgets in the given {@link SubPanel} and closing the panel.
 *
 * @author Artem Zatsarynnyi
 */
public class ClosePaneAction extends BaseAction {

  private final SubPanel subPanel;

  public ClosePaneAction(SubPanel subPanel) {
    super("Close Pane", "Close Pane");
    this.subPanel = subPanel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    for (WidgetToShow widget : subPanel.getAllWidgets()) {
      subPanel.removeWidget(widget);
    }

    subPanel.closePane();
  }
}
