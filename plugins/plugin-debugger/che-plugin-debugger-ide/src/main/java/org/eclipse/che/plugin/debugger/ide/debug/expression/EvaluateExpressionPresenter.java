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
package org.eclipse.che.plugin.debugger.ide.debug.expression;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;

/**
 * Presenter for evaluating an expression.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class EvaluateExpressionPresenter implements EvaluateExpressionView.ActionDelegate {
    private DebuggerManager              debuggerManager;
    private EvaluateExpressionView       view;
    private DebuggerLocalizationConstant constant;

    @Inject
    public EvaluateExpressionPresenter(EvaluateExpressionView view,
                                       DebuggerLocalizationConstant constant,
                                       DebuggerManager debuggerManager) {
        this.view = view;
        this.debuggerManager = debuggerManager;
        this.view.setDelegate(this);
        this.constant = constant;
    }

    public void showDialog() {
        view.setResult("");
        view.setEnableEvaluateButton(false);
        view.showDialog();
        view.focusInExpressionField();
    }

    /** Close dialog. */
    public void closeDialog() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onEvaluateClicked() {
        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null) {
            view.setEnableEvaluateButton(false);

            debugger.evaluate(view.getExpression()).then(new Operation<String>() {
                @Override
                public void apply(String result) throws OperationException {
                    view.setResult(result);
                    view.setEnableEvaluateButton(true);
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError error) throws OperationException {
                    view.setResult(constant.evaluateExpressionFailed(error.getMessage()));
                    view.setEnableEvaluateButton(true);
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onExpressionValueChanged() {
        final String expression = view.getExpression();
        boolean isExpressionFieldNotEmpty = !expression.trim().isEmpty();
        view.setEnableEvaluateButton(isExpressionFieldNotEmpty);
    }
}
