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

import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type;
import org.eclipse.che.api.vfs.impl.file.event.HiEventClientBroadcaster;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class FileTrackingOperationReceiver implements JsonRpcRequestReceiver {
    private static final Logger LOG = getLogger(FileTrackingOperationReceiver.class);

    private final FileTrackingRegistry registry;

    @Inject
    public FileTrackingOperationReceiver(FileTrackingRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void receive(JsonRpcRequest request, Integer endpoint) {
        final String params = request.getParams();
        final FileTrackingOperationDto operation = DtoFactory.getInstance().createDtoFromJson(params, FileTrackingOperationDto.class);
        final Type type = operation.getType();
        final String path = operation.getPath();
        final String oldPath = operation.getOldPath();

        switch (type) {
            case START: {
                LOG.debug("Received file tracking operation START trigger.");

                registry.add(path, endpoint);

                break;
            }
            case STOP: {
                LOG.debug("Received file tracking operation STOP trigger.");

                registry.remove(path, endpoint);

                break;
            }
            case SUSPEND: {
                LOG.debug("Received file tracking operation SUSPEND trigger.");

                registry.suspend(endpoint);

                break;
            }
            case RESUME: {
                LOG.debug("Received file tracking operation RESUME trigger.");

                registry.resume(endpoint);

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
