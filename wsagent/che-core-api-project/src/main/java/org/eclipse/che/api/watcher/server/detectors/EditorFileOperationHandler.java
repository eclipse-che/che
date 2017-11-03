/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.watcher.server.detectors;

import static java.lang.String.format;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.isAbsolute;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.MOVE;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.RESUME;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.SUSPEND;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type;

/**
 * Receive a file tracking operation call from client and notify server side about it by {@link
 * FileTrackingOperationEvent}.
 */
@Singleton
public class EditorFileOperationHandler {
  private static final String INCOMING_METHOD = "track:editor-file";

  private final EventService eventService;

  @Inject
  public EditorFileOperationHandler(EventService eventService) {
    this.eventService = eventService;
  }

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(INCOMING_METHOD)
        .paramsAsDto(FileTrackingOperationDto.class)
        .resultAsBoolean()
        .withBiFunction(
            (endpointId, operation) -> {
              handleFileTrackingOperation(endpointId, operation);
              return true;
            });
  }

  private void handleFileTrackingOperation(String endpointId, FileTrackingOperationDto operation) {
    String path = operation.getPath();
    if (path != null && !isAbsolute(path)) {
      operation.withPath(absolutize(path));
    }

    String oldPath = operation.getOldPath();
    if (oldPath != null && !isAbsolute(oldPath)) {
      operation.withOldPath(absolutize(oldPath));
    }

    try {
      Type operationType = operation.getType();
      if (operationType == SUSPEND || operationType == RESUME) {
        eventService.publish(new FileTrackingOperationEvent(endpointId, operation));
        return;
      }

      String filePath = operation.getPath();
      if (filePath.isEmpty()) {
        throw new NotFoundException(
            format("Path for the file should be defined for %s operation", operationType));
      }

      if (operationType == MOVE && operation.getOldPath().isEmpty()) {
        throw new NotFoundException("Old path should be defined for 'MOVE' operation");
      }

      eventService.publish(new FileTrackingOperationEvent(endpointId, operation));
    } catch (Exception e) {
      throw new JsonRpcException(500, "Can not handle file operation: " + e.getLocalizedMessage());
    }
  }
}
