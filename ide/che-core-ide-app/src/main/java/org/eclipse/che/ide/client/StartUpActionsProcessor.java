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
package org.eclipse.che.ide.client;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.StartUpAction;
import org.eclipse.che.ide.api.component.WsAgentComponent;

import java.util.List;

/**
 * Will process all start-up actions that comes from
 * {@link AppContext#getStartAppActions()} after starting ws-agent.
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class StartUpActionsProcessor implements WsAgentComponent {

    private final AppContext    appContext;
    private final ActionManager actionManager;

    @Inject
    public StartUpActionsProcessor(AppContext appContext, ActionManager actionManager) {
        this.appContext = appContext;
        this.actionManager = actionManager;
    }

    @Override
    public void start(Callback<WsAgentComponent, Exception> callback) {
        final List<StartUpAction> startAppActions = appContext.getStartAppActions();
        if (startAppActions != null && !startAppActions.isEmpty()) {
            for (StartUpAction action : startAppActions) {
                actionManager.performAction(action.getActionId(), action.getParameters());
            }
        }
        callback.onSuccess(this);
    }
}
