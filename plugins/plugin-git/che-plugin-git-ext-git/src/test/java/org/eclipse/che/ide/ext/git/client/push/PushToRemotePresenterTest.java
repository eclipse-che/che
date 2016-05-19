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
package org.eclipse.che.ide.ext.git.client.push;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.BranchFilterByRemote;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.eclipse.che.ide.ext.git.client.push.PushToRemotePresenter.PUSH_COMMAND_NAME;

/**
 * Testing {@link PushToRemotePresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Sergii Leschenko
 */
public class PushToRemotePresenterTest extends BaseTest {
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<String>>              asyncRequestCallbackStringCaptor;
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<List<Remote>>>        asyncRequestCallbackArrayRemoteCaptor;
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<List<Branch>>>        asyncRequestCallbackArrayBranchCaptor;
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<Map<String, String>>> asyncRequestCallbackMapCaptor;
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<Branch>>              asyncRequestCallbackBranchCaptor;
    @Captor
    private ArgumentCaptor<AsyncCallback<List<Branch>>>               asyncCallbackArrayBranchCaptor;
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<PushResponse>>        asyncCallbackVoidCaptor;

    @Mock
    private PushToRemoteView   view;
    @Mock
    private Branch             localBranch;
    @Mock
    private Branch             remoteBranch;
    @Mock
    private BranchSearcher     branchSearcher;
    @Mock
    private PushResponse       pushResponse;
    @Mock
    private StatusNotification notification;
    @Mock
    private WorkspaceDto       workspaceDto;

    private PushToRemotePresenter presenter;

    public static final boolean SHOW_ALL_INFORMATION = true;
    public static final boolean DISABLE_CHECK        = false;

    public void disarm() {
        super.disarm();
        when(view.getRepository()).thenReturn(REPOSITORY_NAME);
        when(view.getLocalBranch()).thenReturn(LOCAL_BRANCH);
        when(view.getRemoteBranch()).thenReturn(REMOTE_BRANCH);

        when(localBranch.getName()).thenReturn("refs/heads/" + LOCAL_BRANCH);
        when(localBranch.getDisplayName()).thenReturn(LOCAL_BRANCH);
        when(localBranch.isActive()).thenReturn(true);
        when(localBranch.isRemote()).thenReturn(false);

        when(remoteBranch.getName()).thenReturn("refs/remotes/" + REPOSITORY_NAME + "/" + REMOTE_BRANCH);
        when(remoteBranch.getDisplayName()).thenReturn(REMOTE_BRANCH);
        when(remoteBranch.isActive()).thenReturn(true);
        when(remoteBranch.isRemote()).thenReturn(false);

        presenter = new PushToRemotePresenter(dtoFactory,
                                              view,
                                              service,
                                              appContext,
                                              constant,
                                              notificationManager,
                                              dtoUnmarshallerFactory,
                                              branchSearcher,
                                              gitOutputConsoleFactory,
                                              consolesPanelPresenter);
    }

    @Test
    public void testShowListOfLocalBranchesAndStartUpdateRemoteBranches() throws Exception {
        final List<Branch> localBranches = new ArrayList<>();
        localBranches.add(localBranch);

        presenter.updateLocalBranches();

        verify(service).branchList(eq(devMachine), anyObject(), (String)eq(null), asyncRequestCallbackArrayBranchCaptor.capture());
        AsyncRequestCallback<List<Branch>> value = asyncRequestCallbackArrayBranchCaptor.getValue();
        Method onSuccessRemotes = GwtReflectionUtils.getMethod(value.getClass(), "onSuccess");
        onSuccessRemotes.invoke(value, localBranches);

        verify(branchSearcher).getLocalBranchesToDisplay(eq(localBranches));
        verify(view).setLocalBranches((List<String>)anyObject());
        verify(view).selectLocalBranch(localBranch.getDisplayName());
    }

