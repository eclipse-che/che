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
package org.eclipse.che.ide.ext.git.client.pull;

import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.Event;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.eclipse.che.ide.ext.git.client.pull.PullPresenter.PULL_COMMAND_NAME;

/**
 * Testing {@link PullPresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class PullPresenterTest extends BaseTest {
    public static final boolean SHOW_ALL_INFORMATION = true;
    public static final String  FILE_PATH            = "/src/testClass.java";

    @Mock
    private FileReferenceNode   file;
    @Mock
    private PullView            view;
    @Mock
    private Branch              branch;
    @Mock
    private EditorAgent         editorAgent;
    @Mock
    private EditorInput         editorInput;
    @Mock
    private EditorPartPresenter partPresenter;
    @Mock
    private BranchSearcher      branchSearcher;
    @Mock
    private PullResponse        pullResponse;
    @Mock
    private UsersWorkspaceDto   workspace;

    private PullPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        when(appContext.getWorkspaceId()).thenReturn("id");

        NavigableMap<String, EditorPartPresenter> partPresenterMap = new TreeMap<>();
        partPresenterMap.put("partPresenter", partPresenter);

        when(view.getRepositoryName()).thenReturn(REPOSITORY_NAME);
        when(view.getRepositoryUrl()).thenReturn(REMOTE_URI);
        when(view.getLocalBranch()).thenReturn(LOCAL_BRANCH);
        when(view.getRemoteBranch()).thenReturn(REMOTE_BRANCH);
        when(branch.getName()).thenReturn(REMOTE_BRANCH);
        when(editorAgent.getOpenedEditors()).thenReturn(partPresenterMap);
        when(partPresenter.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(file);
        when(file.getPath()).thenReturn(FILE_PATH);

        presenter = new PullPresenter(view,
                                      editorAgent,
                                      service,
                                      eventBus,
                                      appContext,
                                      constant,
                                      notificationManager,
                                      dtoUnmarshallerFactory,
                                      dtoFactory,
                                      branchSearcher,
                                      projectExplorer,
                                      gitOutputConsoleFactory,
                                      consolesPanelPresenter);
    }

    @Test
    public void testShowDialogWhenBranchListRequestIsSuccessful() throws Exception {
        final List<Remote> remotes = new ArrayList<>();
        remotes.add(mock(Remote.class));
        final List<Branch> branches = new ArrayList<>();
        branches.add(branch);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Remote>> callback = (AsyncRequestCallback<List<Remote>>)arguments[3];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, remotes);
                return callback;
            }
        }).when(service).remoteList(anyObject(),anyObject(), anyString(), anyBoolean(),
                                    (AsyncRequestCallback<List<Remote>>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, branches);
                return callback;
            }
        }).doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, branches);
                return callback;
            }
        }).when(service).branchList(anyObject(), anyObject(), anyString(), (AsyncRequestCallback<List<Branch>>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service).remoteList(anyObject(), eq(rootProjectConfig), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(view, times(2)).setEnablePullButton(eq(ENABLE_BUTTON));
        verify(view).setRepositories((List<Remote>)anyObject());
        verify(view).showDialog();
        verify(view).setRemoteBranches((List<String>)anyObject());
        verify(view).setLocalBranches((List<String>)anyObject());
    }

    @Test
    public void testSelectActiveBranch() throws Exception {
        final List<Remote> remotes = new ArrayList<>();
        remotes.add(mock(Remote.class));
        final List<Branch> branches = new ArrayList<>();
        branches.add(branch);
        when(branch.isActive()).thenReturn(ACTIVE_BRANCH);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Remote>> callback = (AsyncRequestCallback<List<Remote>>)arguments[3];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, remotes);
                return callback;
            }
        }).when(service).remoteList(anyObject(), anyObject(), anyString(), anyBoolean(),
                                    (AsyncRequestCallback<List<Remote>>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, branches);
                return callback;
            }
        }).doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, branches);
                return callback;
            }
        }).when(service).branchList(anyObject(), anyObject(), anyString(), (AsyncRequestCallback<List<Branch>>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service).remoteList(anyObject(), eq(rootProjectConfig), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(service, times(2))
                .branchList(anyObject(), eq(rootProjectConfig), anyString(), (AsyncRequestCallback<List<Branch>>)anyObject());
        verify(view, times(2)).setEnablePullButton(eq(ENABLE_BUTTON));
        verify(view).setRepositories((List<Remote>)anyObject());
        verify(view).showDialog();
        verify(view).setRemoteBranches((List<String>)anyObject());
        verify(view).setLocalBranches((List<String>)anyObject());
        verify(view).selectRemoteBranch(anyString());

        presenter.onRemoteBranchChanged();
        verify(view).selectLocalBranch(anyString());
    }

    @Test
    public void testShowDialogWhenBranchListRequestIsFailed() throws Exception {
        final List<Remote> remotes = new ArrayList<>();
        remotes.add(mock(Remote.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Remote>> callback = (AsyncRequestCallback<List<Remote>>)arguments[3];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, remotes);
                return callback;
            }
        }).when(service)
          .remoteList(anyObject(), anyObject(), anyString(), anyBoolean(), (AsyncRequestCallback<List<Remote>>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).branchList(anyObject(), anyObject(), anyString(), (AsyncRequestCallback<List<Branch>>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service).remoteList(anyObject(), eq(rootProjectConfig), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(constant).branchesListFailed();
        verify(gitOutputConsoleFactory).create(anyString());
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(view).setEnablePullButton(eq(DISABLE_BUTTON));
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
        }).when(service).remoteList(anyObject(), anyObject(), anyString(), anyBoolean(),
                                    (AsyncRequestCallback<List<Remote>>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service).remoteList(anyObject(), eq(rootProjectConfig), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(constant).remoteListFailed();
        verify(view).setEnablePullButton(eq(DISABLE_BUTTON));
    }

    @Test
    public void testOnPullClickedWhenPullHasChanges() throws Exception {
        when(pullResponse.getCommandOutput()).thenReturn("Something pulled");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[3];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, pullResponse);
                return callback;
            }
        }).when(service).pull(anyObject(), anyObject(), anyString(), anyString(), (AsyncRequestCallback<PullResponse>)anyObject());

        presenter.showDialog();
        presenter.onPullClicked();

        verify(view, times(2)).getRepositoryName();
        verify(view).getRepositoryUrl();
        verify(view).close();
        verify(editorAgent).getOpenedEditors();
        verify(service).pull(anyObject(), eq(rootProjectConfig), anyString(), eq(REPOSITORY_NAME), (AsyncRequestCallback)anyObject());
        verify(gitOutputConsoleFactory).create(PULL_COMMAND_NAME);
        verify(console).printInfo(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(constant).pullSuccess(anyString());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(appContext).getCurrentProject();
        verify(eventBus).fireEvent(Matchers.<Event<GwtEvent>>anyObject());
        verify(partPresenter).getEditorInput();
        verify(file).getPath();
    }

    @Test
    public void testOnPullClickedWhenPullIsUpToDate() throws Exception {
        when(pullResponse.getCommandOutput()).thenReturn("Already up-to-date");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[3];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, pullResponse);
                return callback;
            }
        }).when(service).pull(anyObject(), anyObject(), anyString(), anyString(), (AsyncRequestCallback<PullResponse>)anyObject());

        presenter.showDialog();
        presenter.onPullClicked();

        verify(view, times(2)).getRepositoryName();
        verify(view).getRepositoryUrl();
        verify(view).close();
        verify(editorAgent).getOpenedEditors();
        verify(service).pull(anyObject(), eq(rootProjectConfig), anyString(), eq(REPOSITORY_NAME), (AsyncRequestCallback)anyObject());
        verify(gitOutputConsoleFactory).create(PULL_COMMAND_NAME);
        verify(console).printInfo(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(constant).pullUpToDate();
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(appContext).getCurrentProject();
        verify(eventBus, never()).fireEvent(Matchers.<Event<GwtEvent>>anyObject());
        verify(partPresenter, never()).getEditorInput();
        verify(file, never()).getPath();
    }

    @Test
    public void testOnPullClickedWhenPullRequestIsFailed() throws Exception {
        final Throwable exception = mock(Throwable.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[3];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, exception);
                return callback;
            }
        }).when(service).pull(anyObject(), anyObject(), anyString(), anyString(), (AsyncRequestCallback<PullResponse>)anyObject());
        when(exception.getMessage()).thenReturn("error Message");

        presenter.showDialog();
        presenter.onPullClicked();

        verify(service).pull(anyObject(), eq(rootProjectConfig), anyString(), eq(REPOSITORY_NAME),
                             (AsyncRequestCallback<PullResponse>)anyObject());
        verify(view).close();
        verify(gitOutputConsoleFactory).create(PULL_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnPullClickedWhenMergeConflictHappenedAndRefreshProjectIsSuccessful() throws Exception {
        final Throwable exception = mock(Throwable.class);
        when(exception.getMessage()).thenReturn("Merge conflict");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[3];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, exception);
                return callback;
            }
        }).when(service).pull(anyObject(), anyObject(), anyString(), anyString(), (AsyncRequestCallback<PullResponse>)anyObject());

        presenter.showDialog();
        presenter.onPullClicked();

        verify(view).close();
        verify(service).pull(anyObject(), eq(rootProjectConfig), anyString(), eq(REPOSITORY_NAME), (AsyncRequestCallback)anyObject());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(gitOutputConsoleFactory).create(PULL_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(appContext).getCurrentProject();
        verify(eventBus, times(1)).fireEvent(Matchers.<Event<GwtEvent>>anyObject());
        verify(partPresenter).getEditorInput();
    }

    @Test
    public void testOnPullClickedWhenAlreadyUpToDateHappenedAndRefreshProjectIsNotCalled() throws Exception {
        when(pullResponse.getCommandOutput()).thenReturn("Already up-to-date");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[3];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, pullResponse);
                return callback;
            }
        }).when(service).pull(anyObject(), anyObject(), anyString(), anyString(), (AsyncRequestCallback<PullResponse>)anyObject());

        presenter.showDialog();
        presenter.onPullClicked();

        verify(view).close();
        verify(gitOutputConsoleFactory).create(PULL_COMMAND_NAME);
        verify(console).printInfo(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        //check Refresh project is not called
        verify(eventBus, never()).fireEvent(Matchers.<Event<GwtEvent>>anyObject());
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void shouldRefreshRemoteBranchesWhenRepositoryIsChanged() throws Exception {
        final List<Remote> remotes = new ArrayList<>();
        remotes.add(mock(Remote.class));
        final List<Branch> branches = new ArrayList<>();
        branches.add(branch);
        when(branch.isActive()).thenReturn(ACTIVE_BRANCH);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<List<Branch>> callback = (AsyncRequestCallback<List<Branch>>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, branches);
                return callback;
            }
        }).when(service).branchList(anyObject(), anyObject(), anyString(), (AsyncRequestCallback<List<Branch>>)anyObject());

        presenter.onRemoteRepositoryChanged();

        verify(service, times(2))
                .branchList(anyObject(), eq(rootProjectConfig), anyString(), (AsyncRequestCallback<List<Branch>>)anyObject());
        verify(view).setRemoteBranches((List<String>)anyObject());
        verify(view).setLocalBranches((List<String>)anyObject());
        verify(view).selectRemoteBranch(anyString());
    }
}
