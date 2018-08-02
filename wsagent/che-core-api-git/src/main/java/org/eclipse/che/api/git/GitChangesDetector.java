/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.git.shared.Constants.EVENT_GIT_FILE_CHANGED;
import static org.eclipse.che.api.git.shared.FileChangedEventDto.Status.ADDED;
import static org.eclipse.che.api.git.shared.FileChangedEventDto.Status.MODIFIED;
import static org.eclipse.che.api.git.shared.FileChangedEventDto.Status.NOT_MODIFIED;
import static org.eclipse.che.api.git.shared.FileChangedEventDto.Status.UNTRACKED;
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
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.git.exception.GitCommitInProgressException;
import org.eclipse.che.api.git.exception.GitInvalidRepositoryException;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.eclipse.che.api.watcher.server.detectors.FileTrackingOperationEvent;
import org.slf4j.Logger;

/**
 * Detects changes in files and sends message to client Git handler.
 *
 * @author Igor Vinokur
 */
@Singleton
public class GitChangesDetector {

  private static final Logger LOG = getLogger(GitChangesDetector.class);

  private final RequestTransmitter transmitter;
  private final FileWatcherManager manager;
  private final ProjectManager projectManager;
  private final PathTransformer pathTransformer;
  private final GitConnectionFactory gitConnectionFactory;
  private final EventService eventService;
  private final EventSubscriber<FileTrackingOperationEvent> eventSubscriber;

  private final Map<String, Integer> watchIdRegistry = new HashMap<>();

  @Inject
  public GitChangesDetector(
      RequestTransmitter transmitter,
      FileWatcherManager manager,
      ProjectManager projectManager,
      PathTransformer pathTransformer,
      GitConnectionFactory gitConnectionFactory,
      EventService eventService) {
    this.transmitter = transmitter;
    this.manager = manager;
    this.projectManager = projectManager;
    this.pathTransformer = pathTransformer;
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
    return it -> {};
  }

  private Consumer<String> fsEventConsumer(String endpointId, String wsPath) {
    return it -> {
      try {
        String normalizedPath = wsPath.startsWith("/") ? wsPath.substring(1) : wsPath;
        String itemPath = normalizedPath.substring(normalizedPath.indexOf("/") + 1);
        String projectName = normalizedPath.split("/")[0];
        if (!projectManager.isRegistered(absolutize(projectName))) {
          throw new NotFoundException("Project '" + projectName + "' is not found");
        }
        String projectFsPath = pathTransformer.transform(projectName).toString();
        GitConnection gitConnection = gitConnectionFactory.getConnection(projectFsPath);
        Status status = gitConnection.status(singletonList(itemPath));
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
            .methodName(EVENT_GIT_FILE_CHANGED)
            .paramsAsDto(
                newDto(FileChangedEventDto.class)
                    .withPath(wsPath)
                    .withStatus(fileStatus)
                    .withEditedRegions(
                        fileStatus == MODIFIED ? gitConnection.getEditedRegions(itemPath) : null))
            .sendAndSkipResult();
      } catch (GitCommitInProgressException | GitInvalidRepositoryException e) {
        // Silent ignore
      } catch (ServerException | NotFoundException e) {
        LOG.error(e.getMessage());
      }
    };
  }
}