    @Test
    public void testShowListOfRemoteBranchesAndSelectUpstreamBranch() throws Exception {
        final List<Branch> remoteBranches = new ArrayList<>();
        remoteBranches.add(remoteBranch);

        final String upstreamBranchName = "upstream";
        Map<String, String> configs = new HashMap<>();
        configs.put("branch." + view.getLocalBranch() + ".remote", REPOSITORY_NAME);
        configs.put("branch." + view.getLocalBranch() + ".merge", upstreamBranchName);

        Branch upstream = mock(Branch.class);
        when(dtoFactory.createDto(eq(Branch.class))).thenReturn(upstream);
        when(upstream.withActive(anyBoolean())).thenReturn(upstream);
        when(upstream.withRemote(anyBoolean())).thenReturn(upstream);
        when(upstream.withDisplayName(anyString())).thenReturn(upstream);
        when(upstream.withName(anyString())).thenReturn(upstream);

        when(upstream.getDisplayName()).thenReturn(upstreamBranchName);
        when(upstream.getName()).thenReturn("refs/remotes/" + REPOSITORY_NAME + "/" + upstreamBranchName);
        when(upstream.isRemote()).thenReturn(true);

        List<String> list = new ArrayList<>();
        list.add(remoteBranch.getDisplayName());
        when(branchSearcher.getRemoteBranchesToDisplay((BranchFilterByRemote)anyObject(), (List<Branch>)anyObject())).thenReturn(list);

        presenter.updateRemoteBranches();

        verify(service).branchList(eq(devMachine), anyObject(), eq("r"), asyncRequestCallbackArrayBranchCaptor.capture());
        AsyncRequestCallback<List<Branch>> value = asyncRequestCallbackArrayBranchCaptor.getValue();
        Method onSuccessRemotes = GwtReflectionUtils.getMethod(value.getClass(), "onSuccess");
        onSuccessRemotes.invoke(value, remoteBranches);

        verify(service).config(eq(devMachine), anyObject(), anyListOf(String.class), anyBoolean(),
                               asyncRequestCallbackMapCaptor.capture());
        AsyncRequestCallback<Map<String, String>> mapCallback = asyncRequestCallbackMapCaptor.getValue();
        Method onSuccessConfig = GwtReflectionUtils.getMethod(mapCallback.getClass(), "onSuccess");
        onSuccessConfig.invoke(mapCallback, configs);

        verify(branchSearcher).getRemoteBranchesToDisplay((BranchFilterByRemote)anyObject(), eq(remoteBranches));
        verify(view).setRemoteBranches((List<String>)anyObject());
        verify(view).selectRemoteBranch(upstreamBranchName);
    }

    @Test
    public void testShowListOfRemoteBranchesAndSelectActiveLocalBranch() throws Exception {
        final List<Branch> remoteBranches = new ArrayList<>();
        remoteBranches.add(remoteBranch);

        List<String> list = new ArrayList<>();
        list.add(remoteBranch.getDisplayName());
        when(branchSearcher.getRemoteBranchesToDisplay((BranchFilterByRemote)anyObject(), (List<Branch>)anyObject())).thenReturn(list);

        presenter.updateRemoteBranches();

        verify(service).branchList(eq(devMachine), anyObject(), eq("r"), asyncRequestCallbackArrayBranchCaptor.capture());
        AsyncRequestCallback<List<Branch>> value = asyncRequestCallbackArrayBranchCaptor.getValue();
        Method onSuccessRemotes = GwtReflectionUtils.getMethod(value.getClass(), "onSuccess");
        onSuccessRemotes.invoke(value, remoteBranches);

        verify(service).config(eq(devMachine), anyObject(), anyListOf(String.class), anyBoolean(),
                               asyncRequestCallbackMapCaptor.capture());
        AsyncRequestCallback<Map<String, String>> mapCallback = asyncRequestCallbackMapCaptor.getValue();
        Method onSuccessConfig = GwtReflectionUtils.getMethod(mapCallback.getClass(), "onSuccess");
        onSuccessConfig.invoke(mapCallback, new HashMap());

        verify(branchSearcher).getRemoteBranchesToDisplay((BranchFilterByRemote)anyObject(), eq(remoteBranches));
        verify(view).setRemoteBranches((List<String>)anyObject());
        verify(view).selectRemoteBranch(LOCAL_BRANCH);
    }

