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
package org.eclipse.che.ide.api.event.ng;


import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.ExternalResourceDelta;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class EditorFileStatusNotificationReceiver implements JsonRpcRequestReceiver {
    private final DtoFactory dtoFactory;
    private final EventBus   eventBus;
    private final DeletedFilesController deletedFilesController;
    private final AppContext appContext;

    private NotificationManager notificationManager;

    @Inject
    public EditorFileStatusNotificationReceiver(DtoFactory dtoFactory,
                                                EventBus eventBus,
                                                DeletedFilesController deletedFilesController,
                                                AppContext appContext) {
        this.dtoFactory = dtoFactory;
        this.eventBus = eventBus;
        this.deletedFilesController = deletedFilesController;
        this.appContext = appContext;

    }

    public void inject(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    @Override
    public void receive(JsonRpcRequest request) {
        final String params = request.getParams();
        final VfsFileStatusUpdateDto vfsFileStatusUpdateDto = dtoFactory.createDtoFromJson(params, VfsFileStatusUpdateDto.class);

        final FileWatcherEventType status = vfsFileStatusUpdateDto.getType();
        final String path = vfsFileStatusUpdateDto.getPath();
        final String name = path.substring(path.lastIndexOf("/") + 1);

        switch (status) {
            case MODIFIED: {
                Log.debug(getClass(), "Received updated file event status: " + path);

                eventBus.fireEvent(new FileContentUpdateEvent(path, vfsFileStatusUpdateDto.getHashCode()));

                break;
            }
            case DELETED: {

                Log.debug(getClass(), "Received removed file event status: " + path);

                appContext.getWorkspaceRoot().synchronize(new ExternalResourceDelta(Path.valueOf(path), Path.valueOf(path), REMOVED));
                if (notificationManager != null && !deletedFilesController.remove(path)) {
                    notificationManager.notify("External operation", "File '" + name + "' is removed", SUCCESS, EMERGE_MODE);
                }

                break;
            }
        }
    }
}
