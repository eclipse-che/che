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

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.ui.menubutton.PopupItem;
import org.eclipse.che.ide.ui.menubutton.MenuPopupItemDataProvider;
import org.eclipse.che.ide.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/** Provides items for {@link CommandsButton}. */
public class CommandsDataProvider implements MenuPopupItemDataProvider {

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
        List<PopupItem> items = new ArrayList<>(commands.size());

        if (defaultItem != null && defaultItem instanceof MachinePopupItem) {
            items.add(new MachinePopupItem((MachinePopupItem)defaultItem));
        }

        for (ContextualCommand command : commands) {
            items.add(new CommandPopupItem(command));
        }

        return items;
    }

    @Override
    public boolean isGroup(PopupItem item) {
        if (item instanceof CommandPopupItem) {
            return appContext.getWorkspace().getRuntime().getMachines().size() > 1;
        }

        return false;
    }

    @Override
    public Pair<List<PopupItem>, String> getChildren(PopupItem parent) {
        List<PopupItem> items = new ArrayList<>();

        if (parent instanceof CommandPopupItem) {
            ContextualCommand command = ((CommandPopupItem)parent).getCommand();
            List<MachineEntity> machines = appContext.getActiveRuntime().getMachines();

            items.addAll(machines.stream()
                                 .map(machine -> new MachinePopupItem(command, machine))
                                 .collect(toList()));
        }

        return Pair.of(items, null);
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
