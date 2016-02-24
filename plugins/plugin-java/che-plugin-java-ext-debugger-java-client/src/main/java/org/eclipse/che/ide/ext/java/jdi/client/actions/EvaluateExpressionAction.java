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
package org.eclipse.che.ide.ext.java.jdi.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.debug.BreakpointManager;
import org.eclipse.che.ide.debug.DebuggerState;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeResources;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import org.eclipse.che.ide.ext.java.jdi.client.debug.expression.EvaluateExpressionPresenter;

/**
 * Action which allows evaluateExpression expression with debugger
 *
 * @author Mykola Morhun
 */
@Singleton
public class EvaluateExpressionAction extends Action {

    private final DebuggerPresenter           debuggerPresenter;
    private final EvaluateExpressionPresenter evaluateExpressionPresenter;
    private final BreakpointManager           breakpointManager;

    @Inject
    public EvaluateExpressionAction(DebuggerPresenter presenter,
                                    EvaluateExpressionPresenter evaluateExpressionPresenter,
                                    JavaRuntimeLocalizationConstant locale,
                                    JavaRuntimeResources resources,
                                    BreakpointManager breakpointManager) {
        super(locale.evaluateExpression(), locale.evaluateExpressionDescription(), null, resources.evaluateExpression());

        this.debuggerPresenter = presenter;
        this.evaluateExpressionPresenter = evaluateExpressionPresenter;
        this.breakpointManager = breakpointManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        evaluateExpressionPresenter.showDialog();
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabled(debuggerPresenter.getDebuggerState() == DebuggerState.CONNECTED &&
                                       breakpointManager.getCurrentBreakpoint() != null);
    }

}
