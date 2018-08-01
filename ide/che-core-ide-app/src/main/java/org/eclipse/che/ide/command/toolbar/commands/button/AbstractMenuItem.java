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
package org.eclipse.che.ide.command.toolbar.commands.button;

import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.ui.menubutton.MenuItem;

/** An abstract {@link MenuItem} for {@link ExecuteCommandButton}s. */
public abstract class AbstractMenuItem implements MenuItem {

  private final CommandImpl command;

  protected AbstractMenuItem(CommandImpl command) {
    this.command = command;
  }

  public CommandImpl getCommand() {
    return command;
  }
}
