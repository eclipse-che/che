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

import org.eclipse.che.ide.api.event.ng.ClientServerEventService;
import org.eclipse.che.ide.api.event.ng.EditorFileStatusNotificationOperation;
import org.eclipse.che.ide.api.event.ng.FileOpenCloseEventListener;
import org.eclipse.che.ide.api.event.ng.ProjectTreeStateNotificationOperation;

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

        notificationOperations();
        requestFunctions();
    }

    private void requestFunctions() {
    }

    private void notificationOperations() {
        bind(EditorFileStatusNotificationOperation.class).asEagerSingleton();
        bind(ProjectTreeStateNotificationOperation.class).asEagerSingleton();
    }
}
