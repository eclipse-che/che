package org.eclipse.che.ide.command.toolbar.commands.button;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.ui.menubutton.PopupItem;

/** Factory for {@link PopupItem}s for {@link GoalButton}. */
public interface PopupItemFactory {

    GuidePopupItem newHintPopupItem();

    CommandPopupItem newCommandPopupItem(ContextualCommand command);

    MachinePopupItem newMachinePopupItem(ContextualCommand command, Machine machine);

    MachinePopupItem newMachinePopupItem(MachinePopupItem item);
}
