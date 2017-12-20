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
package org.eclipse.che.ide.command.manager;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_GOAL_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import elemental.util.ArrayOf;
import elemental.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandAddedEvent;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandRemovedEvent;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.command.CommandUpdatedEvent;
import org.eclipse.che.ide.api.command.CommandsLoadedEvent;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.util.loging.Log;

/** Implementation of {@link CommandManager}. */
@Singleton
public class CommandManagerImpl implements CommandManager {

  private final AppContext appContext;
  private final PromiseProvider promiseProvider;
  private final CommandTypeRegistry commandTypeRegistry;
  private final ProjectCommandManagerDelegate projectCommandManager;
  private final WorkspaceCommandManagerDelegate workspaceCommandManager;
  private final SelectionAgent selectionAgent;
  private final EventBus eventBus;
  private final CommandNameGenerator commandNameGenerator;

  /** Map of the commands' names to the commands. */
  private final Map<String, CommandImpl> commands;

  @Inject
  public CommandManagerImpl(
      AppContext appContext,
      PromiseProvider promiseProvider,
      CommandTypeRegistry commandTypeRegistry,
      ProjectCommandManagerDelegate projectCommandManagerDelegate,
      WorkspaceCommandManagerDelegate workspaceCommandManagerDelegate,
      SelectionAgent selectionAgent,
      EventBus eventBus,
      CommandNameGenerator commandNameGenerator) {
    this.appContext = appContext;
    this.promiseProvider = promiseProvider;
    this.commandTypeRegistry = commandTypeRegistry;
    this.projectCommandManager = projectCommandManagerDelegate;
    this.workspaceCommandManager = workspaceCommandManagerDelegate;
    this.selectionAgent = selectionAgent;
    this.eventBus = eventBus;
    this.commandNameGenerator = commandNameGenerator;

    commands = new HashMap<>();
    registerNative();

    eventBus.addHandler(WorkspaceReadyEvent.getType(), e -> fetchCommands());
    eventBus.addHandler(
        WorkspaceStoppedEvent.TYPE,
        e -> {
          commands.clear();
          notifyCommandsLoaded();
        });
  }

  private void fetchCommands() {
    // get all commands related to the workspace
    workspaceCommandManager
        .fetchCommands()
        .then(
            workspaceCommands -> {
              workspaceCommands.forEach(
                  workspaceCommand ->
                      commands.put(
                          workspaceCommand.getName(),
                          new CommandImpl(workspaceCommand, new ApplicableContext())));

              Arrays.stream(appContext.getProjects())
                  .forEach(
                      project ->
                          projectCommandManager
                              .getCommands(project)
                              .forEach(
                                  projectCommand -> {
                                    final CommandImpl existedCommand =
                                        this.commands.get(projectCommand.getName());

                                    if (existedCommand == null) {
                                      this.commands.put(
                                          projectCommand.getName(),
                                          new CommandImpl(
                                              projectCommand,
                                              new ApplicableContext(project.getPath())));
                                    } else {
                                      if (projectCommand.equalsIgnoreContext(existedCommand)) {
                                        existedCommand
                                            .getApplicableContext()
                                            .addProject(project.getPath());
                                      } else {
                                        // normally, should never happen
                                        Log.error(
                                            CommandManagerImpl.this.getClass(),
                                            "Different commands with the same names found");
                                      }
                                    }
                                  }));

              notifyCommandsLoaded();
            });
  }

  @Override
  public List<CommandImpl> getCommands() {
    return commands.values().stream().map(CommandImpl::new).collect(toList());
  }

  @Override
  public java.util.Optional<CommandImpl> getCommand(String name) {
    return commands.values().stream().filter(command -> name.equals(command.getName())).findFirst();
  }

  @Override
  public List<CommandImpl> getApplicableCommands() {
    return commands
        .values()
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
    final Set<String> applicableProjects = command.getApplicableContext().getApplicableProjects();

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
    return createCommand(goalId, typeId, null, null, new HashMap<>(), new ApplicableContext());
  }

  @Override
  public Promise<CommandImpl> createCommand(
      String goalId, String typeId, ApplicableContext context) {
    return createCommand(goalId, typeId, null, null, new HashMap<>(), context);
  }

  @Override
  public Promise<CommandImpl> createCommand(
      String goalId,
      String typeId,
      String name,
      String commandLine,
      Map<String, String> attributes) {
    return createCommand(goalId, typeId, name, commandLine, attributes, new ApplicableContext());
  }

