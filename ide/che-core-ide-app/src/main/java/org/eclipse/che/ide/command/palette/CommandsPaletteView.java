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
package org.eclipse.che.ide.command.palette;

import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view for {@link CommandsPalettePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandsPaletteView extends View<CommandsPaletteView.ActionDelegate> {

  /** Show the view. */
  void show();

  /** Close the view. */
  void close();

  /**
   * Sets the commands to display in the view.
   *
   * @param commands commands grouped by type
   */
  void setCommands(Map<CommandGoal, List<CommandImpl>> commands);

  /** The action delegate for this view. */
  interface ActionDelegate {

    /** Called when filtering commands is requested. */
    void onFilterChanged(String filterValue);

    /** Called when command execution is requested. */
    void onCommandExecute(CommandImpl command);
  }
}
