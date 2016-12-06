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
package org.eclipse.che.ide.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;

import org.eclipse.che.ide.api.event.ng.ClientServerEventService;
import org.eclipse.che.ide.api.event.ng.EditorFileStatusNotificationHandler;
import org.eclipse.che.ide.api.event.ng.FileOpenCloseEventListener;
import org.eclipse.che.ide.api.event.ng.ProjectTreeStatusNotificationHandler;
import org.eclipse.che.ide.jsonrpc.RequestHandler;

/**
 * GIN module for configuring client server events.
 *
 * @author Artem Zatsarynnyi
 */
public class ClientServerEventModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(FileOpenCloseEventListener.class).asEagerSingleton();
        bind(ClientServerEventService.class).asEagerSingleton();

        GinMapBinder.newMapBinder(binder(), String.class, RequestHandler.class)
                    .addBinding("event:file-in-vfs-status-changed")
                    .to(EditorFileStatusNotificationHandler.class);

        GinMapBinder.newMapBinder(binder(), String.class, RequestHandler.class)
                    .addBinding("event:project-tree-status-changed")
                    .to(ProjectTreeStatusNotificationHandler.class);
    }
}
