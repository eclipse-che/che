/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.watcher.server.detectors;

import static com.google.common.io.Files.hash;
import static java.nio.charset.Charset.defaultCharset;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.hash.Hashing;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.api.project.shared.dto.event.FileStateUpdateDto;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.eclipse.che.api.watcher.server.impl.FileWatcherUtils;
import org.slf4j.Logger;

/**
 * Receive a file tracking operation call from client. There are several type of such calls:
 *
 * <ul>
 *   <li>START/STOP - tells to start/stop tracking specific file
 *   <li>SUSPEND/RESUME - tells to start/stop tracking all files registered for specific endpoint
 *   <li>MOVE - tells that file that is being tracked should be moved (renamed)
 * </ul>
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class EditorFileTracker {

  private static final Logger LOG = getLogger(EditorFileTracker.class);

  private static final String OUTGOING_METHOD = "event:file-state-changed";

  private final Map<String, String> hashRegistry = new HashMap<>();
  private final Map<String, Integer> watchIdRegistry = new HashMap<>();

  private final RequestTransmitter transmitter;
  private final FileWatcherManager fileWatcherManager;
  private final FsManager fsManager;
  private final EventService eventService;
  private final EventSubscriber<FileTrackingOperationEvent> fileOperationEventSubscriber;
  private Path root;

  @Inject
  public EditorFileTracker(
      RootDirPathProvider pathProvider,
      FileWatcherManager fileWatcherManager,
      RequestTransmitter transmitter,
      FsManager fsManager,
      EventService eventService) {
    this.root = Paths.get(pathProvider.get());
    this.fileWatcherManager = fileWatcherManager;
    this.transmitter = transmitter;
    this.fsManager = fsManager;
    this.eventService = eventService;

    fileOperationEventSubscriber =
        new EventSubscriber<FileTrackingOperationEvent>() {
          @Override
          public void onEvent(FileTrackingOperationEvent event) {
            onFileTrackingOperationReceived(
                event.getEndpointId(), event.getFileTrackingOperation());
          }
        };
    eventService.subscribe(fileOperationEventSubscriber);
  }

  private void onFileTrackingOperationReceived(
      String endpointId, FileTrackingOperationDto operation) {
    Type type = operation.getType();
    String path = operation.getPath();
    String oldPath = operation.getOldPath();

    switch (type) {
      case START:
        {
          String key = path + endpointId;
          LOG.debug("Received file tracking operation START trigger key : {}", key);
          if (watchIdRegistry.containsKey(key)) {
            LOG.debug("Already registered {}", key);
            return;
          }
          int id =
              fileWatcherManager.registerByPath(
                  path,
                  getCreateConsumer(endpointId, path),
                  getModifyConsumer(endpointId, path),
                  getDeleteConsumer(endpointId, path));
          watchIdRegistry.put(key, id);

          break;
        }
      case STOP:
        {
          LOG.debug("Received file tracking operation STOP trigger.");

          Integer id = watchIdRegistry.remove(path + endpointId);
          if (id != null) {
            fileWatcherManager.unRegisterByPath(id);
          }

          break;
        }
      case SUSPEND:
        {
          LOG.debug("Received file tracking operation SUSPEND trigger.");

          break;
        }
      case RESUME:
        {
          LOG.debug("Received file tracking operation RESUME trigger.");

          break;
        }
      case MOVE:
        {
          LOG.debug("Received file tracking operation MOVE trigger.");

          Integer oldId = watchIdRegistry.remove(oldPath + endpointId);
          if (oldId != null) {
            fileWatcherManager.unRegisterByPath(oldId);
          }

          int newId =
              fileWatcherManager.registerByPath(
                  path,
                  getCreateConsumer(endpointId, path),
                  getModifyConsumer(endpointId, path),
                  getDeleteConsumer(endpointId, path));
          watchIdRegistry.put(path + endpointId, newId);

          break;
        }
      default:
        {
          LOG.error("Received file tracking operation UNKNOWN trigger.");

          break;
        }
    }
  }

  private Consumer<String> getCreateConsumer(String endpointId, String path) {
    // for case when file is updated through recreation
    return getModifyConsumer(endpointId, path);
  }

  private Consumer<String> getModifyConsumer(String endpointId, String path) {
    return it -> {
      String newHash = hashFile(path);
      String oldHash = hashRegistry.getOrDefault(path + endpointId, null);

      if (Objects.equals(newHash, oldHash)) {
        return;
      }

      hashRegistry.put(path + endpointId, newHash);

      FileStateUpdateDto params =
          newDto(FileStateUpdateDto.class).withPath(path).withType(MODIFIED).withHashCode(newHash);
      transmitter
          .newRequest()
          .endpointId(endpointId)
          .methodName(OUTGOING_METHOD)
          .paramsAsDto(params)
          .sendAndSkipResult();
    };
  }

  private Consumer<String> getDeleteConsumer(String endpointId, String path) {
    return it ->
        new Timer()
            .schedule(
                new TimerTask() {
                  @Override
                  public void run() {
                    if (!Files.exists(FileWatcherUtils.toNormalPath(root, it))) {
                      hashRegistry.remove(path + endpointId);
                      FileStateUpdateDto params =
                          newDto(FileStateUpdateDto.class).withPath(path).withType(DELETED);
                      transmitter
                          .newRequest()
                          .endpointId(endpointId)
                          .methodName(OUTGOING_METHOD)
                          .paramsAsDto(params)
                          .sendAndSkipResult();
                    }
                  }
                },
                1_000L);
  }

  private String hashFile(String wsPath) {
    try {
      File file = fsManager.toIoFile(wsPath);
      return file == null
          ? Hashing.md5().hashString("", defaultCharset()).toString()
          : hash(file, Hashing.md5()).toString();
    } catch (IOException e) {
      LOG.error("Error trying to read {} file and broadcast it", wsPath, e);
    }
    return null;
  }

  @PreDestroy
  private void unsubscribe() {
    eventService.unsubscribe(fileOperationEventSubscriber);
  }
}
