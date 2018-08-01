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
package org.eclipse.che.plugin.debugger.ide.debug.dialogs.watch.expression.add;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.api.debug.shared.model.impl.WatchExpressionImpl;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.DebuggerDialogFactory;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.common.TextAreaDialogView;

/**
 * Presenter to apply expression in the debugger watch list.
 *
 * @author Oleksandr Andriienko
 */
@Singleton
public class AddWatchExpressionPresenter implements TextAreaDialogView.ActionDelegate {

  private final TextAreaDialogView view;
  private final DebuggerPresenter debuggerPresenter;

  @Inject
  public AddWatchExpressionPresenter(
      DebuggerDialogFactory dialogFactory,
      DebuggerLocalizationConstant constant,
      DebuggerPresenter debuggerPresenter) {
    this.view =
        dialogFactory.createTextAreaDialogView(
            constant.addExpressionViewDialogTitle(),
            constant.addExpressionViewSaveButtonTitle(),
            constant.addExpressionViewCancelButtonTitle());
    this.view.setDelegate(this);
    this.debuggerPresenter = debuggerPresenter;
  }

  @Override
  public void showDialog() {
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
    WatchExpression expression = new WatchExpressionImpl(view.getValue());
    debuggerPresenter.onAddExpressionBtnClicked(expression);

    view.close();
  }

  @Override
  public void onValueChanged() {
    final String value = view.getValue();
    boolean isExpressionFieldNotEmpty = !value.trim().isEmpty();
    view.setEnableChangeButton(isExpressionFieldNotEmpty);
  }
}
