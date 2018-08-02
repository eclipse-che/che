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
package org.eclipse.che.plugin.debugger.ide.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;

/**
 * Action allows remove debugger watch expression from debugger tree.
 *
 * @author Oleksandr Andriienko
 */
public class RemoveWatchExpressionAction extends AbstractPerspectiveAction {

  private final DebuggerPresenter debuggerPresenter;

  @Inject
  public RemoveWatchExpressionAction(
      DebuggerLocalizationConstant locale,
      DebuggerResources resources,
      DebuggerPresenter debuggerPresenter) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.removeWatchExpression(),
        locale.removeWatchExpressionDescription(),
        resources.removeWatchExpressionBtn());
    this.debuggerPresenter = debuggerPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    WatchExpression expression = debuggerPresenter.getSelectedWatchExpression();
    if (expression != null) {
      debuggerPresenter.onRemoveExpressionBtnClicked(expression);
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    event.getPresentation().setEnabled(debuggerPresenter.getSelectedWatchExpression() != null);
  }
}
