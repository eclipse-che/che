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
package org.eclipse.che.ide.ext.git.client.status;

import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Testing {@link StatusCommandPresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class StatusCommandPresenterTest extends BaseTest {
    public static final StatusFormat IS_NOT_FORMATTED = StatusFormat.LONG;
    @InjectMocks
    private StatusCommandPresenter presenter;

    @Mock
    private WorkspaceAgent workspaceAgent;

    @Mock
    private GitOutputConsoleFactory gitOutputConsoleFactory;

    @Mock
    private ConsolesPanelPresenter consolesPanelPresenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new StatusCommandPresenter(service,
                                               appContext,
                                               gitOutputConsoleFactory,
                                               consolesPanelPresenter,
                                               constant,
                                               notificationManager);
    }

    @Test
    public void testShowStatusWhenStatusTextRequestIsSuccessful() throws Exception {
        doAnswer(new Answer<AsyncRequestCallback<String>>() {
            @Override
            public AsyncRequestCallback<String> answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, EMPTY_TEXT);
                return callback;
            }
        }).when(service).statusText(anyString(),
                                    Matchers.<ProjectConfigDto>anyObject(),
                                    Matchers.<StatusFormat>anyObject(),
                                    Matchers.<AsyncRequestCallback<String>>anyObject());

        presenter.showStatus();

        verify(appContext).getCurrentProject();
        verify(service).statusText(anyString(),
                                   eq(rootProjectConfig),
                                   eq(IS_NOT_FORMATTED),
                                   Matchers.<AsyncRequestCallback<String>>anyObject());
    }

    @Test
    public void testShowStatusWhenStatusTextRequestIsFailed() throws Exception {
        doAnswer(new Answer<AsyncRequestCallback<String>>() {
            @Override
            public AsyncRequestCallback<String> answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).statusText(anyString(),
                                    Matchers.<ProjectConfigDto>anyObject(),
                                    Matchers.<StatusFormat>anyObject(),
                                    Matchers.<AsyncRequestCallback<String>>anyObject());

        presenter.showStatus();

        verify(appContext).getCurrentProject();
        verify(service).statusText(anyString(), eq(rootProjectConfig), eq(IS_NOT_FORMATTED), Matchers.<AsyncRequestCallback<String>>anyObject());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(constant).statusFailed();
    }

}
