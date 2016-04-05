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

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ext.debugger.client.DebuggerResources;
import org.eclipse.che.ide.ext.debugger.client.debug.DebuggerPresenter;
import org.eclipse.che.ide.ext.debugger.client.debug.changevalue.ChangeValuePresenter;

/**
 * Action which allows change value of selected variable with debugger
 *
 * @author Mykola Morhun
 */
public class ChangeVariableValueAction extends Action {

    private final ChangeValuePresenter changeValuePresenter;
    private final DebuggerPresenter    debuggerPresenter;

    @Inject
    public ChangeVariableValueAction(DebuggerLocalizationConstant locale,
                                     DebuggerResources resources,
                                     ChangeValuePresenter changeValuePresenter,
                                     DebuggerPresenter debuggerPresenter) {
        super(locale.changeVariableValue(), locale.changeVariableValueDescription(), null, resources.changeVariableValue());

        this.changeValuePresenter = changeValuePresenter;
        this.debuggerPresenter = debuggerPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        changeValuePresenter.showDialog();
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabled(debuggerPresenter.getSelectedVariable() != null);
    }

}
