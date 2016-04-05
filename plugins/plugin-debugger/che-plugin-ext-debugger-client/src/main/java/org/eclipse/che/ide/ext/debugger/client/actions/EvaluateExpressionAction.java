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
package org.eclipse.che.ide.ext.debugger.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ext.debugger.client.DebuggerResources;
import org.eclipse.che.ide.ext.debugger.client.debug.expression.EvaluateExpressionPresenter;

/**
 * Action which allows evaluateExpression expression with debugger
 *
 * @author Mykola Morhun
 */
@Singleton
public class EvaluateExpressionAction extends Action {

    private final DebuggerManager             debuggerManager;
    private final EvaluateExpressionPresenter evaluateExpressionPresenter;

    @Inject
    public EvaluateExpressionAction(DebuggerManager debuggerManager,
                                    EvaluateExpressionPresenter evaluateExpressionPresenter,
                                    DebuggerLocalizationConstant locale,
                                    DebuggerResources resources) {
        super(locale.evaluateExpression(), locale.evaluateExpressionDescription(), null, resources.evaluateExpression());

        this.debuggerManager = debuggerManager;
        this.evaluateExpressionPresenter = evaluateExpressionPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        evaluateExpressionPresenter.showDialog();
    }

    @Override
    public void update(ActionEvent e) {
        Debugger debugger = debuggerManager.getActiveDebugger();
        e.getPresentation().setEnabled(debugger != null && debugger.isSuspended());
    }

}
