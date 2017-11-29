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
package org.eclipse.che.api.git;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.FileChangedEventDto.Status.ADDED;
import static org.eclipse.che.api.git.shared.FileChangedEventDto.Status.MODIFIED;
import static org.eclipse.che.api.git.shared.FileChangedEventDto.Status.NOT_MODIFIED;
import static org.eclipse.che.api.git.shared.FileChangedEventDto.Status.UNTRACKED;
import static org.eclipse.che.api.vfs.watcher.FileWatcherManager.EMPTY_CONSUMER;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.vfs.impl.file.event.detectors.FileTrackingOperationEvent;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;
import org.slf4j.Logger;

/**
 * Detects changes in files and sends message to client Git handler.
 *
 * @author Igor Vinokur
 */
@Singleton
public class GitChangesDetector {
  private static final Logger LOG = getLogger(GitChangesDetector.class);

  private static final String OUTGOING_METHOD = "event/git-change";

  private final RequestTransmitter transmitter;
  private final FileWatcherManager manager;
  private final ProjectManager projectManager;
  private final GitConnectionFactory gitConnectionFactory;
  private final EventService eventService;
  private final EventSubscriber<FileTrackingOperationEvent> eventSubscriber;

  private final Map<String, Integer> watchIdRegistry = new HashMap<>();

  @Inject
  public GitChangesDetector(
      RequestTransmitter transmitter,
      FileWatcherManager manager,
      ProjectManager projectManager,
      GitConnectionFactory gitConnectionFactory,
      EventService eventService) {
    this.transmitter = transmitter;
    this.manager = manager;
    this.projectManager = projectManager;
    this.gitConnectionFactory = gitConnectionFactory;
    this.eventService = eventService;

    eventSubscriber =
        new EventSubscriber<FileTrackingOperationEvent>() {
          @Override
          public void onEvent(FileTrackingOperationEvent event) {
            onFileTrackingOperationReceived(
                event.getEndpointId(), event.getFileTrackingOperation());
          }
        };
    eventService.subscribe(eventSubscriber);
  }

  private void onFileTrackingOperationReceived(
      String endpointId, FileTrackingOperationDto operation) {
    FileTrackingOperationDto.Type type = operation.getType();
    String path = operation.getPath();
    String oldPath = operation.getOldPath();

    switch (type) {
      case START:
        {
          String key = path + endpointId;
          if (watchIdRegistry.containsKey(key)) {
            return;
          }
          int id =
              manager.registerByPath(
                  path,
                  createConsumer(endpointId, path),
                  modifyConsumer(endpointId, path),
                  deleteConsumer(endpointId, path));
          watchIdRegistry.put(key, id);

          break;
        }
      case STOP:
        {
          Integer id = watchIdRegistry.remove(path + endpointId);
          if (id != null) {
            manager.unRegisterByPath(id);
          }

          break;
        }
      case MOVE:
        {
          Integer oldId = watchIdRegistry.remove(oldPath + endpointId);
          if (oldId != null) {
            manager.unRegisterByPath(oldId);
          }

          int newId =
              manager.registerByPath(
                  path,
                  createConsumer(endpointId, path),
                  modifyConsumer(endpointId, path),
                  deleteConsumer(endpointId, path));
          watchIdRegistry.put(path + endpointId, newId);

          break;
        }
      default:
        break;
    }
  }

  @PreDestroy
  public void stopWatcher() {
    eventService.unsubscribe(eventSubscriber);
  }

  private Consumer<String> createConsumer(String endpointId, String path) {
    return fsEventConsumer(endpointId, path);
  }

  private Consumer<String> modifyConsumer(String endpointId, String path) {
    return fsEventConsumer(endpointId, path);
  }

  private Consumer<String> deleteConsumer(String endpointId, String path) {
    return EMPTY_CONSUMER;
  }

  private Consumer<String> fsEventConsumer(String endpointId, String path) {
    return it -> {
      try {
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        String itemPath = normalizedPath.substring(normalizedPath.indexOf("/") + 1);
        String projectPath =
            projectManager
                .getProject(normalizedPath.split("/")[0])
                .getBaseFolder()
                .getVirtualFile()
                .toIoFile()
                .getAbsolutePath();
        GitConnection connection = gitConnectionFactory.getConnection(projectPath);
        Status status = connection.status(singletonList(itemPath));
        FileChangedEventDto.Status fileStatus;
        if (status.getAdded().contains(itemPath)) {
          fileStatus = ADDED;
        } else if (status.getUntracked().contains(itemPath)) {
          fileStatus = UNTRACKED;
        } else if (status.getModified().contains(itemPath)
            || status.getChanged().contains(itemPath)) {
          fileStatus = MODIFIED;
        } else {
          fileStatus = NOT_MODIFIED;
        }

        transmitter
            .newRequest()
            .endpointId(endpointId)
            .methodName(OUTGOING_METHOD)
            .paramsAsDto(
                newDto(FileChangedEventDto.class)
                    .withPath(path)
                    .withStatus(fileStatus)
                    .withEditedRegions(
                        gitConnectionFactory.getConnection(projectPath).getEditedRegions(itemPath)))
            .sendAndSkipResult();
      } catch (NotFoundException | ServerException e) {
        String errorMessage = e.getMessage();
        if (!("Not a git repository".equals(errorMessage))) {
          LOG.error(errorMessage);
        }
      }
    };
  }
}
