/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.toolbar.commands;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.command.goal.DebugGoal;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.toolbar.commands.button.ExecuteCommandButton;
import org.eclipse.che.ide.command.toolbar.commands.button.ExecuteCommandButtonFactory;
import org.eclipse.che.ide.command.toolbar.commands.button.ExecuteCommandButtonItemsProvider;
import org.eclipse.che.ide.ui.menubutton.MenuButton;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;

/**
 * Implementation of {@link ExecuteCommandView} uses {@link MenuButton}s for displaying commands
 * grouped by goals. Allows to choose command from the {@link MenuButton}'s dropdown menu.
 */
@Singleton
public class ExecuteCommandViewImpl implements ExecuteCommandView {

  /** Stores created buttons by goals. */
  private final Map<String, ExecuteCommandButton> goalButtons;

  private final FlowPanel buttonsPanel;

  private final ExecuteCommandButtonFactory buttonFactory;
  private final RunGoal runGoal;
  private final DebugGoal debugGoal;

  private ActionDelegate delegate;

  @Inject
  public ExecuteCommandViewImpl(
      ExecuteCommandButtonFactory buttonFactory, RunGoal runGoal, DebugGoal debugGoal) {
    this.buttonFactory = buttonFactory;
    this.runGoal = runGoal;
    this.debugGoal = debugGoal;

    goalButtons = new HashMap<>();
    buttonsPanel = new FlowPanel();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return buttonsPanel;
  }

  @Override
  public void setGoals(Set<CommandGoal> goals) {
    goals.forEach(this::createButton);
  }

  /** Creates {@link ExecuteCommandButton} for the given goal and adds button to the panel. */
  private void createButton(CommandGoal goal) {
    ExecuteCommandButton button = buttonFactory.newButton(goal, delegate, getKeyBinding(goal));
    goalButtons.put(goal.getId(), button);

    button.updateTooltip();

    buttonsPanel.add(button);
  }

  @Override
  public void addCommand(CommandImpl command) {
    final ExecuteCommandButton button = goalButtons.get(command.getGoal());

    if (button == null) {
      return;
    }

    ExecuteCommandButtonItemsProvider itemsProvider = button.getItemsProvider();
    itemsProvider.addCommand(command);

    button.updateTooltip();
  }

  @Override
  public void removeCommand(CommandImpl command) {
    final ExecuteCommandButton button = goalButtons.get(command.getGoal());

    if (button == null) {
      return;
    }

    ExecuteCommandButtonItemsProvider itemsProvider = button.getItemsProvider();
    itemsProvider.removeCommand(command);

    button.updateTooltip();
  }

  @Nullable
  private CharCodeWithModifiers getKeyBinding(CommandGoal goal) {
    if (goal.equals(runGoal)) {
      return new KeyBuilder().alt().charCode('r').build();
    } else if (goal.equals(debugGoal)) {
      return new KeyBuilder().alt().charCode('d').build();
    }

    return null;
  }
}
