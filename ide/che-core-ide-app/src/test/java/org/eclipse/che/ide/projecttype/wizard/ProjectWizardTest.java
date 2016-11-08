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
package org.eclipse.che.ide.projecttype.wizard;

import com.google.common.base.Optional;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
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
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.IMPORT;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link ProjectWizard}.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectWizardTest {
    private static final String PROJECT_NAME = "project1";

    @Mock
    private MutableProjectConfig         dataObject;
    @Mock
    private Wizard.CompleteCallback      completeCallback;
    @Mock
    private AppContext                   appContext;
    @Mock
    private Container                    workspaceRoot;
    @Mock
    private Project.ProjectRequest       createProjectRequest;
    @Mock
    private Promise<Project>             createProjectPromise;
    @Mock
    private Project                      createdProject;
    @Mock
    private Promise<Optional<Container>> optionalContainerPromise;
    @Mock
    private Project                      projectToUpdate;
    @Mock
    private Folder                       folderToUpdate;

    @Mock
    private PromiseError promiseError;
    @Mock
    private Exception    exception;

    @Captor
    private ArgumentCaptor<Operation<Project>>             completeOperationCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>        failedOperationCaptor;
    @Captor
    private ArgumentCaptor<Operation<Optional<Container>>> optionalContainerCaptor;

    private ProjectWizard wizard;

    @Before
    public void setUp() {
        when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
        when(dataObject.getPath()).thenReturn(Path.valueOf(PROJECT_NAME).toString());
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
        wizard = new ProjectWizard(dataObject, mode, appContext);
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
        optionalContainerCaptor.getValue().apply(Optional.of((Container)projectToUpdate));

        verify(createProjectPromise).then(completeOperationCaptor.capture());
        completeOperationCaptor.getValue().apply(createdProject);

        verify(completeCallback).onCompleted();
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
        optionalContainerCaptor.getValue().apply(Optional.of((Container)projectToUpdate));

        verify(createProjectPromise).catchError(failedOperationCaptor.capture());
        failedOperationCaptor.getValue().apply(promiseError);

        verify(promiseError).getCause();
        verify(completeCallback).onFailure(eq(exception));
    }

    @Test
    public void shouldCreateConfigForFolder() throws Exception {
//        prepareWizard(UPDATE);
//
//        when(workspaceRoot.getContainer(any(Path.class))).thenReturn(optionalContainerPromise);
//        when(folderToUpdate.getResourceType()).thenReturn(Resource.FOLDER);
//        when(folderToUpdate.toProject()).thenReturn(createProjectRequest);
//        when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
//        when(createProjectRequest.send()).thenReturn(createProjectPromise);
//        when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
//        when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);
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
//        when(workspaceRoot.getContainer(any(Path.class))).thenReturn(optionalContainerPromise);
//        when(folderToUpdate.getResourceType()).thenReturn(Resource.FOLDER);
//        when(folderToUpdate.toProject()).thenReturn(createProjectRequest);
//        when(createProjectRequest.withBody(any(ProjectConfig.class))).thenReturn(createProjectRequest);
//        when(createProjectRequest.send()).thenReturn(createProjectPromise);
//        when(createProjectPromise.then(any(Operation.class))).thenReturn(createProjectPromise);
//        when(createProjectPromise.catchError(any(Operation.class))).thenReturn(createProjectPromise);
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
