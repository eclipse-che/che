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
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent.WorkspaceReadyHandler;
import org.eclipse.che.ide.util.loging.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_GOAL_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/** Implementation of {@link CommandManager}. */
@Singleton
public class CommandManagerImpl implements CommandManager, Component, WorkspaceReadyHandler {

    private final AppContext                      appContext;
    private final PromiseProvider                 promiseProvider;
    private final CommandTypeRegistry             commandTypeRegistry;
    private final ProjectCommandManagerDelegate   projectCommandManager;
    private final WorkspaceCommandManagerDelegate workspaceCommandManager;
    private final EventBus                        eventBus;
    private final SelectionAgent                  selectionAgent;

    /** Map of the commands' names to the commands. */
    private final Map<String, CommandImpl>    commands;
    private final Set<CommandLoadedListener>  commandLoadedListeners;
    private final Set<CommandChangedListener> commandChangedListeners;

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
        this.projectCommandManager = projectCommandManagerDelegate;
        this.workspaceCommandManager = workspaceCommandManagerDelegate;
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
        workspaceCommandManager.getCommands(appContext.getWorkspaceId()).then(workspaceCommands -> {
            workspaceCommands.forEach(workspaceCommand -> commands.put(workspaceCommand.getName(),
                                                                       new CommandImpl(workspaceCommand, new ApplicableContext())));

            // get all commands related to the projects
            Arrays.stream(appContext.getProjects())
                  .forEach(project -> projectCommandManager.getCommands(project).forEach(projectCommand -> {
                      final CommandImpl existedCommand = commands.get(projectCommand.getName());

                      if (existedCommand == null) {
                          commands.put(projectCommand.getName(),
                                       new CommandImpl(projectCommand, new ApplicableContext(project.getPath())));
                      } else {
                          if (projectCommand.equalsIgnoreContext(existedCommand)) {
                              existedCommand.getApplicableContext().addProject(project.getPath());
                          } else {
                              // normally, should never happen
                              Log.error(CommandManagerImpl.this.getClass(), "Different commands with the same names found");
                          }
                      }
                  }));

            notifyCommandsLoaded();
        });
    }

    @Override
    public List<CommandImpl> getCommands() {
        return commands.values()
                       .stream()
                       .map(CommandImpl::new)
                       .collect(toList());
    }

    @Override
    public java.util.Optional<CommandImpl> getCommand(String name) {
        return commands.values()
                       .stream()
                       .filter(command -> name.equals(command.getName()))
                       .findFirst();
    }

    @Override
    public List<CommandImpl> getApplicableCommands() {
        return commands.values()
                       .stream()
                       .filter(this::isCommandApplicable)
                       .map(CommandImpl::new)
                       .collect(toList());
    }

    @Override
    public boolean isCommandApplicable(CommandImpl command) {
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
    private boolean isCommandApplicableToCurrentProject(CommandImpl command) {
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
    public Promise<CommandImpl> createCommand(String goalId, String typeId) {
        return createCommand(goalId,
                             typeId,
                             null,
                             null,
                             new HashMap<>(),
                             new ApplicableContext());
    }

    @Override
    public Promise<CommandImpl> createCommand(String goalId, String typeId, ApplicableContext context) {
        return createCommand(goalId, typeId, null, null, new HashMap<>(), context);
    }

    @Override
    public Promise<CommandImpl> createCommand(String goalId,
                                              String typeId,
                                              String name,
                                              String commandLine,
                                              Map<String, String> attributes) {
        return createCommand(goalId, typeId, name, commandLine, attributes, new ApplicableContext());
    }

    @Override
    public Promise<CommandImpl> createCommand(String goalId,
                                              String typeId,
                                              @Nullable String name,
                                              @Nullable String commandLine,
                                              Map<String, String> attributes,
                                              ApplicableContext context) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(typeId);

        if (commandType == null) {
            return promiseProvider.reject(new Exception("Unknown command type: '" + typeId + "'"));
        }

        final Map<String, String> attr = new HashMap<>(attributes);
        attr.put(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME, commandType.getPreviewUrlTemplate());
        attr.put(COMMAND_GOAL_ATTRIBUTE_NAME, goalId);

        return createCommand(new CommandImpl(getUniqueCommandName(typeId, name),
                                             commandLine != null ? commandLine : commandType.getCommandLineTemplate(),
                                             typeId,
                                             attr,
                                             context));
    }

    @Override
    public Promise<CommandImpl> createCommand(CommandImpl command) {
        return doCreateCommand(command).then((Function<CommandImpl, CommandImpl>)newCommand -> {
            // postpone the notification because
            // listeners should be notified after returning from #createCommand method
            Scheduler.get().scheduleDeferred(() -> notifyCommandAdded(newCommand));

            return newCommand;
        });
    }

    /** Does the actual work for command creation. Doesn't notify listeners. */
    private Promise<CommandImpl> doCreateCommand(CommandImpl command) {
        final ApplicableContext context = command.getApplicableContext();
        if (!context.isWorkspaceApplicable() && context.getApplicableProjects().isEmpty()) {
            return promiseProvider.reject(new Exception("Command has to be applicable to the workspace or at least one project"));
        }

        final CommandType commandType = commandTypeRegistry.getCommandTypeById(command.getType());
        if (commandType == null) {
            return promiseProvider.reject(new Exception("Unknown command type: '" + command.getType() + "'"));
        }

        final CommandImpl newCommand = new CommandImpl(command);
        newCommand.setName(getUniqueCommandName(command.getType(), command.getName()));

        final ArrayOf<Promise<?>> commandPromises = Collections.arrayOf();

        if (context.isWorkspaceApplicable()) {
            Promise<CommandImpl> p = workspaceCommandManager.createCommand(newCommand)
                                                            .then((Function<CommandImpl, CommandImpl>)arg -> {
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

            Promise<CommandImpl> p = projectCommandManager.createCommand(project, newCommand)
                                                          .then((Function<CommandImpl, CommandImpl>)arg -> {
                                                              newCommand.getApplicableContext().addProject(projectPath);
                                                              return newCommand;
                                                          });

            commandPromises.push(p);
        }

        return promiseProvider.all2(commandPromises)
                              .then((Function<ArrayOf<?>, CommandImpl>)ignore -> {
                                  commands.put(newCommand.getName(), newCommand);
                                  return newCommand;
                              });
    }

    @Override
    public Promise<CommandImpl> updateCommand(String name, CommandImpl commandToUpdate) {
        final CommandImpl existedCommand = commands.get(name);

        if (existedCommand == null) {
            return promiseProvider.reject(new Exception("Command '" + name + "' does not exist."));
        }

        return doRemoveCommand(name).thenPromise(aVoid -> doCreateCommand(commandToUpdate)
                .then((Function<CommandImpl, CommandImpl>)updatedCommand -> {
                    // listeners should be notified after returning from #updateCommand method
                    // so let's postpone notification
                    Scheduler.get().scheduleDeferred(() -> notifyCommandUpdated(existedCommand, updatedCommand));

                    return updatedCommand;
                }));
    }

    @Override
    public Promise<Void> removeCommand(String name) {
        final CommandImpl command = commands.get(name);

        if (command == null) {
            return promiseProvider.reject(new Exception("Command '" + name + "' does not exist."));
        }

        return doRemoveCommand(name).then(aVoid -> {
            // listeners should be notified after returning from #removeCommand method
            // so let's postpone notification
            Scheduler.get().scheduleDeferred(() -> notifyCommandRemoved(command));
        });
    }

    /** Removes the command without notifying listeners. */
    private Promise<Void> doRemoveCommand(String name) {
        final CommandImpl command = commands.get(name);

        if (command == null) {
            return promiseProvider.reject(new Exception("Command '" + name + "' does not exist."));
        }

        final ApplicableContext context = command.getApplicableContext();

        final ArrayOf<Promise<?>> commandPromises = Collections.arrayOf();

        if (context.isWorkspaceApplicable()) {
            final Promise<Void> p = workspaceCommandManager.removeCommand(name).then((Function<Void, Void>)aVoid -> {
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

            final Promise<Void> p = projectCommandManager.removeCommand(project, name).then((Function<Void, Void>)aVoid -> {
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

    private void notifyCommandAdded(CommandImpl command) {
        commandChangedListeners.forEach(listener -> listener.onCommandAdded(command));
    }

    private void notifyCommandRemoved(CommandImpl command) {
        commandChangedListeners.forEach(listener -> listener.onCommandRemoved(command));
    }

    private void notifyCommandUpdated(CommandImpl prevCommand, CommandImpl command) {
        commandChangedListeners.forEach(listener -> listener.onCommandUpdated(prevCommand, command));
    }

    /**
     * Returns {@code customName} if it's unique within the given {@code commandTypeId}
     * or newly generated name if it isn't unique within the given {@code commandTypeId}.
     */
    private String getUniqueCommandName(String commandTypeId, @Nullable String customName) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(commandTypeId);
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
