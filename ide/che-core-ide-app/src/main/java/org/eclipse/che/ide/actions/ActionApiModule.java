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
package org.eclipse.che.ide.actions;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.actions.find.FindActionView;
import org.eclipse.che.ide.actions.find.FindActionViewImpl;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.client.StartUpActionsProcessor;

/**
 * GIN module for configuring Action API components.
 *
 * @author Artem Zatsarynnyi
 */
public class ActionApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(ActionManager.class).to(ActionManagerImpl.class).in(Singleton.class);

        bind(StartUpActionsProcessor.class).in(Singleton.class);
        GinMapBinder.newMapBinder(binder(), String.class, WsAgentComponent.class)
                    .addBinding("Start-up actions processor")
                    .to(StartUpActionsProcessor.class);

        bind(FindActionView.class).to(FindActionViewImpl.class).in(Singleton.class);
    }
}
