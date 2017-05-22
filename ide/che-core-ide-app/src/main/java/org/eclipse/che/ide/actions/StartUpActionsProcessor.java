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
package org.eclipse.che.ide.actions;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.StartUpAction;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;

import java.util.List;

/**
 * Will process all start-up actions that comes from
 * {@link AppContext#getStartAppActions()} after starting ws-agent.
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class StartUpActionsProcessor {

    private final AppContext    appContext;
    private final ActionManager actionManager;

    @Inject
    public StartUpActionsProcessor(AppContext appContext, ActionManager actionManager, EventBus eventBus) {
        this.appContext = appContext;
        this.actionManager = actionManager;

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                new Timer() {
                    @Override
                    public void run() {
                        processActions();
                    }
                }.schedule(1000);
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
            }
        });
    }

    private void processActions() {
        final List<StartUpAction> startAppActions = appContext.getStartAppActions();
        if (startAppActions != null && !startAppActions.isEmpty()) {
            for (StartUpAction action : startAppActions) {
                actionManager.performAction(action.getActionId(), action.getParameters());
            }
        }
    }
}
