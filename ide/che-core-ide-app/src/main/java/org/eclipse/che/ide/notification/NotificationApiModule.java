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
package org.eclipse.che.ide.notification;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.notification.NotificationManager;

/**
 * GIN module for configuring Notification API components.
 *
 * @author Artem Zatsarynnyi
 */
public class NotificationApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(NotificationManager.class).to(NotificationManagerImpl.class).in(Singleton.class);
        bind(NotificationManagerView.class).to(NotificationManagerViewImpl.class).in(Singleton.class);
    }
}
