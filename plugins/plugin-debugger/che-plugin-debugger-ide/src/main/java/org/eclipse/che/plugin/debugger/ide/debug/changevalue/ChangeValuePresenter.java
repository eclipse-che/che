/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug.changevalue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;

/**
 * Presenter for changing variables value.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ChangeValuePresenter implements ChangeValueView.ActionDelegate {
  private final DebuggerManager debuggerManager;
  private final ChangeValueView view;
  private final DebuggerPresenter debuggerPresenter;
  private final DebuggerLocalizationConstant constant;

  @Inject
  public ChangeValuePresenter(
      ChangeValueView view,
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
    Variable selectedVariable = debuggerPresenter.getSelectedVariable();
    view.setValueTitle(constant.changeValueViewExpressionFieldTitle(selectedVariable.getName()));
    view.setValue(selectedVariable.getValue().getString());
    view.focusInValueField();
    view.selectAllText();
    view.setEnableChangeButton(false);
    view.showDialog();
  }

  @Override
  public void onCancelClicked() {
    view.close();
  }

  @Override
  public void onChangeClicked() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      Variable selectedVariable = debuggerPresenter.getSelectedVariable();

      if (selectedVariable != null) {
        Variable newVariable =
            new VariableImpl(
                selectedVariable.getType(),
                selectedVariable.getName(),
                new SimpleValueImpl(view.getValue()),
                selectedVariable.isPrimitive(),
                selectedVariable.getVariablePath());

        final long threadId = debuggerPresenter.getSelectedThreadId();
        final int frameIndex = debuggerPresenter.getSelectedFrameIndex();
        debugger.setValue(newVariable, threadId, frameIndex);
      }
    }

    view.close();
  }

  @Override
  public void onVariableValueChanged() {
    final String value = view.getValue();
    boolean isExpressionFieldNotEmpty = !value.trim().isEmpty();
    view.setEnableChangeButton(isExpressionFieldNotEmpty);
  }
}
