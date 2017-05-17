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
package org.eclipse.che.api.vfs.impl.file.event.detectors;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;

import javax.inject.Inject;
import javax.inject.Singleton;

/** Receive a file tracking operation call from client and notify server side about it by {@link FileTrackingOperationEvent}. */
@Singleton
public class EditorFileOperationHandler {
    private static final String INCOMING_METHOD = "track:editor-file";

    private final EventService      eventService;

    @Inject
    public EditorFileOperationHandler(EventService eventService) {
        this.eventService = eventService;
    }

    @Inject
    public void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName(INCOMING_METHOD)
                    .paramsAsDto(FileTrackingOperationDto.class)
                    .resultAsEmpty()
                    .withFunction(this::handleFileTrackingOperation);
    }

    private Void handleFileTrackingOperation(String endpointId, FileTrackingOperationDto operation) {
        try {
            eventService.publish(new FileTrackingOperationEvent(endpointId, operation));
            return null;
        } catch (Exception e) {
            throw new JsonRpcException(500, e.getLocalizedMessage());
        }
    }
}
