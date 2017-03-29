/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.toolbar.commands.button;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.ui.menubutton.PopupItem;

/** Factory for {@link PopupItem}s for {@link GoalButton}. */
public interface PopupItemFactory {

    GuidePopupItem newHintPopupItem();

    CommandPopupItem newCommandPopupItem(CommandImpl command);

    MachinePopupItem newMachinePopupItem(CommandImpl command, Machine machine);

    MachinePopupItem newMachinePopupItem(MachinePopupItem item);
}
