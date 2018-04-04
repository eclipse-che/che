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
