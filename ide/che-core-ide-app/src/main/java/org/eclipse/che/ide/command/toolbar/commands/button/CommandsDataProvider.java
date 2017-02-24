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
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.ui.menubutton.PopupItem;
import org.eclipse.che.ide.ui.menubutton.PopupItemDataProvider;
import org.eclipse.che.ide.util.Pair;

import java.util.ArrayList;
import java.util.List;

/** Provides items for {@link CommandsButton}. */
public class CommandsDataProvider implements PopupItemDataProvider {

    private final List<ContextualCommand> commands;
    private final AppContext              appContext;

    private ItemDataChangeHandler handler;
    private PopupItem             defaultItem;

    public CommandsDataProvider(AppContext appContext) {
        this.appContext = appContext;
        this.commands = new ArrayList<>();
    }

    @Nullable
    @Override
    public PopupItem getDefaultItem() {
        if (defaultItem != null) {
            return defaultItem;
        }

        return null;
    }

    public void setDefaultItem(PopupItem item) {
        defaultItem = item;
    }

    @Override
    public List<PopupItem> getItems() {
        List<PopupItem> result = new ArrayList<>(commands.size());

        if (defaultItem != null && defaultItem instanceof MachinePopupItem) {
            result.add(new MachinePopupItem((MachinePopupItem)defaultItem));
        }

        for (ContextualCommand command : commands) {
            result.add(new CommandPopupItem(command));
        }

        return result;
    }

    @Override
    public boolean isGroup(PopupItem popupItem) {
        if (popupItem instanceof CommandPopupItem) {
            return appContext.getWorkspace().getRuntime().getMachines().size() > 1;
        }

        return false;
    }

    @Override
    public Pair<List<PopupItem>, String> getChildren(PopupItem parent) {
        List<PopupItem> result = new ArrayList<>();

        if (parent instanceof CommandPopupItem) {
            final ContextualCommand command = ((CommandPopupItem)parent).getCommand();

            for (Machine machine : appContext.getWorkspace().getRuntime().getMachines()) {
                result.add(new MachinePopupItem(command, machine));
            }
        }

        return Pair.of(result, null);
    }

    @Override
    public void setItemDataChangedHandler(ItemDataChangeHandler handler) {
        this.handler = handler;
    }

    public void setCommands(List<ContextualCommand> commands) {
        this.commands.clear();
        this.commands.addAll(commands);

        handler.onItemDataChanged();
    }
}
