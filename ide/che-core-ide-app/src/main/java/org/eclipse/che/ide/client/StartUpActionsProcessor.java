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

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.StartUpAction;
import org.eclipse.che.ide.api.workspace.event.WsStatusChangedEvent;

import java.util.List;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

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

        eventBus.addHandler(WsStatusChangedEvent.TYPE, event -> {
            if (event.getStatus() == RUNNING) {
                new Timer() {
                    @Override
                    public void run() {
                        processActions();
                    }
                }.schedule(1000);
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
