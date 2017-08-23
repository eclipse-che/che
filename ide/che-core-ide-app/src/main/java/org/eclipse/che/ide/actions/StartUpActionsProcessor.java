/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.StartUpAction;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;

/**
 * Will process all start-up actions which come from {@link AppContext#getStartAppActions()} after
 * starting ws-agent.
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class StartUpActionsProcessor {

  private final AppContext appContext;
  private final ActionManager actionManager;

  @Inject
  public StartUpActionsProcessor(
      AppContext appContext, ActionManager actionManager, EventBus eventBus) {
    this.appContext = appContext;
    this.actionManager = actionManager;

    // delay is required because we need to wait some time while different components initialized
    eventBus.addHandler(WsAgentServerRunningEvent.TYPE, e -> performActionsWithDelay());
    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> performActionsWithDelay());
  }

  private void performActionsWithDelay() {
    new Timer() {
      @Override
      public void run() {
        performActions();
      }
    }.schedule(1000);
  }

  private void performActions() {
    List<StartUpAction> startAppActions = appContext.getStartAppActions();

    for (StartUpAction action : startAppActions) {
      actionManager.performAction(action.getActionId(), action.getParameters());
    }
  }
}
