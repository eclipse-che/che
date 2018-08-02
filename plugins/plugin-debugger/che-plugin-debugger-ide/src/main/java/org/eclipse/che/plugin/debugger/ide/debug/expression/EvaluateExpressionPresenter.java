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
package org.eclipse.che.plugin.debugger.ide.debug.expression;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;

/**
 * Presenter for evaluating an expression.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class EvaluateExpressionPresenter implements EvaluateExpressionView.ActionDelegate {
  private final DebuggerManager debuggerManager;
  private final EvaluateExpressionView view;
  private final DebuggerLocalizationConstant constant;
  private final DebuggerPresenter debuggerPresenter;

  @Inject
  public EvaluateExpressionPresenter(
      EvaluateExpressionView view,
      DebuggerLocalizationConstant constant,
      DebuggerManager debuggerManager,
      DebuggerPresenter debuggerPresenter) {
    this.view = view;
    this.debuggerManager = debuggerManager;
    this.debuggerPresenter = debuggerPresenter;
    this.view.setDelegate(this);
    this.constant = constant;
  }

  public void showDialog() {
    view.setResult("");
    view.setEnableEvaluateButton(false);
    view.showDialog();
    view.focusInExpressionField();
  }

  public void closeDialog() {
    view.close();
  }

  @Override
  public void onCloseClicked() {
    view.close();
  }

  @Override
  public void onEvaluateClicked() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      view.setEnableEvaluateButton(false);

      final long threadId = debuggerPresenter.getSelectedThreadId();
      final int frameIndex = debuggerPresenter.getSelectedFrameIndex();
      debugger
          .evaluate(view.getExpression(), threadId, frameIndex)
          .then(
              result -> {
                view.setResult(result);
                view.setEnableEvaluateButton(true);
              })
          .catchError(
              error -> {
                view.setResult(constant.evaluateExpressionFailed(error.getMessage()));
                view.setEnableEvaluateButton(true);
              });
    }
  }

  @Override
  public void onExpressionValueChanged() {
    final String expression = view.getExpression();
    boolean isExpressionFieldNotEmpty = !expression.trim().isEmpty();
    view.setEnableEvaluateButton(isExpressionFieldNotEmpty);
  }
}
