/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.machine;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.event.MachineFailedEvent;

/** Notifies about failing of any machine. */
@Singleton
class MachineFailNotifier {

  @Inject
  MachineFailNotifier(
      EventBus eventBus, Provider<NotificationManager> notificationManagerProvider) {
    eventBus.addHandler(
        MachineFailedEvent.TYPE,
        event -> {
          if (!isNullOrEmpty(event.getError())) {
            notificationManagerProvider.get().notify(event.getError(), FAIL, EMERGE_MODE);
          }
        });
  }
}
