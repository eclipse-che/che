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

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.changevalue.ChangeValuePresenter;

import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action which allows change value of selected variable with debugger
 *
 * @author Mykola Morhun
 */
public class ChangeVariableValueAction extends AbstractPerspectiveAction {

    private final ChangeValuePresenter changeValuePresenter;
    private final DebuggerPresenter    debuggerPresenter;

    @Inject
    public ChangeVariableValueAction(DebuggerLocalizationConstant locale,
                                     DebuggerResources resources,
                                     ChangeValuePresenter changeValuePresenter,
                                     DebuggerPresenter debuggerPresenter) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              locale.changeVariableValue(),
              locale.changeVariableValueDescription(),
              null,
              resources.changeVariableValue());
        this.changeValuePresenter = changeValuePresenter;
        this.debuggerPresenter = debuggerPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        changeValuePresenter.showDialog();
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        event.getPresentation().setEnabled(debuggerPresenter.getSelectedVariable() != null);
    }
}
