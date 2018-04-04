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

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.ui.menubutton.ItemsProvider;
import org.eclipse.che.ide.ui.menubutton.MenuItem;
import org.eclipse.che.ide.util.Pair;

/** Provides items for {@link ExecuteCommandButton}. */
public class ExecuteCommandButtonItemsProvider implements ItemsProvider {

  private final AppContext appContext;
  private final MenuItemsFactory menuItemsFactory;
  private final CommandGoal goal;
  private final Set<CommandImpl> commands;

  private DataChangedHandler dataChangedHandler;
  private MenuItem defaultItem;

  ExecuteCommandButtonItemsProvider(
      AppContext appContext, MenuItemsFactory menuItemsFactory, CommandGoal goal) {
    this.appContext = appContext;
    this.menuItemsFactory = menuItemsFactory;
    this.goal = goal;
    this.commands = new HashSet<>();
  }

  @Override
  public Optional<MenuItem> getDefaultItem() {
    return ofNullable(defaultItem);
  }

  void setDefaultItem(MenuItem item) {
    defaultItem = item;
  }

  @Override
  public List<MenuItem> getItems() {
    final List<MenuItem> items = new ArrayList<>(commands.size());

    if (defaultItem != null && defaultItem instanceof MachineItem) {
      items.add(menuItemsFactory.newMachineItem((MachineItem) defaultItem));
    }

    for (CommandImpl command : commands) {
      items.add(menuItemsFactory.newCommandItem(command));
    }

    if (items.isEmpty()) {
      items.add(menuItemsFactory.newGuideItem(goal));
    }

    return items;
  }

  @Override
  public boolean isGroup(MenuItem item) {
    if (item instanceof CommandItem) {
      final WorkspaceImpl workspace = appContext.getWorkspace();
      final RuntimeImpl runtime = workspace.getRuntime();

      if (runtime != null) {
        return runtime.getMachines().size() > 1;
      }
    }

    return false;
  }

  @Override
  public Pair<List<MenuItem>, String> getChildren(MenuItem parent) {
    List<MenuItem> items = new ArrayList<>();

    if (parent instanceof CommandItem) {
      final WorkspaceImpl workspace = appContext.getWorkspace();
      final RuntimeImpl runtime = workspace.getRuntime();

      if (runtime != null) {
        final CommandImpl command = ((CommandItem) parent).getCommand();
        final Map<String, ? extends MachineImpl> machines = runtime.getMachines();

        items.addAll(
            machines
                .values()
                .stream()
                .map(machine -> menuItemsFactory.newMachineItem(command, machine))
                .collect(toList()));
      }
    }

    return Pair.of(items, null);
  }

  @Override
  public void setDataChangedHandler(DataChangedHandler handler) {
    dataChangedHandler = handler;
  }

  /** Adds the given {@code command} to the provider. */
  public void addCommand(CommandImpl command) {
    commands.add(command);

    if (dataChangedHandler != null) {
      dataChangedHandler.onDataChanged();
    }
  }

  /** Removes the given {@code command} from the provider. */
  public void removeCommand(CommandImpl command) {
    commands.remove(command);

    // reset the defaultItem
    if (defaultItem instanceof CommandItem) {
      final CommandImpl cmd = ((CommandItem) defaultItem).getCommand();
      if (cmd.equals(command)) {
        defaultItem = null;
      }
    }

    if (dataChangedHandler != null) {
      dataChangedHandler.onDataChanged();
    }
  }

  /** Checks whether the {@link GuideItem} is the only item which provider contains. */
  boolean containsGuideItemOnly() {
    List<MenuItem> items = getItems();

    return items.isEmpty() || (items.size() == 1 && items.get(0) instanceof GuideItem);
  }
}
