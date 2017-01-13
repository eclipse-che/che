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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Creates snapshot of the workspace using {@link WorkspaceServiceClient}.
 *
 * <p>This component is for managing notifications which are related to creating snapshot process.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceSnapshotCreator {

    private final WorkspaceServiceClient   workspaceService;
    private final NotificationManager      notificationManager;
    private final CoreLocalizationConstant locale;

    private StatusNotification notification;

    @Inject
    public WorkspaceSnapshotCreator(WorkspaceServiceClient workspaceService,
                                    NotificationManager notificationManager,
                                    CoreLocalizationConstant locale) {
        this.workspaceService = workspaceService;
        this.notificationManager = notificationManager;
        this.locale = locale;
    }

    /**
     * Changes notification state to finished with an error.
     */
    public void creationError(String message) {
        if (notification != null) {
            notification.setTitle(locale.createSnapshotFailed());
            notification.setContent(message);
            notification.setStatus(FAIL);
        }
    }

    /**
     * Changes notification state to successfully finished.
     */
    public void successfullyCreated() {
        if (notification != null) {
            notification.setStatus(SUCCESS);
            notification.setTitle(locale.createSnapshotSuccess());
        }
    }

    /**
     * Returns true if workspace creation process is not done, otherwise when it is done - returns false
     */
    public boolean isInProgress() {
        return notification != null && notification.getStatus() == PROGRESS;
    }

    /**
     * Creates snapshot from workspace with given id and shows appropriate notification.
     *
     * @param workspaceId
     *         id of the workspace to create snapshot from.
     */
    public void createSnapshot(String workspaceId) {
        notification = notificationManager.notify(locale.createSnapshotProgress(), PROGRESS, FLOAT_MODE);
        workspaceService.createSnapshot(workspaceId)
                        .catchError(new Operation<PromiseError>() {
                            @Override
                            public void apply(PromiseError error) throws OperationException {
                                creationError(error.getMessage());
                            }
                        });
    }
}
