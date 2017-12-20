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
package org.eclipse.che.ide.command.toolbar.commands;

import java.util.Set;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;

/** View for {@link ExecuteCommandPresenter}. */
public interface ExecuteCommandView extends View<ExecuteCommandView.ActionDelegate> {

  /** Set command goals which commands may be added to the view. */
  void setGoals(Set<CommandGoal> goals);

  /** Add command to the view. */
  void addCommand(CommandImpl command);

  /** Remove command from the view. */
  void removeCommand(CommandImpl command);

  interface ActionDelegate {

    /** Called when command execution has been requested. */
    void onCommandExecute(CommandImpl command);

    /** Called when command execution has been requested on the specified machine. */
    void onCommandExecute(CommandImpl command, MachineImpl machine);

    /** Called when guide of commands creation has been requested. */
    void onGuide(CommandGoal goal);
  }
}
