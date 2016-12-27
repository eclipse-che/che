/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.explorer;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandManager.CommandChangedListener;
import org.eclipse.che.ide.api.command.CommandManager.CommandLoadedListener;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.command.type.CommandTypeChooser;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;

/**
 * Presenter for Commands Explorer.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsExplorerPresenter extends BasePresenter implements CommandsExplorerView.ActionDelegate,
                                                                        Component,
                                                                        CommandChangedListener,
                                                                        CommandLoadedListener {

    private final CommandsExplorerView view;
    private final CommandResources     resources;
    private final WorkspaceAgent       workspaceAgent;
    private final CommandManager       commandManager;
    private final NotificationManager  notificationManager;
    private final CommandTypeChooser   commandTypeChooser;
    private final ExplorerMessages     messages;
    private final RefreshViewTask      refreshViewTask;

    @Inject
    public CommandsExplorerPresenter(CommandsExplorerView view,
                                     CommandResources commandResources,
                                     WorkspaceAgent workspaceAgent,
                                     CommandManager commandManager,
                                     NotificationManager notificationManager,
                                     CommandTypeChooser commandTypeChooser,
                                     ExplorerMessages messages,
                                     RefreshViewTask refreshViewTask) {
        this.view = view;
        this.resources = commandResources;
        this.workspaceAgent = workspaceAgent;
        this.commandManager = commandManager;
        this.notificationManager = notificationManager;
        this.commandTypeChooser = commandTypeChooser;
        this.messages = messages;
        this.refreshViewTask = refreshViewTask;

        view.setDelegate(this);
    }

    @Override
    public void start(Callback<Component, Exception> callback) {
        workspaceAgent.openPart(this, NAVIGATION, Constraints.LAST);
        workspaceAgent.setActivePart(this);

        commandManager.addCommandLoadedListener(this);
        commandManager.addCommandChangedListener(this);

        callback.onSuccess(this);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        refreshView();

        container.setWidget(getView());
    }

    @Override
    public String getTitle() {
        return messages.explorerPartTitle();
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return messages.explorerPartTooltip();
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return resources.explorerPart();
    }

    @Override
    public void onCommandAdd(int left, int top) {
        // by default, command should be applicable to the workspace only
        final ApplicableContext defaultApplicableContext = new ApplicableContext();
        defaultApplicableContext.setWorkspaceApplicable(true);

        commandTypeChooser.show(left, top).then(new Operation<CommandType>() {
            @Override
            public void apply(CommandType selectedCommandType) throws OperationException {
                final CommandGoal selectedGoal = view.getSelectedGoal();

                if (selectedGoal != null) {
                    commandManager.createCommand(selectedGoal.getId(),
                                                 selectedCommandType.getId(),
                                                 defaultApplicableContext)
                                  .catchError(new Operation<PromiseError>() {
                                      @Override
                                      public void apply(PromiseError arg) throws OperationException {
                                          notificationManager.notify(messages.explorerMessageUnableCreate(),
                                                                     arg.getMessage(),
                                                                     FAIL,
                                                                     EMERGE_MODE);
                                      }
                                  });
                }
            }
        });
    }

    @Override
    public void onCommandDuplicate(ContextualCommand command) {
        commandManager.createCommand(command).then(new Operation<ContextualCommand>() {
            @Override
            public void apply(ContextualCommand arg) throws OperationException {
                view.selectCommand(arg);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(messages.explorerMessageUnableDuplicate(),
                                           arg.getMessage(),
                                           FAIL,
                                           EMERGE_MODE);
            }
        });
    }

    @Override
    public void onCommandRemove(ContextualCommand command) {
        commandManager.removeCommand(command.getName()).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(messages.explorerMessageUnableRemove(),
                                           arg.getMessage(),
                                           FAIL,
                                           EMERGE_MODE);
            }
        });
    }

    @Override
    public void onCommandsLoaded() {
        refreshView();
    }

    @Override
    public void onCommandAdded(ContextualCommand command) {
        refreshViewAndSelectCommand(command);
    }

    @Override
    public void onCommandUpdated(ContextualCommand command) {
        refreshView();
    }

    @Override
    public void onCommandRemoved(ContextualCommand command) {
        refreshView();
    }

    private void refreshView() {
        refreshViewTask.delayAndSelectCommand(null);
    }

    private void refreshViewAndSelectCommand(ContextualCommand command) {
        refreshViewTask.delayAndSelectCommand(command);
    }

    /**
     * {@link DelayedTask} for refreshing the view and optionally selecting the specified command.
     * <p>Tree widget in the view works asynchronously using events
     * and it needs some time to be fully rendered.
     * So successive refreshing view must be called with some delay.
     */
    // since GIN can't instantiate inner classes
    // made it nested in order to allow injection
    @VisibleForTesting
    static class RefreshViewTask extends DelayedTask {

        // delay determined experimentally
        private static final int DELAY_MILLIS = 300;

        private final CommandsExplorerView          view;
        private final PredefinedCommandGoalRegistry goalRegistry;
        private final CommandManager                commandManager;
        private final CommandUtils                  commandUtils;

        private ContextualCommand command;

        @Inject
        public RefreshViewTask(CommandsExplorerView view,
                               PredefinedCommandGoalRegistry goalRegistry,
                               CommandManager commandManager,
                               CommandUtils commandUtils) {
            this.view = view;
            this.goalRegistry = goalRegistry;
            this.commandManager = commandManager;
            this.commandUtils = commandUtils;
        }

        @Override
        public void onExecute() {
            refreshView();

            if (command != null) {
                // wait some time while tree in the view will be fully refreshed
                new Timer() {
                    @Override
                    public void run() {
                        view.selectCommand(command);
                    }
                }.schedule(DELAY_MILLIS);
            }
        }

        void delayAndSelectCommand(@Nullable ContextualCommand command) {
            this.command = command;

            delay(DELAY_MILLIS);
        }

        private void refreshView() {
            final Map<CommandGoal, List<ContextualCommand>> commandsByGoal = new HashMap<>();

            // all predefined command goals must be shown in the view
            // so populate map by all registered command goals
            for (CommandGoal goal : goalRegistry.getAllGoals()) {
                commandsByGoal.put(goal, new ArrayList<ContextualCommand>());
            }

            commandsByGoal.putAll(commandUtils.groupCommandsByGoal(commandManager.getCommands()));

            view.setCommands(commandsByGoal);
        }
    }
}
