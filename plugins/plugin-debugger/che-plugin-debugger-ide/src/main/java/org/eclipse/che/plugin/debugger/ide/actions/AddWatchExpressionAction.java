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
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.watch.expression.add.AddWatchExpressionPresenter;

/**
 * Action allows add new debugger watch expression to the debugger tree.
 *
 * @author Oleksandr Andriienko
 */
public class AddWatchExpressionAction extends AbstractPerspectiveAction {

  private final AddWatchExpressionPresenter addWatchExpressionPresenter;

  @Inject
  public AddWatchExpressionAction(
      DebuggerLocalizationConstant locale,
      DebuggerResources resources,
      AddWatchExpressionPresenter addWatchExpressionPresenter) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.addWatchExpression(),
        locale.addWatchExpressionDescription(),
        resources.addWatchExpressionBtn());
    this.addWatchExpressionPresenter = addWatchExpressionPresenter;
  }

  @Override
  public void updateInPerspective(ActionEvent event) {}

  @Override
  public void actionPerformed(ActionEvent e) {
    addWatchExpressionPresenter.showDialog();
  }
}
