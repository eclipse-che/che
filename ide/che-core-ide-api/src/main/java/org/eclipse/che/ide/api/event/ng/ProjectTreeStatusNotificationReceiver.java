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


import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeStatusUpdateDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.ExternalResourceDelta;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ProjectTreeStatusNotificationReceiver implements JsonRpcRequestReceiver {
    private final DtoFactory dtoFactory;
    private final AppContext appContext;

    @Inject
    public ProjectTreeStatusNotificationReceiver(DtoFactory dtoFactory, AppContext appContext) {
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
    }

    @Override
    public void receive(JsonRpcRequest request) {
        final String params = request.getParams();
        final ProjectTreeStatusUpdateDto vfsFileStatusUpdateDto = dtoFactory.createDtoFromJson(params, ProjectTreeStatusUpdateDto.class);

        final String path = vfsFileStatusUpdateDto.getPath();
        final FileWatcherEventType type = vfsFileStatusUpdateDto.getType();

        final int status;

        switch (type) {
            case CREATED: {
                status = ADDED;

                break;
            }
            case DELETED: {
                status = REMOVED;

                break;
            }
            case MODIFIED: {
                status = UPDATED;

                break;
            }
            default: {
                status = UPDATED;

                break;
            }
        }

        Log.debug(getClass(), "Received request\npath: " + path +"\ntype:"+ type +"\nstatus:" + status);

        appContext.getWorkspaceRoot().synchronize(new ExternalResourceDelta(Path.valueOf(path), Path.valueOf(path), status));
    }
}
