/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.debugger.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.expression.EvaluateExpressionPresenter;

import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action which allows evaluate expression with debugger
 *
 * @author Mykola Morhun
 */
@Singleton
public class EvaluateExpressionAction extends AbstractPerspectiveAction {

    private final DebuggerManager             debuggerManager;
    private final EvaluateExpressionPresenter evaluateExpressionPresenter;

    @Inject
    public EvaluateExpressionAction(DebuggerManager debuggerManager,
                                    EvaluateExpressionPresenter evaluateExpressionPresenter,
                                    DebuggerLocalizationConstant locale,
                                    DebuggerResources resources) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              locale.evaluateExpression(),
              locale.evaluateExpressionDescription(),
              null,
              resources.evaluateExpression());
        this.debuggerManager = debuggerManager;
        this.evaluateExpressionPresenter = evaluateExpressionPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        evaluateExpressionPresenter.showDialog();
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        Debugger debugger = debuggerManager.getActiveDebugger();
        event.getPresentation().setEnabled(debugger != null && debugger.isSuspended());
    }
}
