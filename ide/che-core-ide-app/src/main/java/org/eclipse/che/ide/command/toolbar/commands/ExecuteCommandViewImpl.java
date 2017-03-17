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
package org.eclipse.che.ide.command.toolbar.commands;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.command.goal.DebugGoal;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.toolbar.commands.button.GoalButton;
import org.eclipse.che.ide.command.toolbar.commands.button.GoalButtonDataProvider;
import org.eclipse.che.ide.command.toolbar.commands.button.GoalButtonFactory;
import org.eclipse.che.ide.ui.menubutton.MenuPopupButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * Implementation of {@link ExecuteCommandView} uses {@link MenuPopupButton}s
 * for displaying commands grouped by goal.
 * Allows to execute command by choosing one from the button's dropdown menu.
 */
@Singleton
public class ExecuteCommandViewImpl implements ExecuteCommandView {

    private final Map<CommandGoal, List<CommandImpl>> commands;
    /** Stores created buttons by goals in order to reuse it. */
    private final Map<CommandGoal, GoalButton>        buttonsCache;

    private final FlowPanel         buttonsPanel;
    private final RunGoal           runGoal;
    private final DebugGoal         debugGoal;
    private final GoalButtonFactory goalButtonFactory;

    private ActionDelegate delegate;

    @Inject
    public ExecuteCommandViewImpl(RunGoal runGoal, DebugGoal debugGoal, GoalButtonFactory goalButtonFactory) {
        this.runGoal = runGoal;
        this.debugGoal = debugGoal;
        this.goalButtonFactory = goalButtonFactory;

        commands = new HashMap<>();
        buttonsCache = new HashMap<>();
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
    public void setCommands(Map<CommandGoal, List<CommandImpl>> commands) {
        this.commands.clear();
        this.commands.putAll(commands);

        buttonsCache.clear();
        buttonsPanel.clear();

        createOrUpdateButtons();
    }

    /** Add buttons with commands to panel. */
    private void createOrUpdateButtons() {
        // for now, display commands of Run and Debug goals only
        List<CommandGoal> goals = new ArrayList<>();
        goals.add(runGoal);
        goals.add(debugGoal);

        goals.forEach(this::createOrUpdateButton);
    }

    /** Add button with commands of the given goal to panel. */
    private void createOrUpdateButton(CommandGoal goal) {
        final GoalButton button = buttonsCache.getOrDefault(goal, goalButtonFactory.newButton(goal, delegate));
        buttonsCache.put(goal, button);

        final List<CommandImpl> commandsOfGoal = commands.getOrDefault(goal, emptyList());
        final GoalButtonDataProvider dataProvider = button.getPopupItemDataProvider();

        dataProvider.setCommands(commandsOfGoal);

        button.updateTooltip();

        buttonsPanel.add(button);
    }
}
