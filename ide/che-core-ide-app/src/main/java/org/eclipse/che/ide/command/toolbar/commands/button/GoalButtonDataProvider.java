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
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.ui.menubutton.MenuPopupItemDataProvider;
import org.eclipse.che.ide.ui.menubutton.PopupItem;
import org.eclipse.che.ide.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/** Provides items for {@link GoalButton}. */
public class GoalButtonDataProvider implements MenuPopupItemDataProvider {

    private final List<CommandImpl> commands;
    private final AppContext        appContext;
    private final PopupItemFactory  popupItemFactory;

    private ItemDataChangeHandler handler;
    private PopupItem             defaultItem;

    public GoalButtonDataProvider(AppContext appContext, PopupItemFactory popupItemFactory) {
        this.appContext = appContext;
        this.popupItemFactory = popupItemFactory;
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
            items.add(popupItemFactory.newMachinePopupItem((MachinePopupItem)defaultItem));
        }

        for (CommandImpl command : commands) {
            items.add(popupItemFactory.newCommandPopupItem(command));
        }

        if (items.isEmpty()) {
            items.add(popupItemFactory.newHintPopupItem());
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
            CommandImpl command = ((CommandPopupItem)parent).getCommand();
            List<MachineEntity> machines = appContext.getActiveRuntime().getMachines();

            items.addAll(machines.stream()
                                 .map(machine -> popupItemFactory.newMachinePopupItem(command, machine))
                                 .collect(toList()));
        }

        return Pair.of(items, null);
    }

    @Override
    public void setItemDataChangedHandler(ItemDataChangeHandler handler) {
        this.handler = handler;
    }

    public void setCommands(List<CommandImpl> commands) {
        this.commands.clear();
        this.commands.addAll(commands);

        handler.onItemDataChanged();
    }

    /** Checks whether the {@link GuidePopupItem} is the only item. */
    public boolean hasGuideOnly() {
        List<PopupItem> items = getItems();
        return items.isEmpty() || (items.size() == 1 && items.get(0) instanceof GuidePopupItem);
    }
}
