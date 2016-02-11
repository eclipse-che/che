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
package org.eclipse.che.ide.bootstrap;


import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.util.StartUpAction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Will process all start up actions that comes from {@link AppContext#getStartAppActions()}
 * after starting extension server and initialize Project API.
 * In this case we will be sure all needed component initialized.
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class StartUpActionsProcessor  {

    private final AppContext    appContext;
    private final ActionManager actionManager;


    @Inject
    public StartUpActionsProcessor(AppContext appContext,
                                   ActionManager actionManager) {

        this.appContext = appContext;
        this.actionManager = actionManager;
    }


    public void performStartUpActions() {
        final List<StartUpAction> startAppActions = appContext.getStartAppActions();
        if (startAppActions != null && !startAppActions.isEmpty()) {
            for (StartUpAction action : startAppActions) {
                actionManager.performAction(action.getActionId(), action.getParameters());
            }
        }

    }
}
