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
package org.eclipse.che.ide.ext.git.client.remote;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.remote.add.AddRemoteRepositoryPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.eclipse.che.ide.ext.git.client.remote.RemotePresenter.REMOTE_REPO_COMMAND_NAME;

/**
 * Testing {@link RemotePresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class RemotePresenterTest extends BaseTest {
    public static final boolean SHOW_ALL_INFORMATION = true;
    public static final boolean IS_SHOWN             = true;
    @Mock
    private RemoteView                   view;
    @Mock
    private Remote                       selectedRemote;
    @Mock
    private AddRemoteRepositoryPresenter addRemoteRepositoryPresenter;
    @Mock
    private ProjectServiceClient         projectService;

    @Captor
    private ArgumentCaptor<AsyncRequestCallback<ProjectConfigDto>> getProjectCallbackCaptor;

    private RemotePresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new RemotePresenter(view,
                                        service,
                                        appContext,
                                        eventBus,
                                        constant,
                                        projectService,
                                        addRemoteRepositoryPresenter,
                                        notificationManager,
                                        dtoUnmarshallerFactory,
                                        gitOutputConsoleFactory,
                                        consolesPanelPresenter);

        when(selectedRemote.getName()).thenReturn(REPOSITORY_NAME);
    }

    @Test
    public void testShowDialogWhenRemoteListRequestIsSuccessful() throws Exception {
        final List<Remote> remotes = new ArrayList<>();
        remotes.add(selectedRemote);
        when(view.isShown()).thenReturn(!IS_SHOWN);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Remote>> callback = (AsyncRequestCallback<List<Remote>>)arguments[3];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, remotes);
                return callback;
            }
        }).when(service).remoteList(anyString(), anyObject(), anyString(), anyBoolean(),
                                    (AsyncRequestCallback<List<Remote>>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service).remoteList(anyString(), eq(rootProjectConfig), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(view).setEnableDeleteButton(eq(DISABLE_BUTTON));
        verify(view).setRemotes((List<Remote>)anyObject());
        verify(view).showDialog();
    }

    @Test
    public void testShowDialogWhenRemoteListRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Remote>> callback = (AsyncRequestCallback<List<Remote>>)arguments[3];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).remoteList(anyString(), anyObject(), anyString(), anyBoolean(),
                                    (AsyncRequestCallback<List<Remote>>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service).remoteList(anyString(), eq(rootProjectConfig), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(constant).remoteListFailed();
    }

    @Test
    public void testOnCloseClicked() throws Exception {
        presenter.onCloseClicked();

        verify(view).close();
    }

    @Test
    public void testOnAddClicked() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Void> callback = (AsyncCallback<Void>)arguments[0];
                callback.onSuccess(null);
                return callback;
            }
        }).when(addRemoteRepositoryPresenter).showDialog((AsyncCallback<Void>)anyObject());

        presenter.onAddClicked();

        AsyncRequestCallback<ProjectConfigDto> getProjectCallback = getProjectCallbackCaptor.getValue();
        org.eclipse.che.test.GwtReflectionUtils.callOnSuccess(getProjectCallback, PROJECT_PATH);

        verify(service).remoteList(anyString(), anyObject(), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(console, never()).printError(anyString());
        verify(notificationManager, never()).notify(anyString(), eq(rootProjectConfig));
        verify(projectService).getProject(anyString(), anyString(), anyObject());
        verify(eventBus).fireEvent(Matchers.<ProjectUpdatedEvent>anyObject());
    }

    @Test
    public void testOnAddClickedWhenExceptionHappened() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Void> callback = (AsyncCallback<Void>)arguments[0];
                callback.onFailure(mock(Throwable.class));
                return callback;
            }
        }).when(addRemoteRepositoryPresenter).showDialog((AsyncCallback<Void>)anyObject());

        presenter.onAddClicked();

        AsyncRequestCallback<ProjectConfigDto> getProjectCallback = getProjectCallbackCaptor.getValue();
        org.eclipse.che.test.GwtReflectionUtils.callOnFailure(getProjectCallback, mock(Throwable.class));

        verify(service, never()).remoteList(anyString(), anyObject(), anyString(), eq(SHOW_ALL_INFORMATION),
                                            (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(gitOutputConsoleFactory).create(REMOTE_REPO_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager, never()).notify(anyString(), eq(rootProjectConfig));
        verify(notificationManager).notify(anyString(), eq(StatusNotification.Status.FAIL), eq(true), eq(rootProjectConfig));
        verify(constant).remoteAddFailed();
    }

    @Test
    public void testOnDeleteClickedWhenRemoteDeleteRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, EMPTY_TEXT);
                return callback;
            }
        }).when(service).remoteDelete(anyString(), anyObject(), anyString(), (AsyncRequestCallback<String>)anyObject());

        presenter.showDialog();
        presenter.onRemoteSelected(selectedRemote);
        presenter.onDeleteClicked();

        AsyncRequestCallback<ProjectConfigDto> getProjectCallback = getProjectCallbackCaptor.getValue();
        org.eclipse.che.test.GwtReflectionUtils.callOnSuccess(getProjectCallback, PROJECT_PATH);

        verify(service).remoteDelete(anyString(), eq(rootProjectConfig), eq(REPOSITORY_NAME), (AsyncRequestCallback<String>)anyObject());
        verify(service, times(2)).remoteList(anyString(), anyObject(), anyString(), eq(SHOW_ALL_INFORMATION),
                                             (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(projectService).getProject(anyString(), anyString(), anyObject());
        verify(eventBus).fireEvent(Matchers.<ProjectUpdatedEvent>anyObject());
    }

    @Test
    public void testOnDeleteClickedWhenRemoteDeleteRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).remoteDelete(anyString(), anyObject(), anyString(), (AsyncRequestCallback<String>)anyObject());

        presenter.showDialog();
        presenter.onRemoteSelected(selectedRemote);
        presenter.onDeleteClicked();

        AsyncRequestCallback<ProjectConfigDto> getProjectCallback = getProjectCallbackCaptor.getValue();
        org.eclipse.che.test.GwtReflectionUtils.callOnFailure(getProjectCallback, mock(Throwable.class));

        verify(service).remoteDelete(anyString(), eq(rootProjectConfig), eq(REPOSITORY_NAME), (AsyncRequestCallback<String>)anyObject());
        verify(constant).remoteDeleteFailed();
        verify(gitOutputConsoleFactory).create(REMOTE_REPO_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager, never()).notify(anyString(), eq(rootProjectConfig));
        verify(notificationManager).notify(anyString(), eq(StatusNotification.Status.FAIL), eq(true), eq(rootProjectConfig));
    }

    @Test
    public void testOnRemoteSelected() throws Exception {
        presenter.onRemoteSelected(selectedRemote);

        verify(view).setEnableDeleteButton(eq(ENABLE_BUTTON));
    }
}
