/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.execute;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.resources.tree.ResourceNode;

/**
 * Action group that contains all actions for executing commands.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsActionGroup extends DefaultActionGroup {

  private final SelectionAgent selectionAgent;

  @Inject
  public CommandsActionGroup(
      ActionManager actionManager, SelectionAgent selectionAgent, ExecMessages messages) {
    super(messages.actionCommandsTitle(), true, actionManager);

    this.selectionAgent = selectionAgent;
  }

  @Override
  public void update(ActionEvent e) {
    e.getPresentation().setEnabledAndVisible(false);

    // action group should be visible when current selection is machine or project

    final Selection<?> selection = selectionAgent.getSelection();

    if (selection != null && !selection.isEmpty() && selection.isSingleSelection()) {
      final Object possibleNode = selection.getHeadElement();

      if (possibleNode instanceof Machine) {
        e.getPresentation().setEnabledAndVisible(true);
      } else if (possibleNode instanceof ResourceNode) {
        final Resource selectedResource = ((ResourceNode) possibleNode).getData();

        e.getPresentation().setEnabledAndVisible(selectedResource.isProject());
      }
    }
  }
}