  @Override
  public Promise<CommandImpl> createCommand(
      String goalId,
      String typeId,
      @Nullable String name,
      @Nullable String commandLine,
      Map<String, String> attributes,
      ApplicableContext context) {

    final Map<String, String> attr = new HashMap<>(attributes);
    attr.put(COMMAND_GOAL_ATTRIBUTE_NAME, goalId);

    final Optional<CommandType> commandType = commandTypeRegistry.getCommandTypeById(typeId);
    commandType.ifPresent(
        type ->
            attr.put(
                COMMAND_PREVIEW_URL_ATTRIBUTE_NAME, commandType.get().getPreviewUrlTemplate()));

    final String commandLineTemplate =
        commandType.map(CommandType::getCommandLineTemplate).orElse("");

    return createCommand(
        new CommandImpl(
            commandNameGenerator.generate(typeId, name),
            commandLine != null ? commandLine : commandLineTemplate,
            typeId,
            attr,
            context));
  }

  @Override
  public Promise<CommandImpl> createCommand(CommandImpl command) {
    return doCreateCommand(command)
        .then(
            (Function<CommandImpl, CommandImpl>)
                newCommand -> {
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
      return promiseProvider.reject(
          new Exception("Command has to be applicable to the workspace or at least one project"));
    }

    final CommandImpl newCommand = new CommandImpl(command);
    newCommand.setName(commandNameGenerator.generate(command.getType(), command.getName()));

    final ArrayOf<Promise<?>> commandPromises = Collections.arrayOf();

    if (context.isWorkspaceApplicable()) {
      Promise<CommandImpl> p =
          workspaceCommandManager
              .createCommand(newCommand)
              .then(
                  (Function<Void, CommandImpl>)
                      aVoid -> {
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

      Promise<CommandImpl> p =
          projectCommandManager
              .createCommand(project, newCommand)
              .then(
                  (Function<CommandImpl, CommandImpl>)
                      arg -> {
                        newCommand.getApplicableContext().addProject(projectPath);
                        return newCommand;
                      });

      commandPromises.push(p);
    }

    return promiseProvider
        .all2(commandPromises)
        .then(
            (Function<ArrayOf<?>, CommandImpl>)
                ignore -> {
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

    return doRemoveCommand(name)
        .thenPromise(
            aVoid ->
                doCreateCommand(commandToUpdate)
                    .then(
                        (Function<CommandImpl, CommandImpl>)
                            updatedCommand -> {
                              // listeners should be notified after returning from #updateCommand
                              // method
                              // so let's postpone notification
                              Scheduler.get()
                                  .scheduleDeferred(
                                      () -> notifyCommandUpdated(existedCommand, updatedCommand));

                              return updatedCommand;
                            }));
  }

  @Override
  public Promise<Void> removeCommand(String name) {
    final CommandImpl command = commands.get(name);

    if (command == null) {
      return promiseProvider.reject(new Exception("Command '" + name + "' does not exist."));
    }

    return doRemoveCommand(name)
        .then(
            aVoid -> {
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
      commandPromises.push(workspaceCommandManager.removeCommand(name));
    }

    for (final String projectPath : context.getApplicableProjects()) {
      final Project project = getProjectByPath(projectPath);

      if (project == null) {
        continue;
      }

      commandPromises.push(projectCommandManager.removeCommand(project, name));
    }

    return promiseProvider
        .all2(commandPromises)
        .then(
            (Function<ArrayOf<?>, Void>)
                arg -> {
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

  private void notifyCommandsLoaded() {
    eventBus.fireEvent(new CommandsLoadedEvent());
  }

  private void notifyCommandAdded(CommandImpl command) {
    eventBus.fireEvent(new CommandAddedEvent(command));
  }

  private void notifyCommandRemoved(CommandImpl command) {
    eventBus.fireEvent(new CommandRemovedEvent(command));
  }

  private void notifyCommandUpdated(CommandImpl prevCommand, CommandImpl command) {
    eventBus.fireEvent(new CommandUpdatedEvent(prevCommand, command));
  }

  /* Expose Command Manager's internal API to the world, to allow selenium tests or clients that use IDE to refresh commands. */
  private native void registerNative() /*-{
        var that = this;

        var CommandManager = {};

        CommandManager.refresh = $entry(function () {
            that.@org.eclipse.che.ide.command.manager.CommandManagerImpl::fetchCommands()();
        });

        $wnd.IDE.CommandManager = CommandManager;
    }-*/;
}
