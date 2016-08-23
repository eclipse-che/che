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

import com.google.common.hash.HashCode;

import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto;
import org.eclipse.che.api.vfs.impl.file.event.detectors.EditorFileStatusDetector.FileTrackingEvent;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class FileTrackingOperationTransmitter {
    private static final Logger LOG = getLogger(FileTrackingOperationTransmitter.class);

    private final JsonRpcRequestTransmitter transmitter;
    private final FileTrackingRegistry      registry;

    @Inject
    public FileTrackingOperationTransmitter(EventService eventService,
                                            JsonRpcRequestTransmitter transmitter,
                                            FileTrackingRegistry registry) {
        this.transmitter = transmitter;
        this.registry = registry;

        eventService.subscribe(new FileTrackingEventSubscriber());
    }


    private class FileTrackingEventSubscriber implements EventSubscriber<FileTrackingEvent> {

        @Override
        public void onEvent(FileTrackingEvent event) {
            final String path = event.getPath();
            final FileWatcherEventType type = event.getType();

            switch (type) {
                case MODIFIED: {
                    LOG.debug("Received file MODIFIED trigger");
                    transmitModified(path);

                    break;
                }
                case DELETED: {
                    LOG.debug("Received file DELETED trigger");

                    transmitDeleted(path);

                    break;
                }
                default: {
                    LOG.debug("Received UNKNOWN file operation trigger");

                    break;
                }
            }
        }

        private void transmitDeleted(String path) {
            final String params = getParams(path, null, DELETED);
            final JsonRpcRequest request = getJsonRpcRequest(params);

            registry.getEndpoints(path).forEach(endpoint -> transmitter.transmit(request, endpoint));
        }

        private void transmitModified(String path) {
            if (registry.updateHash(path)) {
                final HashCode hashCode = registry.getHashCode(path);
                final String params = getParams(path, hashCode, MODIFIED);
                final JsonRpcRequest request = getJsonRpcRequest(params);

                registry.getEndpoints(path).forEach(endpoint -> transmitter.transmit(request, endpoint));
            }
        }

        private String getParams(String path, HashCode hashCode, FileWatcherEventType type) {
            return newDto(VfsFileStatusUpdateDto.class).withPath(path).withType(type).withHashCode(String.valueOf(hashCode)).toString();
        }

        private JsonRpcRequest getJsonRpcRequest(String params) {
            return newDto(JsonRpcRequest.class)
                    .withMethod("event:file-in-vfs-status-changed")
                    .withJsonrpc("2.0")
                    .withParams(params);
        }
    }
}