    @Test
    public void testShowErrorNotificationWhenListOfRemoteBranchesFailedToLoad() throws Exception {
        presenter.updateRemoteBranches();

        verify(service).branchList(eq(devMachine), anyObject(), anyString(), asyncRequestCallbackArrayBranchCaptor.capture());
        AsyncRequestCallback<List<Branch>> value = asyncRequestCallbackArrayBranchCaptor.getValue();
        Method onFailureRemotes = GwtReflectionUtils.getMethod(value.getClass(), "onFailure");
        onFailureRemotes.invoke(value, mock(Throwable.class));


        verify(constant, times(2)).remoteBranchesListFailed();
        verify(gitOutputConsoleFactory).create(anyString());
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testShowErrorNotificationWhenUpstreanBranchFailedToLoad() throws Exception {
        presenter.updateRemoteBranches();

        verify(service).branchList(eq(devMachine), anyObject(), eq("r"), asyncRequestCallbackArrayBranchCaptor.capture());
        AsyncRequestCallback<List<Branch>> remoteBranches = asyncRequestCallbackArrayBranchCaptor.getValue();
        Method onSuccess = GwtReflectionUtils.getMethod(remoteBranches.getClass(), "onSuccess");
        onSuccess.invoke(remoteBranches, new ArrayList<>());

        verify(service).config(eq(devMachine), anyObject(), anyListOf(String.class), anyBoolean(),
                               asyncRequestCallbackMapCaptor.capture());
        AsyncRequestCallback<Map<String, String>> mapCallback = asyncRequestCallbackMapCaptor.getValue();
        Method onFailureConfig = GwtReflectionUtils.getMethod(mapCallback.getClass(), "onFailure");
        onFailureConfig.invoke(mapCallback, mock(Throwable.class));


        verify(constant, times(2)).failedGettingConfig();
        verify(gitOutputConsoleFactory).create(anyString());
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testUpdatingRemoteBranchesOnViewWhenLocalBranchChange() {
        presenter.onLocalBranchChanged();

        verify(view).addRemoteBranch(LOCAL_BRANCH);
        verify(view).selectRemoteBranch(LOCAL_BRANCH);
    }

    @Test
    public void testShowDialogWhenLocalBranchListRequestIsFailed() throws Exception {
        final List<Remote> remotes = new ArrayList<>();
        remotes.add(mock(Remote.class));

        presenter.showDialog();

        verify(service).remoteList(eq(devMachine), anyObject(), anyString(), anyBoolean(),
                                   asyncRequestCallbackArrayRemoteCaptor.capture());
        AsyncRequestCallback<List<Remote>> remoteCallback = asyncRequestCallbackArrayRemoteCaptor.getValue();
        Method onSuccessRemotes = GwtReflectionUtils.getMethod(remoteCallback.getClass(), "OnSuccess");
        onSuccessRemotes.invoke(remoteCallback, remotes);

        verify(service).branchList(eq(devMachine), anyObject(), anyString(), asyncRequestCallbackArrayBranchCaptor.capture());
        AsyncRequestCallback<List<Branch>> branchesCallback = asyncRequestCallbackArrayBranchCaptor.getValue();
        Method onFailureBranches = GwtReflectionUtils.getMethod(branchesCallback.getClass(), "onFailure");
        onFailureBranches.invoke(branchesCallback, mock(Throwable.class));

        verify(appContext).getCurrentProject();
        verify(service).remoteList(eq(devMachine), eq(rootProjectConfig), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());

        verify(constant, times(2)).localBranchesListFailed();
        verify(gitOutputConsoleFactory).create(anyString());
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(view).setEnablePushButton(eq(DISABLE_BUTTON));
    }

    @Test
    public void testShowDialogWhenRemoteListRequestIsFailed() throws Exception {
        presenter.showDialog();

        verify(service).remoteList(eq(devMachine), anyObject(), anyString(), anyBoolean(),
                                   asyncRequestCallbackArrayRemoteCaptor.capture());
        AsyncRequestCallback<List<Remote>> branchesCallback = asyncRequestCallbackArrayRemoteCaptor.getValue();
        Method onFailureBranches = GwtReflectionUtils.getMethod(branchesCallback.getClass(), "onFailure");
        onFailureBranches.invoke(branchesCallback, mock(Throwable.class));

        verify(appContext).getCurrentProject();
        verify(service).remoteList(eq(devMachine), eq(rootProjectConfig), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(constant, times(2)).remoteListFailed();
        verify(gitOutputConsoleFactory).create(anyString());
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(view).setEnablePushButton(eq(DISABLE_BUTTON));
    }

    @Test
    public void testOnPushClickedWhenPusHasChanges() throws Exception {
        when(pushResponse.getCommandOutput()).thenReturn("Something pushed");

        presenter.showDialog();
        presenter.onPushClicked();

        verify(service).push(eq(devMachine), anyObject(), anyListOf(String.class), anyString(), anyBoolean(),
                             asyncCallbackVoidCaptor.capture());
        AsyncRequestCallback<PushResponse> voidCallback = asyncCallbackVoidCaptor.getValue();
        Method onSuccess = GwtReflectionUtils.getMethod(voidCallback.getClass(), "onSuccess");
        onSuccess.invoke(voidCallback, pushResponse);

        verify(service).push(eq(devMachine), eq(rootProjectConfig), anyListOf(String.class), eq(REPOSITORY_NAME), eq(DISABLE_CHECK),
                             (AsyncRequestCallback<PushResponse>)anyObject());
        verify(view).close();
        verify(gitOutputConsoleFactory).create(PUSH_COMMAND_NAME);
        verify(console).print(anyString());
        verify(constant).pushSuccess(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnPushClickedWhenPusIsUpToDate() throws Exception {
        when(pushResponse.getCommandOutput()).thenReturn("Everything up-to-date");

        presenter.showDialog();
        presenter.onPushClicked();

        verify(service).push(eq(devMachine), anyObject(), anyListOf(String.class), anyString(), anyBoolean(),
                             asyncCallbackVoidCaptor.capture());
        AsyncRequestCallback<PushResponse> voidCallback = asyncCallbackVoidCaptor.getValue();
        Method onSuccess = GwtReflectionUtils.getMethod(voidCallback.getClass(), "onSuccess");
        onSuccess.invoke(voidCallback, pushResponse);

        verify(service).push(eq(devMachine), eq(rootProjectConfig), anyListOf(String.class), eq(REPOSITORY_NAME), eq(DISABLE_CHECK),
                             (AsyncRequestCallback<PushResponse>)anyObject());
        verify(view).close();
        verify(gitOutputConsoleFactory).create(PUSH_COMMAND_NAME);
        verify(console).print(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(constant).pushUpToDate();
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnPushClickedWhenPushWSRequestIsFailed() throws Exception {
        presenter.showDialog();
        presenter.onPushClicked();

        verify(service).push(eq(devMachine), anyObject(), anyListOf(String.class), anyString(), anyBoolean(),
                             asyncCallbackVoidCaptor.capture());
        AsyncRequestCallback<PushResponse> voidCallback = asyncCallbackVoidCaptor.getValue();
        Method onFailure = GwtReflectionUtils.getMethod(voidCallback.getClass(), "onFailure");
        onFailure.invoke(voidCallback, mock(Throwable.class));

        verify(service).push(eq(devMachine), eq(rootProjectConfig), anyListOf(String.class), eq(REPOSITORY_NAME), eq(DISABLE_CHECK),
                             (AsyncRequestCallback<PushResponse>)anyObject());
        verify(view).close();
        verify(constant, times(2)).pushFail();
        verify(gitOutputConsoleFactory).create(PUSH_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void testShowDefaultMessageOnUnauthorizedException() {
        when(constant.messagesNotAuthorized()).thenReturn("Not authorized");

        presenter.handleError(mock(UnauthorizedException.class), notification, console);

        verify(notification).setContent("Not authorized");
    }

    @Test
    public void testShowDefaultMessageOnExceptionWithoutMessage() {
        when(constant.pushFail()).thenReturn("Push fail");

        presenter.handleError(mock(Throwable.class), notification, console);

        verify(notification).setContent("Push fail");
    }

    @Test
    public void testShowDefaultMessageOnException() {
        String errorMessage = "Error";
        Exception exception = mock(Exception.class);
        when(exception.getMessage()).thenReturn(errorMessage);

        presenter.handleError(exception, notification, console);

        verify(notification).setContent("Error");
    }

    @Test
    public void testShowDialogWhenAllRequestsAreSuccessful() throws Exception {
        final List<Remote> remotes = new ArrayList<>();
        remotes.add(mock(Remote.class));
        final List<Branch> branches = new ArrayList<>();
        branches.add(localBranch);

        final List<String> names_branches = new ArrayList<>();
        names_branches.add(LOCAL_BRANCH);

        when(branchSearcher.getLocalBranchesToDisplay((List<Branch>)anyObject())).thenReturn(names_branches);

        List<String> list = new ArrayList<>();
        list.add(remoteBranch.getDisplayName());
        when(branchSearcher.getRemoteBranchesToDisplay((BranchFilterByRemote)anyObject(), (List<Branch>)anyObject())).thenReturn(list);

        presenter.showDialog();

        verify(service).remoteList(eq(devMachine), anyObject(), anyString(), anyBoolean(),
                                   asyncRequestCallbackArrayRemoteCaptor.capture());

        AsyncRequestCallback<List<Remote>> remotesCallback = asyncRequestCallbackArrayRemoteCaptor.getValue();
        //noinspection NonJREEmulationClassesInClientCode
        Method onSuccessRemotes = GwtReflectionUtils.getMethod(remotesCallback.getClass(), "onSuccess");
        onSuccessRemotes.invoke(remotesCallback, remotes);

        verify(service).branchList(eq(devMachine), anyObject(), anyString(), asyncRequestCallbackArrayBranchCaptor.capture());
        AsyncRequestCallback<List<Branch>> branchesCallback = asyncRequestCallbackArrayBranchCaptor.getValue();
        //noinspection NonJREEmulationClassesInClientCode
        Method onSuccessBranches = GwtReflectionUtils.getMethod(branchesCallback.getClass(), "onSuccess");
        onSuccessBranches.invoke(branchesCallback, branches);

        verify(service, times(2)).branchList(eq(devMachine), anyObject(), anyString(), asyncRequestCallbackArrayBranchCaptor.capture());
        branchesCallback = asyncRequestCallbackArrayBranchCaptor.getValue();
        //noinspection NonJREEmulationClassesInClientCode
        onSuccessBranches = GwtReflectionUtils.getMethod(branchesCallback.getClass(), "onSuccess");
        onSuccessBranches.invoke(branchesCallback, branches);

        verify(service).config(eq(devMachine), anyObject(), anyListOf(String.class), anyBoolean(),
                               asyncRequestCallbackMapCaptor.capture());
        AsyncRequestCallback<Map<String, String>> value = asyncRequestCallbackMapCaptor.getValue();
        Method config = GwtReflectionUtils.getMethod(value.getClass(), "onSuccess");
        config.invoke(value, new HashMap<String, String>());

        verify(appContext).getCurrentProject();
        verify(service).remoteList(eq(devMachine), eq(rootProjectConfig), anyString(), eq(SHOW_ALL_INFORMATION),
                                   (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(view).setEnablePushButton(eq(ENABLE_BUTTON));
        verify(view).setRepositories((List<Remote>)anyObject());
        verify(view).showDialog();
        verify(view).setRemoteBranches((List<String>)anyObject());
        verify(view).setLocalBranches((List<String>)anyObject());
    }
}
