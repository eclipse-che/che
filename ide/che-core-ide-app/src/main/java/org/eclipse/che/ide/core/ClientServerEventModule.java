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
package org.eclipse.che.ide.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.event.ng.ClientServerEventService;
import org.eclipse.che.ide.api.event.ng.ClientServerEventServiceImpl;
import org.eclipse.che.ide.api.event.ng.EditorFileStatusNotificationOperation;
import org.eclipse.che.ide.api.event.ng.FileOpenCloseEventListener;
import org.eclipse.che.ide.api.event.ng.FileWatcherExcludesOperation;

/** GIN module for configuring client server events. */
public class ClientServerEventModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(FileOpenCloseEventListener.class).asEagerSingleton();
        bind(ClientServerEventService.class).to(ClientServerEventServiceImpl.class).in(Singleton.class);

        bind(EditorFileStatusNotificationOperation.class).asEagerSingleton();
        bind(FileWatcherExcludesOperation.class).asEagerSingleton();
    }
}
