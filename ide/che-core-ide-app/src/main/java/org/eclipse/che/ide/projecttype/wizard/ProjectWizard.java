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
package org.eclipse.che.ide.projecttype.wizard;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.IMPORT;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.PROJECT_NAME_KEY;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.eclipse.che.ide.api.resources.Resource.FOLDER;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.wizard.AbstractWizard;
import org.eclipse.che.ide.resource.Path;

/**
 * Project wizard used for creating new a project or updating an existing one.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 * @author Valeriy Svydenko
 */
public class ProjectWizard extends AbstractWizard<MutableProjectConfig> {

  private static final String PROJECT_PATH_MACRO_REGEX = "\\$\\{current.project.path\\}";

  private final ProjectWizardMode mode;
  private final AppContext appContext;
  private final CommandManager commandManager;

  @Inject
  public ProjectWizard(
      @Assisted MutableProjectConfig dataObject,
      @Assisted ProjectWizardMode mode,
      AppContext appContext,
      CommandManager commandManager) {
    super(dataObject);
    this.mode = mode;
    this.appContext = appContext;
    this.commandManager = commandManager;

    context.put(WIZARD_MODE_KEY, mode.toString());
    context.put(PROJECT_NAME_KEY, dataObject.getName());
  }

  @Override
  public void complete(@NotNull final CompleteCallback callback) {
    if (mode == CREATE) {
      appContext
          .getWorkspaceRoot()
          .newProject()
          .withBody(dataObject)
          .send()
          .then(onComplete(callback))
          .catchError(onFailure(callback));
    } else if (mode == UPDATE) {
      String path =
          dataObject.getPath().startsWith("/")
              ? dataObject.getPath().substring(1)
              : dataObject.getPath();

      appContext
          .getWorkspaceRoot()
          .getContainer(Path.valueOf(path))
          .then(
              optContainer -> {
                checkState(optContainer.isPresent(), "Failed to update non existed path");

                final Container container = optContainer.get();
                if (container.getResourceType() == PROJECT) {
                  ((Project) container)
                      .update()
                      .withBody(dataObject)
                      .send()
                      .then(onComplete(callback))
                      .catchError(onFailure(callback));
                } else if (container.getResourceType() == FOLDER) {
                  ((Folder) container)
                      .toProject()
                      .withBody(dataObject)
                      .send()
                      .then(onComplete(callback))
                      .catchError(onFailure(callback));
                }
              });
    } else if (mode == IMPORT) {
      appContext
          .getWorkspaceRoot()
          .newProject()
          .withBody(dataObject)
          .send()
          .thenPromise(project -> project.update().withBody(dataObject).send())
          .then(addCommands(callback))
          .catchError(onFailure(callback));
    }
  }

  private Operation<Project> addCommands(CompleteCallback callback) {
    return project -> {
      Promise<CommandImpl> chain = null;
      for (final CommandDto command : dataObject.getCommands()) {
        if (chain == null) {
          chain = addCommand(project, command);
        } else {
          chain = chain.thenPromise(ignored -> addCommand(project, command));
        }
      }

      if (chain == null) {
        callback.onCompleted();
      } else {
        chain
            .then(
                ignored -> {
                  callback.onCompleted();
                })
            .catchError(onFailure(callback));
      }
    };
  }

  private Promise<CommandImpl> addCommand(Project project, CommandDto commandDto) {
    final String name = project.getName() + ": " + commandDto.getName();
    final String absoluteProjectPath =
        appContext.getProjectsRoot().append(project.getPath()).toString();
    final String commandLine =
        commandDto.getCommandLine().replaceAll(PROJECT_PATH_MACRO_REGEX, absoluteProjectPath);

    final CommandImpl command =
        new CommandImpl(
            name,
            commandLine,
            commandDto.getType(),
            commandDto.getAttributes(),
            new ApplicableContext(project.getPath()));

    return commandManager.createCommand(command);
  }

  private Operation<Project> onComplete(final CompleteCallback callback) {
    return ignored -> callback.onCompleted();
  }

  private Operation<PromiseError> onFailure(final CompleteCallback callback) {
    return error -> callback.onFailure(error.getCause());
  }
}
