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
package org.eclipse.che.plugin.debugger.ide.debug.dialogs.changevalue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.DebuggerDialogFactory;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.common.TextAreaDialogView;

/**
 * Presenter for changing variables value.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ChangeValuePresenter implements TextAreaDialogView.ActionDelegate {
  private final DebuggerManager debuggerManager;
  private final TextAreaDialogView view;
  private final DebuggerPresenter debuggerPresenter;
  private final DebuggerLocalizationConstant constant;
  private Variable selectedVariable;

  @Inject
  public ChangeValuePresenter(
      DebuggerDialogFactory dialogFactory,
      DebuggerLocalizationConstant constant,
      DebuggerManager debuggerManager,
      DebuggerPresenter debuggerPresenter) {
    this.view =
        dialogFactory.createTextAreaDialogView(
            constant.changeValueViewTitle(),
            constant.changeValueViewChangeButtonTitle(),
            constant.changeValueViewCancelButtonTitle());
    this.debuggerManager = debuggerManager;
    this.debuggerPresenter = debuggerPresenter;
    this.view.setDelegate(this);
    this.constant = constant;
  }

  public void showDialog() {
    this.selectedVariable = debuggerPresenter.getSelectedVariable();
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
  public void onAgreeClicked() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (selectedVariable != null && debugger != null && debugger.isSuspended()) {
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

    view.close();
  }

  @Override
  public void onValueChanged() {
    final String value = view.getValue();
    boolean isExpressionFieldNotEmpty = !value.trim().isEmpty();
    view.setEnableChangeButton(isExpressionFieldNotEmpty);
  }
}
