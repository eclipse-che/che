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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.ui.menubutton.MenuItem;

/** A {@link MenuItem} represents {@link CommandImpl}. */
public class CommandItem extends AbstractMenuItem {

  @Inject
  public CommandItem(@Assisted CommandImpl command) {
    super(command);
  }

  @Override
  public String getName() {
    return getCommand().getName();
  }
}
