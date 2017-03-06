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
package org.eclipse.che.ide.command.manager;

import elemental.util.ArrayOf;
import elemental.util.Collections;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent.WorkspaceReadyHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_GOAL_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/**
 * Implementation of {@link CommandManager}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandManagerImpl implements CommandManager,
                                           Component,
                                           WorkspaceReadyHandler {

    private final AppContext                      appContext;
    private final PromiseProvider                 promiseProvider;
    private final CommandTypeRegistry             commandTypeRegistry;
    private final ProjectCommandManagerDelegate   projectCommandManagerDelegate;
    private final WorkspaceCommandManagerDelegate workspaceCommandManagerDelegate;
    private final EventBus                        eventBus;
    private final SelectionAgent                  selectionAgent;

    private final Map<String, ContextualCommand> commands;
    private final Set<CommandLoadedListener>     commandLoadedListeners;
    private final Set<CommandChangedListener>    commandChangedListeners;

    @Inject
    public CommandManagerImpl(AppContext appContext,
                              PromiseProvider promiseProvider,
                              CommandTypeRegistry commandTypeRegistry,
                              ProjectCommandManagerDelegate projectCommandManagerDelegate,
                              WorkspaceCommandManagerDelegate workspaceCommandManagerDelegate,
                              EventBus eventBus,
                              SelectionAgent selectionAgent) {
        this.appContext = appContext;
        this.promiseProvider = promiseProvider;
        this.commandTypeRegistry = commandTypeRegistry;
        this.projectCommandManagerDelegate = projectCommandManagerDelegate;
        this.workspaceCommandManagerDelegate = workspaceCommandManagerDelegate;
        this.eventBus = eventBus;
        this.selectionAgent = selectionAgent;

        commands = new HashMap<>();
        commandLoadedListeners = new HashSet<>();
        commandChangedListeners = new HashSet<>();
    }

    @Override
    public void start(Callback<Component, Exception> callback) {
        eventBus.addHandler(WorkspaceReadyEvent.getType(), this);

        callback.onSuccess(this);
    }

    @Override
    public void onWorkspaceReady(WorkspaceReadyEvent event) {
        fetchCommands();
    }

    private void fetchCommands() {
        // get all commands related to the workspace
        workspaceCommandManagerDelegate.getCommands(appContext.getWorkspaceId()).then(new Operation<List<CommandImpl>>() {
            @Override
            public void apply(List<CommandImpl> commands) throws OperationException {
                for (CommandImpl workspaceCommand : commands) {
                    final ApplicableContext context = new ApplicableContext();
                    context.setWorkspaceApplicable(true);

                    CommandManagerImpl.this.commands.put(workspaceCommand.getName(), new ContextualCommand(workspaceCommand, context));
                }

                // get all commands related to the projects
                for (Project project : appContext.getProjects()) {
                    for (CommandImpl projectCommand : projectCommandManagerDelegate.getCommands(project)) {
                        final ContextualCommand existedCommand = CommandManagerImpl.this.commands.get(projectCommand.getName());

                        if (existedCommand == null) {
                            final ApplicableContext context = new ApplicableContext();
                            context.addProject(project.getPath());

                            CommandManagerImpl.this.commands.put(projectCommand.getName(), new ContextualCommand(projectCommand, context));
                        } else {
                            if (projectCommand.equals(existedCommand)) {
                                existedCommand.getApplicableContext().addProject(project.getPath());
                            } else {
                                // should never happen
                                // ignore such command
//                                final ApplicableContext context = new ApplicableContext();
//                                context.addProject(project.getPath());
//
//                                commands.put(projectCommand.getName(), new ContextualCommand(projectCommand, context));
                            }
                        }
                    }
                }

                notifyCommandsLoaded();
            }
        });
    }

    @Override
    public List<ContextualCommand> getCommands() {
        return commands.values()
                       .stream()
                       .map(ContextualCommand::new)
                       .collect(Collectors.toList());
    }

    @Override
    public java.util.Optional<ContextualCommand> getCommand(String name) {
        return commands.values()
                       .stream()
                       .filter(command -> name.equals(command.getName()))
                       .findFirst();
    }

    @Override
    public List<ContextualCommand> getApplicableCommands() {
        return commands.values()
                       .stream()
                       .filter(this::isCommandApplicable)
                       .map(ContextualCommand::new)
                       .collect(Collectors.toList());
    }

    @Override
    public boolean isCommandApplicable(ContextualCommand command) {
        return isMachineSelected() || isCommandApplicableToCurrentProject(command);

    }

    /** Checks whether the machine is currently selected. */
    private boolean isMachineSelected() {
        final Selection<?> selection = selectionAgent.getSelection();

        if (selection != null && !selection.isEmpty() && selection.isSingleSelection()) {
            return selection.getHeadElement() instanceof Machine;
        }

        return false;
    }

    /** Checks whether the given command is applicable to the current project. */
    private boolean isCommandApplicableToCurrentProject(ContextualCommand command) {
        final List<String> applicableProjects = command.getApplicableContext().getApplicableProjects();

        if (applicableProjects.isEmpty()) {
            return true;
        }

        final Resource currentResource = appContext.getResource();
        if (currentResource != null) {
            final Project currentProject = currentResource.getProject();
            if (currentProject != null) {
                return applicableProjects.contains(currentProject.getPath());
            }
        }

        return false;
    }

    @Override
    public Promise<ContextualCommand> createCommand(String goalId, String commandTypeId, ApplicableContext applicableContext) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(commandTypeId);

        if (commandType == null) {
            return promiseProvider.reject(new Exception("Unknown command type: '" + commandTypeId + "'"));
        }

        final Map<String, String> attributes = new HashMap<>(1);
        attributes.put(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME, commandType.getPreviewUrlTemplate());
        attributes.put(COMMAND_GOAL_ATTRIBUTE_NAME, goalId);

        return createCommand(new ContextualCommand(getUniqueCommandName(commandTypeId, null),
                                                   commandType.getCommandLineTemplate(),
                                                   commandTypeId,
                                                   attributes,
                                                   applicableContext));
    }

    @Override
    public Promise<ContextualCommand> createCommand(ContextualCommand command) {
        return doCreateCommand(command).then((Function<ContextualCommand, ContextualCommand>)newCommand -> {
            // postpone the notification because
            // listeners should be notified after returning from #createCommand method
            Scheduler.get().scheduleDeferred(() -> notifyCommandAdded(newCommand));

            return newCommand;
        });
    }

    /** Adds the command without notifying listeners. */
    private Promise<ContextualCommand> doCreateCommand(ContextualCommand command) {
        final ApplicableContext context = command.getApplicableContext();

        final CommandType commandType = commandTypeRegistry.getCommandTypeById(command.getType());

        if (commandType == null) {
            return promiseProvider.reject(new Exception("Unknown command type: '" + command.getType() + "'"));
        }

        final ContextualCommand newCommand = new ContextualCommand(command);
        newCommand.setName(getUniqueCommandName(command.getType(), command.getName()));

        final ArrayOf<Promise<?>> commandPromises = Collections.arrayOf();

        if (context.isWorkspaceApplicable()) {
            Promise<CommandImpl> p = workspaceCommandManagerDelegate.createCommand(newCommand).then(
                    (Function<CommandImpl, CommandImpl>)arg -> {
                        newCommand.getApplicableContext().setWorkspaceApplicable(true);

                        return newCommand;
                    });

            commandPromises.push(p);
        }

        for (final String projectPath : context.getApplicableProjects()) {
            final Project project = getProjectByPath(projectPath);

            if (project == null) {
                continue;
            }

            Promise<CommandImpl> p = projectCommandManagerDelegate.createCommand(project, newCommand)
                                                                  .then((Function<CommandImpl, CommandImpl>)arg -> {
                                                                      newCommand.getApplicableContext().addProject(projectPath);

                                                                      return newCommand;
                                                                  });

            commandPromises.push(p);
        }

        return promiseProvider.all2(commandPromises)
                              .then((Function<ArrayOf<?>, ContextualCommand>)ignore -> {
                                  commands.put(newCommand.getName(), newCommand);

                                  return newCommand;
                              });
    }

    @Override
    public Promise<ContextualCommand> updateCommand(final String commandName, final ContextualCommand commandToUpdate) {
        final ContextualCommand existedCommand = commands.get(commandName);

        if (existedCommand == null) {
            return promiseProvider.reject(new Exception("Command '" + commandName + "' does not exist."));
        }

        // Use the simplest way to update command:
        // 1) remove existing command;
        // 2) create new one.
        return doRemoveCommand(commandName)
                .thenPromise(aVoid -> doCreateCommand(commandToUpdate)
                        .then((Function<ContextualCommand, ContextualCommand>)updatedCommand -> {
                            // listeners should be notified after returning from #updateCommand method
                            // so let's postpone notification
                            Scheduler.get().scheduleDeferred(() -> notifyCommandUpdated(existedCommand, updatedCommand));

                            return updatedCommand;
                        }));
    }

    @Override
    public Promise<Void> removeCommand(final String commandName) {
        final ContextualCommand command = commands.get(commandName);

        if (command == null) {
            return promiseProvider.reject(new Exception("Command '" + commandName + "' does not exist."));
        }

        return doRemoveCommand(commandName).then(aVoid -> {
            // listeners should be notified after returning from #removeCommand method
            // so let's postpone notification
            Scheduler.get().scheduleDeferred(() -> notifyCommandRemoved(command));
        });
    }

    /** Removes the command without notifying listeners. */
    private Promise<Void> doRemoveCommand(final String commandName) {
        final ContextualCommand command = commands.get(commandName);

        if (command == null) {
            return promiseProvider.reject(new Exception("Command '" + commandName + "' does not exist."));
        }

        final ApplicableContext context = command.getApplicableContext();

        final ArrayOf<Promise<?>> commandPromises = Collections.arrayOf();

        if (context.isWorkspaceApplicable()) {
            final Promise<Void> p = workspaceCommandManagerDelegate.removeCommand(commandName)
                                                                   .then((Function<Void, Void>)aVoid -> {
                                                                       command.getApplicableContext().setWorkspaceApplicable(false);

                                                                       return null;
                                                                   });

            commandPromises.push(p);
        }

        for (final String projectPath : context.getApplicableProjects()) {
            final Project project = getProjectByPath(projectPath);

            if (project == null) {
                continue;
            }

            final Promise<Void> p = projectCommandManagerDelegate.removeCommand(project, commandName)
                                                                 .then((Function<Void, Void>)aVoid -> {
                                                                     command.getApplicableContext().removeProject(projectPath);

                                                                     return null;
                                                                 });

            commandPromises.push(p);
        }

        return promiseProvider.all2(commandPromises)
                              .then((Function<ArrayOf<?>, Void>)arg -> {
                                  commands.remove(command.getName());

                                  return null;
                              });
    }

    @Nullable
    private Project getProjectByPath(String path) {
        for (Project project : appContext.getProjects()) {
            if (path.equals(project.getPath())) {
                return project;
            }
        }

        return null;
    }

    @Override
    public void addCommandLoadedListener(CommandLoadedListener listener) {
        commandLoadedListeners.add(listener);
    }

    @Override
    public void removeCommandLoadedListener(CommandLoadedListener listener) {
        commandLoadedListeners.remove(listener);
    }

    @Override
    public void addCommandChangedListener(CommandChangedListener listener) {
        commandChangedListeners.add(listener);
    }

    @Override
    public void removeCommandChangedListener(CommandChangedListener listener) {
        commandChangedListeners.remove(listener);
    }

    private void notifyCommandsLoaded() {
        commandLoadedListeners.forEach(CommandLoadedListener::onCommandsLoaded);
    }

    private void notifyCommandAdded(ContextualCommand command) {
        commandChangedListeners.forEach(listener -> listener.onCommandAdded(command));
    }

    private void notifyCommandRemoved(ContextualCommand command) {
        commandChangedListeners.forEach(listener -> listener.onCommandRemoved(command));
    }

    private void notifyCommandUpdated(ContextualCommand prevCommand, ContextualCommand command) {
        commandChangedListeners.forEach(listener -> listener.onCommandUpdated(prevCommand, command));
    }

    /**
     * Returns {@code customName} if it's unique within the given {@code customType}
     * or newly generated name if it isn't unique within the given {@code customType}.
     */
    private String getUniqueCommandName(String customType, @Nullable String customName) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(customType);
        final Set<String> commandNames = commands.keySet();

        final String newCommandName;

        if (isNullOrEmpty(customName)) {
            newCommandName = "new" + commandType.getDisplayName();
        } else {
            if (!commandNames.contains(customName)) {
                return customName;
            }
            newCommandName = customName + " copy";
        }

        if (!commandNames.contains(newCommandName)) {
            return newCommandName;
        }

        for (int count = 1; count < 1000; count++) {
            if (!commandNames.contains(newCommandName + "-" + count)) {
                return newCommandName + "-" + count;
            }
        }

        return newCommandName;
    }
}
