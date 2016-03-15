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
package org.eclipse.che.ide.ext.git.client.delete;

import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.ui.window.Window;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.eclipse.che.ide.ext.git.client.delete.DeleteRepositoryPresenter.DELETE_REPO_COMMAND_NAME;

/**
 * Testing {@link DeleteRepositoryPresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class DeleteRepositoryPresenterTest extends BaseTest {
    private DeleteRepositoryPresenter presenter;

    @Mock
    Window.Resources resources;

    @Mock
    Window.Css css;

    @Override
    public void disarm() {
        super.disarm();
        when(resources.windowCss()).thenReturn(css);
        when(css.alignBtn()).thenReturn("sdgsdf");
        when(css.glassVisible()).thenReturn("sdgsdf");
        when(css.contentVisible()).thenReturn("sdgsdf");
        when(css.animationDuration()).thenReturn(1);
        presenter = new DeleteRepositoryPresenter(service,
                                                  constant,
                                                  gitOutputConsoleFactory,
                                                  consolesPanelPresenter,
                                                  appContext,
                                                  notificationManager,
                                                  projectServiceClient,
                                                  dtoUnmarshallerFactory,
                                                  eventBus);
    }

    @Test
    public void testDeleteRepositoryWhenDeleteRepositoryIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service).deleteRepository(anyString(), anyObject(), (AsyncRequestCallback<Void>)anyObject());

        presenter.deleteRepository();

        verify(appContext).getCurrentProject();
        verify(service).deleteRepository(anyString(), eq(rootProjectConfig), (AsyncRequestCallback<Void>)anyObject());
        verify(gitOutputConsoleFactory).create(DELETE_REPO_COMMAND_NAME);
        verify(console).print(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testDeleteRepositoryWhenDeleteRepositoryIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).deleteRepository(anyString(), anyObject(), (AsyncRequestCallback<Void>)anyObject());

        presenter.deleteRepository();

        verify(appContext).getCurrentProject();
        verify(service).deleteRepository(anyString(), eq(rootProjectConfig), (AsyncRequestCallback<Void>)anyObject());
        verify(gitOutputConsoleFactory).create(DELETE_REPO_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }
}
