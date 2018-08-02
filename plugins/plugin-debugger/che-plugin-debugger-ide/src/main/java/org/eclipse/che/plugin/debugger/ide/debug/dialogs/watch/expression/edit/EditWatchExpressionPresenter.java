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
package org.eclipse.che.plugin.debugger.ide.debug.dialogs.watch.expression.edit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.DebuggerDialogFactory;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.common.TextAreaDialogView;

/**
 * Presenter to edit selected expression in the debugger watch list.
 *
 * @author Oleksandr Andriienko
 */
@Singleton
public class EditWatchExpressionPresenter implements TextAreaDialogView.ActionDelegate {

  private final TextAreaDialogView view;
  private final DebuggerPresenter debuggerPresenter;
  private final DebuggerLocalizationConstant constant;
  private WatchExpression selectedExpression;

  @Inject
  public EditWatchExpressionPresenter(
      DebuggerDialogFactory dialogFactory,
      DebuggerLocalizationConstant constant,
      DebuggerPresenter debuggerPresenter) {
    this.view =
        dialogFactory.createTextAreaDialogView(
            constant.editExpressionViewDialogTitle(),
            constant.editExpressionViewSaveButtonTitle(),
            constant.editExpressionViewCancelButtonTitle());
    this.view.setDelegate(this);
    this.debuggerPresenter = debuggerPresenter;
    this.constant = constant;
  }

  @Override
  public void showDialog() {
    selectedExpression = debuggerPresenter.getSelectedWatchExpression();
    view.setValueTitle(constant.editExpressionViewExpressionFieldTitle());
    view.setValue(selectedExpression.getExpression());
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
    if (selectedExpression != null) {
      selectedExpression.setExpression(view.getValue());
      debuggerPresenter.onEditExpressionBtnClicked(selectedExpression);
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
