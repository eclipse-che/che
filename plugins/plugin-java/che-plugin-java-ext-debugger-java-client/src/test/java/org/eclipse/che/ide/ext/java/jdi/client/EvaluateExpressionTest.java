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

import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.ide.ext.java.jdi.client.debug.expression.EvaluateExpressionPresenter;
import org.eclipse.che.ide.ext.java.jdi.client.debug.expression.EvaluateExpressionView;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link org.eclipse.che.ide.ext.java.jdi.client.debug.expression.EvaluateExpressionPresenter} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class EvaluateExpressionTest extends BaseTest {
    private static final String EXPRESSION        = "expression";
    private static final String EMPTY_EXPRESSION  = "";
    private static final String EVALUATION_RESULT = "result";
    private static final String FAIL_REASON       = "reason";
    @Mock
    private EvaluateExpressionView      view;
    @InjectMocks
    private EvaluateExpressionPresenter presenter;

    @Test
    public void shouldShowDialog() throws Exception {
        presenter.showDialog(debuggerInfo);

        verify(view).setExpression(eq(EMPTY_EXPRESSION));
        verify(view).setResult(eq(EMPTY_EXPRESSION));
        verify(view).setEnableEvaluateButton(eq(DISABLE_BUTTON));
        verify(view).showDialog();
        verify(view).focusInExpressionField();
    }

    @Test
    public void shouldCloseDialog() throws Exception {
        presenter.closeDialog();

        verify(view).close();
    }

    @Test
    public void shouldCloseDialogOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void shouldDisableEvaluateButtonIfNoExpression() throws Exception {
        when(view.getExpression()).thenReturn(EMPTY_EXPRESSION);

        presenter.onExpressionValueChanged();

        verify(view).setEnableEvaluateButton(eq(DISABLE_BUTTON));
    }

    @Test
    public void shouldEnableEvaluateButtonIfExpressionNotEmpty() throws Exception {
        when(view.getExpression()).thenReturn(EXPRESSION);

        presenter.onExpressionValueChanged();

        verify(view).setEnableEvaluateButton(eq(!DISABLE_BUTTON));
    }

    @Test
    public void testEvaluateExpressionRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, EVALUATION_RESULT);
                return callback;
            }
        }).when(service).evaluateExpression(anyString(), anyString(), (AsyncRequestCallback<String>)anyObject());
        when(view.getExpression()).thenReturn(EXPRESSION);

        presenter.showDialog(debuggerInfo);
        presenter.onEvaluateClicked();

        verify(view, atLeastOnce()).setEnableEvaluateButton(eq(DISABLE_BUTTON));
        verify(service).evaluateExpression(eq(DEBUGGER_ID), eq(EXPRESSION), (AsyncRequestCallback<String>)anyObject());
        verify(view).setResult(eq(EVALUATION_RESULT));
        verify(view).setEnableEvaluateButton(eq(!DISABLE_BUTTON));
    }

    @Test
    public void testEvaluateExpressionRequestIsFailed() throws Exception {
        final Throwable throwable = mock(Throwable.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, throwable);
                return callback;
            }
        }).when(service).evaluateExpression(anyString(), anyString(), (AsyncRequestCallback<String>)anyObject());
        when(view.getExpression()).thenReturn(EXPRESSION);
        when(throwable.getMessage()).thenReturn(FAIL_REASON);

        presenter.showDialog(debuggerInfo);
        presenter.onEvaluateClicked();

        verify(view, atLeastOnce()).setEnableEvaluateButton(eq(DISABLE_BUTTON));
        verify(service).evaluateExpression(eq(DEBUGGER_ID), eq(EXPRESSION), (AsyncRequestCallback<String>)anyObject());
        verify(constants).evaluateExpressionFailed(FAIL_REASON);
        verify(view).setEnableEvaluateButton(eq(!DISABLE_BUTTON));
    }

}
