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
package org.eclipse.che.ide.ext.java.jdi.client.debug.changevalue;

import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerVariable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Presenter for changing variables value.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ChangeValuePresenter implements ChangeValueView.ActionDelegate {
    private final DebuggerManager                 debuggerManager;
    private final ChangeValueView                 view;
    private final DebuggerPresenter               debuggerPresenter;
    /** Connected debugger information. */
    private final JavaRuntimeLocalizationConstant constant;
    /** Variable to change its value. */
    private       DebuggerVariable                debuggerVariable;

    /** Create presenter. */
    @Inject
    public ChangeValuePresenter(ChangeValueView view,
                                JavaRuntimeLocalizationConstant constant,
                                DebuggerManager debuggerManager,
                                DebuggerPresenter debuggerPresenter) {
        this.view = view;
        this.debuggerManager = debuggerManager;
        this.debuggerPresenter = debuggerPresenter;
        this.view.setDelegate(this);
        this.constant = constant;
    }

    /** Show dialog. */
    public void showDialog() {
        this.debuggerVariable = debuggerPresenter.getSelectedVariable();

        view.setValueTitle(constant.changeValueViewExpressionFieldTitle(debuggerVariable.getName()));
        view.setValue(debuggerVariable.getValue());
        view.focusInValueField();
        view.selectAllText();
        view.setEnableChangeButton(false);
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onChangeClicked() {
        Debugger debugger = debuggerManager.getDebugger();
        if (debugger != null) {
            final String newValue = view.getValue();
            debugger.changeVariableValue(debuggerVariable.getVariablePath().getPath(), newValue);
        }

        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onVariableValueChanged() {
        final String value = view.getValue();
        boolean isExpressionFieldNotEmpty = !value.trim().isEmpty();
        view.setEnableChangeButton(isExpressionFieldNotEmpty);
    }
}