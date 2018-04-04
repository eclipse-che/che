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

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;

/** Item contains {@link CommandImpl} and {@link MachineImpl}. */
public class MachineItem extends AbstractMenuItem {

  private final MachineImpl machine;
  private final String name;

  @AssistedInject
  public MachineItem(@Assisted CommandImpl command, @Assisted MachineImpl machine) {
    super(command);

    this.machine = machine;
    this.name = machine.getName();
  }

  @AssistedInject
  public MachineItem(@Assisted MachineItem item) {
    super(item.getCommand());

    this.machine = item.machine;
    this.name = getCommand().getName() + " on " + machine.getName();
  }

  @Override
  public String getName() {
    return name;
  }

  public MachineImpl getMachine() {
    return machine;
  }
}
