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

/**
 * Action for vertically splitting the given {@link SubPanel} on the two sub-panels.
 *
 * @author Artem Zatsarynnyi
 */
public class SplitVerticallyAction extends BaseAction {

  private final SubPanel subPanel;

  public SplitVerticallyAction(SubPanel subPanel) {
    super("Split Pane In Two Columns", "Split Pane In Two Columns");
    this.subPanel = subPanel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    subPanel.splitVertically();
  }
}
