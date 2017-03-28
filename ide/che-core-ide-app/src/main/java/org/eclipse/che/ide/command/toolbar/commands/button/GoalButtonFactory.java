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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.goal.DebugGoal;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;
import org.eclipse.che.ide.command.toolbar.commands.ExecuteCommandView.ActionDelegate;

/**
 * Factory for {@link GoalButton}s.
 *
 * @see GoalButton
 */
@Singleton
public class GoalButtonFactory {

    private final CommandResources resources;
    private final AppContext       appContext;
    private final PopupItemFactory popupItemFactory;
    private final ToolbarMessages  messages;
    private final RunGoal          runGoal;
    private final DebugGoal        debugGoal;

    @Inject
    public GoalButtonFactory(CommandResources resources,
                             AppContext appContext,
                             PopupItemFactory popupItemFactory,
                             ToolbarMessages messages,
                             RunGoal runGoal,
                             DebugGoal debugGoal) {
        this.resources = resources;
        this.appContext = appContext;
        this.popupItemFactory = popupItemFactory;
        this.messages = messages;
        this.runGoal = runGoal;
        this.debugGoal = debugGoal;
    }

    /**
     * Creates new instance of the {@link GoalButton}.
     *
     * @param goal
     *         {@link CommandGoal} for displaying commands
     * @param delegate
     *         delegate for receiving events
     * @return {@link GoalButton}
     */
    public GoalButton newButton(CommandGoal goal, ActionDelegate delegate) {
        final GoalButtonDataProvider dataProvider = new GoalButtonDataProvider(appContext, popupItemFactory);
        final GoalButton button = new GoalButton(goal, getIconForGoal(goal), dataProvider, messages);

        button.setActionHandler(item -> {
            if (item instanceof CommandPopupItem) {
                final CommandImpl command = ((CommandPopupItem)item).getCommand();

                delegate.onCommandExecute(command, null);
                dataProvider.setDefaultItem(item);
                button.updateTooltip();
            } else if (item instanceof MachinePopupItem) {
                final MachinePopupItem machinePopupItem = (MachinePopupItem)item;

                delegate.onCommandExecute(machinePopupItem.getCommand(), machinePopupItem.getMachine());
                dataProvider.setDefaultItem(item);
                button.updateTooltip();
            } else if (item instanceof GuidePopupItem) {
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
            safeHtmlBuilder.appendHtmlConstant(FontAwesome.PLAY);
        } else if (goal.equals(debugGoal)) {
            safeHtmlBuilder.appendHtmlConstant(FontAwesome.BUG);
        }

        return safeHtmlBuilder.toSafeHtml();
    }
}
