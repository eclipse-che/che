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
package org.eclipse.che.api.vfs.impl.file.event.detectors;

import org.eclipse.che.api.core.jsonrpc.RequestHandler;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Receive a file tracking operation call from client. There are several type of such calls:
 * <ul>
 *     <li>
 *         START/STOP - tells to start/stop tracking specific file
 *     </li>
 *     <li>
 *         SUSPEND/RESUME - tells to start/stop tracking all files registered for specific endpoint
 *     </li>
 *     <li>
 *         MOVE - tells that file that is being tracked should be moved (renamed)
 *     </li>
 * </ul>
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class FileTrackingOperationReceiver extends RequestHandler<FileTrackingOperationDto, Void> {
    private static final Logger LOG = getLogger(FileTrackingOperationReceiver.class);

    private final FileTrackingRegistry registry;

    @Inject
    public FileTrackingOperationReceiver(FileTrackingRegistry registry) {
        super(FileTrackingOperationDto.class, Void.class);
        this.registry = registry;
    }

    @Override
    public void handleNotification(String endpointId, FileTrackingOperationDto operation) {
        final Type type = operation.getType();
        final String path = operation.getPath();
        final String oldPath = operation.getOldPath();

        switch (type) {
            case START: {
                LOG.debug("Received file tracking operation START trigger.");

                registry.add(path, endpointId);

                break;
            }
            case STOP: {
                LOG.debug("Received file tracking operation STOP trigger.");

                registry.remove(path, endpointId);

                break;
            }
            case SUSPEND: {
                LOG.debug("Received file tracking operation SUSPEND trigger.");

                registry.suspend(endpointId);

                break;
            }
            case RESUME: {
                LOG.debug("Received file tracking operation RESUME trigger.");

                registry.resume(endpointId);

                break;
            }
            case MOVE: {
                LOG.debug("Received file tracking operation MOVE trigger.");

                registry.copy(oldPath, path);
                // TODO temporary workaround to support multi-client refactoring
                // file remove notification
//                registry.move(oldPath, path);

                break;
            }
            default: {
                LOG.error("Received file tracking operation UNKNOWN trigger.");

                break;
            }
        }
    }
}
