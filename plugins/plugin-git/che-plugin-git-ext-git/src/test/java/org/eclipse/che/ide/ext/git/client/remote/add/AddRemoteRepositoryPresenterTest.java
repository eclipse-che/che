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
package org.eclipse.che.ide.ext.git.client.remote.add;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link AddRemoteRepositoryPresenter} functionality.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class AddRemoteRepositoryPresenterTest extends BaseTest {
    @Mock
    private AddRemoteRepositoryView      view;
    @Mock
    private AsyncCallback<Void>          callback;
    private AddRemoteRepositoryPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new AddRemoteRepositoryPresenter(view, service, appContext);

        when(view.getName()).thenReturn(REMOTE_NAME);
        when(view.getUrl()).thenReturn(REMOTE_URI);
    }

    @Test
    public void testShowDialog() throws Exception {
        presenter.showDialog(callback);

        verify(view).setUrl(eq(EMPTY_TEXT));
        verify(view).setName(eq(EMPTY_TEXT));
        verify(view).setEnableOkButton(eq(DISABLE_BUTTON));
        verify(view).showDialog();
    }

    @Test
    public void testOnOkClickedWhenRemoteAddRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[3];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, EMPTY_TEXT);
                return callback;
            }
        }).when(service).remoteAdd(anyString(), anyObject(), anyString(), anyString(), (AsyncRequestCallback<String>)anyObject());

        presenter.showDialog(callback);
        presenter.onOkClicked();

        verify(service).remoteAdd(anyString(), anyObject(), eq(REMOTE_NAME), eq(REMOTE_URI), (AsyncRequestCallback<String>)anyObject());
        verify(callback).onSuccess(eq((Void)null));
        verify(callback, never()).onFailure((Throwable)anyObject());
        verify(view).close();
    }

    @Test
    public void testOnOkClickedWhenRemoteAddRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[3];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).remoteAdd(anyString(), anyObject(), anyString(), anyString(), (AsyncRequestCallback<String>)anyObject());


        presenter.showDialog(callback);
        presenter.onOkClicked();

        verify(service).remoteAdd(anyString(), anyObject(), eq(REMOTE_NAME), eq(REMOTE_URI), (AsyncRequestCallback<String>)anyObject());
        verify(callback).onFailure((Throwable)anyObject());
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void testOnValueChangedEnableButton() throws Exception {
        presenter.onValueChanged();

        verify(view).setEnableOkButton(eq(ENABLE_BUTTON));
    }

    @Test
    public void testOnValueChangedDisableButton() throws Exception {
        when(view.getName()).thenReturn(EMPTY_TEXT);
        when(view.getUrl()).thenReturn(EMPTY_TEXT);

        presenter.onValueChanged();

        verify(view).setEnableOkButton(eq(DISABLE_BUTTON));
    }
}