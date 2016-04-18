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
package org.eclipse.che.ide.ext.git.client.url;

import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link ShowProjectGitReadOnlyUrlPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Oleksii Orel
 */
public class ShowProjectGitReadOnlyUrlPresenterTest extends BaseTest {

    @Captor
    private ArgumentCaptor<AsyncRequestCallback<String>> asyncRequestCallbackGitReadOnlyUrlCaptor;

    @Captor
    private ArgumentCaptor<AsyncRequestCallback<List<Remote>>> asyncRequestCallbackRemoteListCaptor;

    @Mock
    private ShowProjectGitReadOnlyUrlView      view;
    private ShowProjectGitReadOnlyUrlPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        when(appContext.getWorkspaceId()).thenReturn("id");

        presenter = new ShowProjectGitReadOnlyUrlPresenter(view,
                                                           service,
                                                           appContext,
                                                           constant,
                                                           notificationManager,
                                                           dtoUnmarshallerFactory,
                                                           gitOutputConsoleFactory,
                                                           consolesPanelPresenter);
    }

    @Test
    public void getGitReadOnlyUrlAsyncCallbackIsSuccess() throws Exception {
        presenter.showDialog();
        verify(service).getGitReadOnlyUrl(eq(devMachine), anyObject(), asyncRequestCallbackGitReadOnlyUrlCaptor.capture());
        AsyncRequestCallback<String> callback = asyncRequestCallbackGitReadOnlyUrlCaptor.getValue();

        Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
        onSuccess.invoke(callback, LOCALE_URI);

        verify(appContext).getCurrentProject();
        verify(service).getGitReadOnlyUrl(eq(devMachine), eq(rootProjectConfig), (AsyncRequestCallback<String>)anyObject());
        verify(view).setLocaleUrl(eq(LOCALE_URI));
    }

    @Test
    public void getGitReadOnlyUrlAsyncCallbackIsFailed() throws Exception {
        presenter.showDialog();
        verify(service).getGitReadOnlyUrl(eq(devMachine), anyObject(), asyncRequestCallbackGitReadOnlyUrlCaptor.capture());
        AsyncRequestCallback<String> callback = asyncRequestCallbackGitReadOnlyUrlCaptor.getValue();

        Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
        onSuccess.invoke(callback, mock(Throwable.class));

        verify(appContext).getCurrentProject();
        verify(service).getGitReadOnlyUrl(eq(devMachine), eq(rootProjectConfig), (AsyncRequestCallback<String>)anyObject());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(console).printError(anyString());
        verify(constant).initFailed();
    }

    @Test
    public void getGitRemoteListAsyncCallbackIsSuccess() throws Exception {
        final List<Remote> remotes = new ArrayList<>();
        remotes.add(mock(Remote.class));
        presenter.showDialog();
        verify(service)
                .remoteList(eq(devMachine), anyObject(), anyString(), anyBoolean(), asyncRequestCallbackRemoteListCaptor.capture());
        AsyncRequestCallback<List<Remote>> callback = asyncRequestCallbackRemoteListCaptor.getValue();

        Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
        onSuccess.invoke(callback, remotes);

        verify(appContext).getCurrentProject();
        verify(service).remoteList(eq(devMachine), eq(rootProjectConfig), anyString(), eq(true), (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(view).setRemotes((List<Remote>)anyObject());
    }

    @Test
    public void getGitRemoteListAsyncCallbackIsFailed() throws Exception {
        final List<Remote> remotes = new ArrayList<>();
        remotes.add(mock(Remote.class));
        presenter.showDialog();
        verify(service)
                .remoteList(eq(devMachine), anyObject(), anyString(), anyBoolean(), asyncRequestCallbackRemoteListCaptor.capture());
        AsyncRequestCallback<List<Remote>> callback = asyncRequestCallbackRemoteListCaptor.getValue();

        Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
        onSuccess.invoke(callback, mock(Throwable.class));

        verify(appContext).getCurrentProject();
        verify(service).remoteList(eq(devMachine), eq(rootProjectConfig), anyString(), eq(true), (AsyncRequestCallback<List<Remote>>)anyObject());
        verify(view).setRemotes(null);
        verify(console).printError(anyString());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnCloseClicked() throws Exception {
        presenter.onCloseClicked();

        verify(view).close();
    }
}
