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
package org.eclipse.che.ide.ext.java.jdi.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.ide.ext.java.jdi.client.debug.changevalue.ChangeValuePresenter;
import org.eclipse.che.ide.ext.java.jdi.client.debug.changevalue.ChangeValueView;
import org.eclipse.che.ide.ext.java.jdi.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.java.jdi.shared.Variable;
import org.eclipse.che.ide.ext.java.jdi.shared.VariablePath;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
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

/**
 * Testing {@link ChangeValuePresenter} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class ChangeVariableValueTest extends BaseTest {
    private static final String VAR_VALUE   = "var_value";
    private static final String VAR_NAME    = "var_name";
    private static final String EMPTY_VALUE = "";
    @Mock
    private ChangeValueView       view;
    @InjectMocks
    private ChangeValuePresenter  presenter;
    @Mock
    private Variable              var;
    @Mock
    private VariablePath          varPath;
    @Mock
    private AsyncCallback<String> asyncCallback;

    @Before
    public void setUp() {
        super.setUp();
        when(var.getName()).thenReturn(VAR_NAME);
        when(var.getValue()).thenReturn(VAR_VALUE);
        when(var.getVariablePath()).thenReturn(varPath);
        when(dtoFactory.createDto(UpdateVariableRequest.class)).thenReturn(mock(UpdateVariableRequest.class));
    }

    @Test
    public void shouldShowDialog() throws Exception {
        presenter.showDialog(debuggerInfo, var, asyncCallback);

        verify(view).setValueTitle(constants.changeValueViewExpressionFieldTitle(VAR_NAME));
        verify(view).setValue(VAR_VALUE);
        verify(view).focusInValueField();
        verify(view).selectAllText();
        verify(view).setEnableChangeButton(eq(DISABLE_BUTTON));
        verify(view).showDialog();
    }

    @Test
    public void shouldCloseDialogOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void shouldDisableChangeButtonIfNoValue() throws Exception {
        when(view.getValue()).thenReturn(EMPTY_VALUE);

        presenter.onVariableValueChanged();

        verify(view).setEnableChangeButton(eq(DISABLE_BUTTON));
    }

    @Test
    public void shouldEnableChangeButtonIfValueNotEmpty() throws Exception {
        when(view.getValue()).thenReturn(VAR_VALUE);

        presenter.onVariableValueChanged();

        verify(view).setEnableChangeButton(eq(!DISABLE_BUTTON));
    }

    @Test
    public void testChangeValueRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service).setValue(anyString(), (UpdateVariableRequest)anyObject(), (AsyncRequestCallback<Void>)anyObject());
        when(view.getValue()).thenReturn(VAR_VALUE);

        presenter.showDialog(debuggerInfo, var, asyncCallback);
        presenter.onChangeClicked();

        verify(service).setValue(anyString(), (UpdateVariableRequest)anyObject(), (AsyncRequestCallback<Void>)anyObject());
        verify(asyncCallback).onSuccess(eq(VAR_VALUE));
        verify(view).close();
    }

    @Test
    public void testChangeValueRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).setValue(anyString(), (UpdateVariableRequest)anyObject(), (AsyncRequestCallback<Void>)anyObject());
        when(view.getValue()).thenReturn(VAR_VALUE);

        presenter.showDialog(debuggerInfo, var, asyncCallback);
        presenter.onChangeClicked();

        verify(service).setValue(anyString(), (UpdateVariableRequest)anyObject(), (AsyncRequestCallback<Void>)anyObject());
        verify(asyncCallback).onFailure((Throwable)anyObject());
        verify(view).close();
    }

}
