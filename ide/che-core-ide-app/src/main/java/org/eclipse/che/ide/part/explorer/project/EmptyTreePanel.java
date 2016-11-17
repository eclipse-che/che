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
package org.eclipse.che.ide.part.explorer.project;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.part.editor.EmptyEditorsPanel;

import javax.inject.Inject;

/**
 * Represent empty state of project explorer
 */
public class EmptyTreePanel extends EmptyEditorsPanel {

    @Inject
    public EmptyTreePanel(ActionManager actionManager,
                          Provider<PerspectiveManager> perspectiveManagerProvider,
                          KeyBindingAgent keyBindingAgent,
                          AppContext appContext,
                          EventBus eventBus,
                          CoreLocalizationConstant localizationConstant) {
        super(actionManager, perspectiveManagerProvider, keyBindingAgent, appContext, localizationConstant);
        eventBus.addHandler(ResourceChangedEvent.getType(), this);
        root.getStyle().setTop(46, Style.Unit.PX);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                renderNoProjects();
            }
        });
    }

    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                if(appContext.getProjects().length!= 0) {
                    getElement().removeFromParent();
                }
            }
        });
    }
}
