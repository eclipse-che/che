/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.toolbar.commands.button;

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;
import static org.eclipse.che.ide.util.input.KeyMapUtil.getShortcutText;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import elemental.html.DivElement;
import elemental.html.SpanElement;
import java.util.Optional;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menubutton.MenuButton;
import org.eclipse.che.ide.ui.menubutton.MenuItem;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;

/** {@link MenuButton} allows to chose a command for execution. */
public class ExecuteCommandButton extends MenuButton {

  private static final String ACTION_PREFIX = "execute_command_";

  private final CommandGoal goal;
  private final ToolbarMessages messages;
  private final CharCodeWithModifiers keyBinding;

  private Tooltip tooltip;
  private String tooltipText;

  ExecuteCommandButton(
      CommandGoal goal,
      SafeHtml icon,
      ExecuteCommandButtonItemsProvider itemsProvider,
      ToolbarMessages messages,
      ActionManager actionManager,
      KeyBindingAgent keyBindingAgent,
      @Nullable CharCodeWithModifiers keyBinding) {
    super(icon, itemsProvider);

    this.goal = goal;
    this.messages = messages;
    this.keyBinding = keyBinding;

    if (keyBinding != null) {
      actionManager.registerAction(ACTION_PREFIX + goal.getId(), new ExecuteDefaultCommandAction());
      keyBindingAgent.getGlobal().addKey(keyBinding, ACTION_PREFIX + goal.getId());
    }
  }

  public ExecuteCommandButtonItemsProvider getItemsProvider() {
    return (ExecuteCommandButtonItemsProvider) itemsProvider;
  }

  /** Updates button's tooltip depending on it's state (what child elements it contains). */
  public void updateTooltip() {
    final Optional<MenuItem> defaultItem = itemsProvider.getDefaultItem();

    if (defaultItem.isPresent()) {
      MenuItem menuItem = defaultItem.get();
      String message = "";
      if (menuItem instanceof CommandItem) {
        message = messages.goalButtonTooltipExecute(menuItem.getName());
      } else if (menuItem instanceof MachineItem) {
        MachineItem machineMenuItem = (MachineItem) menuItem;
        message =
            messages.goalButtonTooltipExecuteOnMachine(
                machineMenuItem.getCommand().getName(), machineMenuItem.getName());
      }

      setTooltip(message, keyBinding);
    } else if (getItemsProvider().containsGuideItemOnly()) {
      setTooltip(messages.goalButtonTooltipNoCommand(goal.getId()), null);
    } else {
      setTooltip(messages.goalButtonTooltipChooseCommand(goal.getId()), null);
    }
  }

  private void setTooltip(String newTooltipText, @Nullable CharCodeWithModifiers keyBinding) {
    if (newTooltipText.equals(tooltipText)) {
      return;
    }

    tooltipText = newTooltipText;

    if (tooltip != null) {
      tooltip.destroy();
    }

    final DivElement divElement = Elements.createDivElement();
    divElement.setInnerText(newTooltipText);

    if (keyBinding != null) {
      final String hotKey = getShortcutText(keyBinding);
      if (hotKey != null) {
        SpanElement spanElement = Elements.createSpanElement();
        spanElement.getStyle().setMarginLeft("5px");
        spanElement.getStyle().setColor("#aaaaaa");
        spanElement.setInnerText("[" + hotKey + "]");

        divElement.appendChild(spanElement);
      }
    }

    tooltip = Tooltip.create((Element) getElement(), BOTTOM, MIDDLE, divElement);
  }

  private class ExecuteDefaultCommandAction extends BaseAction {

    ExecuteDefaultCommandAction() {
      super("Execute default command of " + goal.getId() + " goal");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      itemsProvider
          .getDefaultItem()
          .ifPresent(
              defaultItem ->
                  getActionHandler().ifPresent(handler -> handler.onAction(defaultItem)));
    }
  }
}
