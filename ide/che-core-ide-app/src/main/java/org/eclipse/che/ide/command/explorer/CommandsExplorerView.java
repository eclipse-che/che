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
package org.eclipse.che.ide.command.explorer;

import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * The view for {@link CommandsExplorerPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandsExplorerView extends View<CommandsExplorerView.ActionDelegate> {

  /**
   * Sets the commands to display in the view.
   *
   * @param commands commands grouped by its type
   */
  void setCommands(Map<CommandGoal, List<CommandImpl>> commands);

  /** Returns the currently selected command goal or {@code null} if none. */
  @Nullable
  CommandGoal getSelectedGoal();

  /** Returns the currently selected command or {@code null} if none. */
  @Nullable
  CommandImpl getSelectedCommand();

  /** Select the given {@code command}. */
  void selectCommand(CommandImpl command);

  /** The action delegate for this view. */
  interface ActionDelegate extends BaseActionDelegate {

    /** Called when adding new command is requested. */
    void onCommandAdd(int left, int top);

    /**
     * Called when duplicating command is requested.
     *
     * @param command command duplication of which is requested
     */
    void onCommandDuplicate(CommandImpl command);

    /**
     * Called when removing command is requested.
     *
     * @param command command removing of which is requested
     */
    void onCommandRemove(CommandImpl command);
  }
}
