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
package org.eclipse.che.ide.client;

import com.google.web.bindery.event.shared.EventBus;

//import org.eclipse.che.api.factory.shared.dto.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.AppCloseActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.event.WindowActionHandler;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler that performs actions before closing of application
 *
 * @author Sergii Leschenko
 */
public class AppCloseHandler {
    private final ActionManager actionManager;
//    private List<Action> actions = new ArrayList<>();

    @Inject
    public AppCloseHandler(ActionManager actionManager, EventBus eventBus) {
        this.actionManager = actionManager;

        eventBus.addHandler(WindowActionEvent.TYPE, new WindowActionHandler() {
            @Override
            public void onWindowClosing(final WindowActionEvent event) {
                String message = performActions();
                if (message != null) {
                    event.setMessage(message);
                }
            }

            @Override
            public void onWindowClosed(WindowActionEvent event) {
            }
        });
    }

    /**
     * Register actions for perform before closing of application
     */
//    public void performBeforeClose(List<Action> actions) {
//        this.actions.addAll(actions);
//    }

    /**
     * Performs registered action
     *
     * @return null if all action is successfully performed
     * or string with message if some action sent cancel closing of application.
     */
    private String performActions() {
        String cancelMessage = null;
        /*for (Action action : actions) {
            org.eclipse.che.ide.api.action.Action ideAction = actionManager.getAction(action.getId());

            if (ideAction == null) {
                continue;
            }

            Presentation presentation = new PresentationFactory().getPresentation(ideAction);
            AppCloseActionEvent e = new AppCloseActionEvent("", presentation, actionManager, 0, action.getProperties());
            ideAction.update(e);

            if (!presentation.isEnabled() || !presentation.isVisible()) {
                continue;
            }

            ideAction.actionPerformed(e);

            if (e.getCancelMessage() != null) {
                cancelMessage = e.getCancelMessage();
            }
        }*/

        return cancelMessage;
    }
}
