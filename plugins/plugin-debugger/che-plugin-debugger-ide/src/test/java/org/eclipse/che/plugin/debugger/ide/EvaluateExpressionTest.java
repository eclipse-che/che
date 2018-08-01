/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.expression.EvaluateExpressionPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.expression.EvaluateExpressionView;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Testing {@link EvaluateExpressionPresenter} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class EvaluateExpressionTest extends BaseTest {
  private static final String EXPRESSION = "expression";
  private static final String EMPTY_EXPRESSION = "";
  private static final String FAIL_REASON = "reason";
  private static final long THREAD_ID = 1;
  private static final int FRAME_INDEX = 0;
  @Mock private EvaluateExpressionView view;
  @Mock private DebuggerManager debuggerManager;
  @Mock private Debugger debugger;
  @Mock private Promise<String> promise;
  @Mock private PromiseError promiseError;
  @Mock private DebuggerPresenter debuggerPresenter;

  @Captor private ArgumentCaptor<Operation<PromiseError>> errorCaptor;
  @InjectMocks private EvaluateExpressionPresenter presenter;

  @Override
  @Before
  public void setUp() {
    when(debuggerPresenter.getSelectedThreadId()).thenReturn(THREAD_ID);
    when(debuggerPresenter.getSelectedFrameIndex()).thenReturn(FRAME_INDEX);
  }

  @Test
  public void shouldShowDialog() throws Exception {
    presenter.showDialog();

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
  public void shouldCloseDialogOnCloseClicked() throws Exception {
    presenter.onCloseClicked();

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
    when(debugger.evaluate(anyString(), eq(THREAD_ID), eq(FRAME_INDEX))).thenReturn(promise);
    when(view.getExpression()).thenReturn(EXPRESSION);
    when(promise.then(any(Operation.class))).thenReturn(promise);

    when(debuggerManager.getActiveDebugger()).thenReturn(debugger);

    presenter.showDialog();
    presenter.onEvaluateClicked();

    verify(view, atLeastOnce()).setEnableEvaluateButton(eq(DISABLE_BUTTON));
    verify(debugger).evaluate(EXPRESSION, THREAD_ID, FRAME_INDEX);
  }

  @Test
  public void testEvaluateExpressionRequestIsFailed() throws Exception {
    when(view.getExpression()).thenReturn(EXPRESSION);
    when(debugger.evaluate(view.getExpression(), THREAD_ID, FRAME_INDEX)).thenReturn(promise);
    when(promise.then((Operation) anyObject())).thenReturn(promise);
    when(promise.catchError(org.mockito.ArgumentMatchers.<Operation<PromiseError>>anyObject()))
        .thenReturn(promise);
    when(debuggerManager.getActiveDebugger()).thenReturn(debugger);
    when(promiseError.getMessage()).thenReturn(FAIL_REASON);

    presenter.showDialog();
    presenter.onEvaluateClicked();

    verify(view, atLeastOnce()).setEnableEvaluateButton(eq(DISABLE_BUTTON));
    verify(debugger).evaluate(EXPRESSION, THREAD_ID, FRAME_INDEX);
    verify(promise).catchError(errorCaptor.capture());

    errorCaptor.getValue().apply(promiseError);

    verify(view).setEnableEvaluateButton(eq(!DISABLE_BUTTON));
    verify(constants).evaluateExpressionFailed(FAIL_REASON);
  }
}
