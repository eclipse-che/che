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

import org.eclipse.che.api.core.jsonrpc.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto;
import org.eclipse.che.api.vfs.impl.file.event.detectors.FileStatusDetector.FileTrackingEvent;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Transmits file status changes for all registered files. Status changes happens if
 * tracked (registered) files are (re)moved or modified. Each file status notification
 * is transmitted to all related to current file endpoints (clients) except for the
 * endpoints that are in 'suspended' state.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class FileTrackingOperationTransmitter {
    private static final Logger LOG = getLogger(FileTrackingOperationTransmitter.class);

    private final RequestTransmitter   transmitter;
    private final FileTrackingRegistry registry;

    @Inject
    public FileTrackingOperationTransmitter(EventService eventService, RequestTransmitter transmitter, FileTrackingRegistry registry) {
        this.transmitter = transmitter;
        this.registry = registry;

        eventService.subscribe(new FileTrackingEventSubscriber());
    }


    private class FileTrackingEventSubscriber implements EventSubscriber<FileTrackingEvent> {

        @Override
        public void onEvent(FileTrackingEvent event) {
            final String path = event.getPath();
            final FileWatcherEventType type = event.getType();

            if (!registry.contains(path)) {
                return;
            }

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
            final VfsFileStatusUpdateDto params = getParams(path, null, DELETED);

            registry.getEndpoints(path).forEach(endpoint -> {
                final String method = "event:file-in-vfs-status-changed";
                transmitter.transmitNotification(endpoint, method, params);
            });
        }

        private void transmitModified(String path) {
            if (registry.updateHash(path)) {
                final String hashCode = registry.getHashCode(path);
                final VfsFileStatusUpdateDto params = getParams(path, hashCode, MODIFIED);

                registry.getEndpoints(path).forEach(endpoint -> {
                    final String method = "event:file-in-vfs-status-changed";
                    transmitter.transmitNotification(endpoint, method, params);
                });
            }
        }

        private VfsFileStatusUpdateDto getParams(String path, String hashCode, FileWatcherEventType type) {
            return newDto(VfsFileStatusUpdateDto.class).withPath(path).withType(type).withHashCode(hashCode);
        }
    }
}
