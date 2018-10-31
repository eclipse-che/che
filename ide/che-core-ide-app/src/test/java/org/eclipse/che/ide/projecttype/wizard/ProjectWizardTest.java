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
package org.eclipse.che.ide.projecttype.wizard;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.IMPORT;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.util.Collections;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Testing {@link ProjectWizard}.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectWizardTest {
  private static final String PROJECT_NAME = "project1";

  @Mock private MutableProjectConfig dataObject;
  @Mock private Wizard.CompleteCallback completeCallback;
  @Mock private AppContext appContext;
  @Mock private CommandManager commandManager;
  @Mock private Container workspaceRoot;
  @Mock private Project.ProjectRequest createProjectRequest;
  @Mock private Promise<Project> createProjectPromise;
  @Mock private Project createdProject;
  @Mock private CommandDto command;
  @Mock private Promise<CommandImpl> createCommandPromise;
  @Mock private CommandImpl createdCommand;
  @Mock private Promise<Optional<Container>> optionalContainerPromise;
  @Mock private Project projectToUpdate;
  @Mock private Folder folderToUpdate;
  @Mock private NotificationManager notificationManager;
  @Mock private StatusNotification statusNotification;
  @Mock private CoreLocalizationConstant localizationConstant;

  @Mock private PromiseError promiseError;
  @Mock private Exception exception;

  @Captor private ArgumentCaptor<Operation<Project>> completeOperationCaptor;
  @Captor private ArgumentCaptor<Operation<CommandImpl>> completeAddCommandsOperationCaptor;
  @Captor private ArgumentCaptor<Operation<PromiseError>> failedOperationCaptor;
  @Captor private ArgumentCaptor<Operation<Optional<Container>>> optionalContainerCaptor;

  private ProjectWizard wizard;

  @Before
  public void setUp() {
    when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
    when(appContext.getProjectsRoot()).thenReturn(Path.valueOf("/projects"));
    when(dataObject.getPath()).thenReturn(Path.valueOf(PROJECT_NAME).toString());
    when(createdProject.getPath()).thenReturn(PROJECT_NAME);
    when(notificationManager.notify(any(), any(), (StatusNotification.Status) any(), any()))
        .thenReturn(statusNotification);
  }

  @Test
  public void shouldCreateProject() throws Exception {
    prepareWizard(CREATE);

    when(workspaceRoot.newProject()).thenReturn(createProjectRequest);
    when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
    when(createProjectRequest.send()).thenReturn(createProjectPromise);
    when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);

    wizard.complete(completeCallback);

    verify(createProjectPromise).then(completeOperationCaptor.capture());
    completeOperationCaptor.getValue().apply(createdProject);

    verify(completeCallback).onCompleted();
  }

  private void prepareWizard(ProjectWizardMode mode) {
    wizard =
        new ProjectWizard(
            dataObject,
            mode,
            appContext,
            commandManager,
            notificationManager,
            localizationConstant);
  }

  @Test
  public void shouldInvokeCallbackWhenCreatingFailure() throws Exception {
    prepareWizard(CREATE);

    when(workspaceRoot.newProject()).thenReturn(createProjectRequest);
    when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
    when(createProjectRequest.send()).thenReturn(createProjectPromise);
    when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);
    when(promiseError.getCause()).thenReturn(exception);

    wizard.complete(completeCallback);

    verify(createProjectPromise).catchError(failedOperationCaptor.capture());
    failedOperationCaptor.getValue().apply(promiseError);

    verify(promiseError).getCause();
    verify(completeCallback).onFailure(eq(exception));
    verify(notificationManager).notify(any(), any(), eq(FAIL), eq(FLOAT_MODE));
  }

  @Test
  public void shouldImportProjectSuccessfully() throws Exception {
    prepareWizard(IMPORT);

    when(workspaceRoot.newProject()).thenReturn(createProjectRequest);
    when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
    when(createProjectRequest.send()).thenReturn(createProjectPromise);
    when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.thenPromise(any(Function.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);
    when(promiseError.getCause()).thenReturn(exception);

    wizard.complete(completeCallback);

    verify(createProjectPromise).then(completeOperationCaptor.capture());
    completeOperationCaptor.getValue().apply(createdProject);

    verify(completeCallback).onCompleted();
  }

  @Test
  public void shouldImportProjectWithCommandSuccessfully() throws Exception {
    prepareWizard(IMPORT);

    when(workspaceRoot.importProject()).thenReturn(createProjectRequest);
    when(workspaceRoot.newProject()).thenReturn(createProjectRequest);
    when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
    when(createProjectRequest.send()).thenReturn(createProjectPromise);
    when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.thenPromise(any(Function.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);
    when(promiseError.getCause()).thenReturn(exception);
    when(dataObject.getCommands()).thenReturn(Collections.singletonList(command));
    when(command.getCommandLine()).thenReturn("echo 'Hello'");
    when(commandManager.createCommand(any(CommandImpl.class))).thenReturn(createCommandPromise);
    when(createCommandPromise.then(any(Operation.class))).thenReturn(createCommandPromise);
    when(createCommandPromise.catchError(any(Operation.class))).thenReturn(createCommandPromise);

    wizard.complete(completeCallback);

    verify(createProjectPromise).then(completeOperationCaptor.capture());
    completeOperationCaptor.getValue().apply(createdProject);

    verify(createCommandPromise).then(completeAddCommandsOperationCaptor.capture());
    completeAddCommandsOperationCaptor.getValue().apply(createdCommand);

    verify(completeCallback).onCompleted();
  }

  @Test
  public void shouldFailOnImportProject() throws Exception {
    prepareWizard(IMPORT);

    when(workspaceRoot.newProject()).thenReturn(createProjectRequest);
    when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
    when(createProjectRequest.send()).thenReturn(createProjectPromise);
    when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.thenPromise(any(Function.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);
    when(promiseError.getCause()).thenReturn(exception);

    wizard.complete(completeCallback);

    verify(createProjectPromise).catchError(failedOperationCaptor.capture());
    failedOperationCaptor.getValue().apply(promiseError);

    verify(promiseError).getCause();
    verify(completeCallback).onFailure(eq(exception));
    verify(notificationManager).notify(any(), any(), eq(FAIL), eq(FLOAT_MODE));
  }

  @Test
  public void shouldUpdateProjectConfig() throws Exception {
    prepareWizard(UPDATE);

    when(workspaceRoot.getContainer(any(Path.class))).thenReturn(optionalContainerPromise);
    when(projectToUpdate.getResourceType()).thenReturn(Resource.PROJECT);
    when(projectToUpdate.update()).thenReturn(createProjectRequest);
    when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
    when(createProjectRequest.send()).thenReturn(createProjectPromise);
    when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);

    wizard.complete(completeCallback);

    verify(optionalContainerPromise).then(optionalContainerCaptor.capture());
    optionalContainerCaptor.getValue().apply(Optional.of((Container) projectToUpdate));

    verify(createProjectPromise).then(completeOperationCaptor.capture());
    completeOperationCaptor.getValue().apply(createdProject);

    verify(completeCallback).onCompleted();
    verify(notificationManager).notify(any(), any(), eq(PROGRESS), eq(FLOAT_MODE));
    verify(statusNotification).setStatus(eq(SUCCESS));
  }

  @Test
  public void shouldFailUpdateProjectConfig() throws Exception {
    prepareWizard(UPDATE);

    when(workspaceRoot.getContainer(any(Path.class))).thenReturn(optionalContainerPromise);
    when(projectToUpdate.getResourceType()).thenReturn(Resource.PROJECT);
    when(projectToUpdate.update()).thenReturn(createProjectRequest);
    when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
    when(createProjectRequest.send()).thenReturn(createProjectPromise);
    when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
    when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);
    when(promiseError.getCause()).thenReturn(exception);

    wizard.complete(completeCallback);

    verify(optionalContainerPromise).then(optionalContainerCaptor.capture());
    optionalContainerCaptor.getValue().apply(Optional.of((Container) projectToUpdate));

    verify(createProjectPromise).catchError(failedOperationCaptor.capture());
    failedOperationCaptor.getValue().apply(promiseError);

    verify(promiseError).getCause();
    verify(completeCallback).onFailure(eq(exception));
    verify(notificationManager).notify(any(), any(), eq(PROGRESS), eq(FLOAT_MODE));
    verify(statusNotification).setStatus(eq(FAIL));
  }

  @Test
  public void shouldCreateConfigForFolder() throws Exception {
    //        prepareWizard(UPDATE);
    //
    //
    // when(workspaceRoot.getContainer(any(Path.class))).thenReturn(optionalContainerPromise);
    //        when(folderToUpdate.getResourceType()).thenReturn(Resource.FOLDER);
    //        when(folderToUpdate.toProject()).thenReturn(createProjectRequest);
    //
    // when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
    //        when(createProjectRequest.send()).thenReturn(createProjectPromise);
    //
    // when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
    //
    // when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);
    //
    //        wizard.complete(completeCallback);
    //
    //        verify(optionalContainerPromise).then(optionalContainerCaptor.capture());
    //        optionalContainerCaptor.getValue().apply(Optional.of((Container)folderToUpdate));
    //
    //        verify(createProjectPromise).then(completeOperationCaptor.capture());
    //        completeOperationCaptor.getValue().apply(createdProject);
    //
    //        verify(completeCallback).onCompleted();
  }

  @Test
  public void shouldFailCreateConfigForFolder() throws Exception {
    //        prepareWizard(UPDATE);
    //
    //
    // when(workspaceRoot.getContainer(any(Path.class))).thenReturn(optionalContainerPromise);
    //        when(folderToUpdate.getResourceType()).thenReturn(Resource.FOLDER);
    //        when(folderToUpdate.toProject()).thenReturn(createProjectRequest);
    //
    // when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
    //        when(createProjectRequest.send()).thenReturn(createProjectPromise);
    //
    // when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
    //
    // when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);
    //        when(promiseError.getCause()).thenReturn(exception);
    //
    //        wizard.complete(completeCallback);
    //
    //        verify(optionalContainerPromise).then(optionalContainerCaptor.capture());
    //        optionalContainerCaptor.getValue().apply(Optional.of((Container)folderToUpdate));
    //
    //        verify(createProjectPromise).catchError(failedOperationCaptor.capture());
    //        failedOperationCaptor.getValue().apply(promiseError);
    //
    //        verify(promiseError).getCause();
    //        verify(completeCallback).onFailure(eq(exception));
  }
}
