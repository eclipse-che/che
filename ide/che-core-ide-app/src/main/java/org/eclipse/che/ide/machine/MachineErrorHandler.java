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
package org.eclipse.che.ide.machine;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.machine.events.MachineFailedEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/** Handles failing machines. */
@Singleton
class MachineErrorHandler {

    @Inject
    MachineErrorHandler(EventBus eventBus, Provider<NotificationManager> notificationManagerProvider) {
        eventBus.addHandler(MachineFailedEvent.TYPE, event -> {
            if (!isNullOrEmpty(event.getError())) {
                notificationManagerProvider.get().notify(event.getError(), FAIL, EMERGE_MODE);
            }
        });
    }
}
