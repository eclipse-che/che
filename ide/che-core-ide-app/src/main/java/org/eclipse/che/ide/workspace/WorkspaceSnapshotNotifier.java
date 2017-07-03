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
package org.eclipse.che.ide.workspace;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.workspace.events.SnapshotCreatedEvent;
import org.eclipse.che.ide.workspace.events.SnapshotCreatingEvent;
import org.eclipse.che.ide.workspace.events.SnapshotCreationErrorEvent;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Shows notifications about workspace snapshotting progress.
 * Each call to {@link #creationStarted()} must be eventually followed by
 * either call to {@link #creationError(String)} or {@link #successfullyCreated()}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
class WorkspaceSnapshotNotifier {

    private final Provider<NotificationManager> notificationManagerProvider;
    private final CoreLocalizationConstant      messages;

    private StatusNotification notification;

    @Inject
    WorkspaceSnapshotNotifier(Provider<NotificationManager> notificationManagerProvider, CoreLocalizationConstant messages) {
        this.notificationManagerProvider = notificationManagerProvider;
        this.messages = messages;
    }

    @Inject
    private void registerEventHandlers(EventBus eventBus) {
        eventBus.addHandler(SnapshotCreatingEvent.TYPE, e -> creationStarted());
        eventBus.addHandler(SnapshotCreatedEvent.TYPE, e -> successfullyCreated());
        eventBus.addHandler(SnapshotCreationErrorEvent.TYPE, e -> creationError("Snapshot creation error: " + e.getErrorMessage()));
    }

    /**
     * Starts showing snapshotting notification.
     * The notification is shown until either {@link #creationError(String)}
     * or {@link #successfullyCreated()} is called.
     */
    @VisibleForTesting
    void creationStarted() {
        notification = notificationManagerProvider.get().notify(messages.createSnapshotProgress(), PROGRESS, FLOAT_MODE);
    }

    /**
     * Changes notification state to finished with an error.
     */
    @VisibleForTesting
    void creationError(String message) {
        if (notification != null) {
            notification.setTitle(messages.createSnapshotFailed());
            notification.setContent(message);
            notification.setStatus(FAIL);
        }
    }

    /**
     * Changes notification state to successfully finished.
     */
    @VisibleForTesting
    void successfullyCreated() {
        if (notification != null) {
            notification.setStatus(SUCCESS);
            notification.setTitle(messages.createSnapshotSuccess());
        }
    }
}
