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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.goal.DebugGoal;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;
import org.eclipse.che.ide.command.toolbar.commands.ExecuteCommandView.ActionDelegate;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;

/** Factory for {@link ExecuteCommandButton}s. */
@Singleton
public class ExecuteCommandButtonFactory {

  private final CommandResources resources;
  private final Resources ideResources;
  private final AppContext appContext;
  private final MenuItemsFactory menuItemsFactory;
  private final ActionManager actionManager;
  private final KeyBindingAgent keyBindingAgent;
  private final ToolbarMessages messages;
  private final RunGoal runGoal;
  private final DebugGoal debugGoal;

  @Inject
  public ExecuteCommandButtonFactory(
      CommandResources resources,
      AppContext appContext,
      MenuItemsFactory menuItemsFactory,
      ToolbarMessages messages,
      RunGoal runGoal,
      DebugGoal debugGoal,
      ActionManager actionManager,
      KeyBindingAgent keyBindingAgent,
      Resources ideResources) {
    this.resources = resources;
    this.appContext = appContext;
    this.menuItemsFactory = menuItemsFactory;
    this.messages = messages;
    this.runGoal = runGoal;
    this.debugGoal = debugGoal;
    this.actionManager = actionManager;
    this.keyBindingAgent = keyBindingAgent;
    this.ideResources = ideResources;
  }

  /**
   * Creates new instance of the {@link ExecuteCommandButton}.
   *
   * @param goal {@link CommandGoal} for displaying commands
   * @param delegate delegate for receiving events
   * @param keyBinding key binding for the button
   * @return {@link ExecuteCommandButton}
   */
  public ExecuteCommandButton newButton(
      CommandGoal goal, ActionDelegate delegate, @Nullable CharCodeWithModifiers keyBinding) {
    final ExecuteCommandButtonItemsProvider itemsProvider =
        new ExecuteCommandButtonItemsProvider(appContext, menuItemsFactory, goal);
    final ExecuteCommandButton button =
        new ExecuteCommandButton(
            goal,
            getIconForGoal(goal),
            itemsProvider,
            messages,
            actionManager,
            keyBindingAgent,
            keyBinding);

    button.setActionHandler(
        item -> {
          if (item instanceof CommandItem) {
            final CommandImpl command = ((CommandItem) item).getCommand();

            delegate.onCommandExecute(command);
            itemsProvider.setDefaultItem(item);
            button.updateTooltip();
          } else if (item instanceof MachineItem) {
            final MachineItem machinePopupItem = (MachineItem) item;

            delegate.onCommandExecute(machinePopupItem.getCommand(), machinePopupItem.getMachine());
            itemsProvider.setDefaultItem(item);
            button.updateTooltip();
          } else if (item instanceof GuideItem) {
            delegate.onGuide(goal);
          }
        });

    button.addStyleName(resources.commandToolbarCss().toolbarButton());

    button.ensureDebugId("command_toolbar-button_" + goal.getId());

    return button;
  }

  /** Returns {@link FontAwesome} icon for the given goal. */
  private SafeHtml getIconForGoal(CommandGoal goal) {
    final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();

    if (goal.equals(runGoal)) {
      safeHtmlBuilder.appendHtmlConstant(
          "<img src=\"" + ideResources.run().getSafeUri().asString() + "\">");
    } else if (goal.equals(debugGoal)) {
      safeHtmlBuilder.appendHtmlConstant(
          "<img src=\"" + ideResources.debug().getSafeUri().asString() + "\">");
    }

    return safeHtmlBuilder.toSafeHtml();
  }
}
