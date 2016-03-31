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

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.debug.BreakpointManager;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeResources;

/**
 * Action which allows remove all breakpoints
 *
 * @author Mykola Morhun
 */
public class DeleteAllBreakpointsAction extends Action {

    private final BreakpointManager breakpointManager;

    @Inject
    public DeleteAllBreakpointsAction(BreakpointManager breakpointManager,
                                      JavaRuntimeLocalizationConstant locale,
                                      JavaRuntimeResources resources) {
        super(locale.deleteAllBreakpoints(), locale.deleteAllBreakpointsDescription() , null, resources.deleteAllBreakpoints());

        this.breakpointManager = breakpointManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        breakpointManager.deleteAllBreakpoints();
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabled(!breakpointManager.getBreakpointList().isEmpty());
    }

}
