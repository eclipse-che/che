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
