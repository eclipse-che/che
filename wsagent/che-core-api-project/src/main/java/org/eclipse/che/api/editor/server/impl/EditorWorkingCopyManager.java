/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.editor.server.impl;

import static java.io.File.separator;
import static java.nio.charset.Charset.defaultCharset;
import static org.eclipse.che.api.project.shared.Constants.CHE_DIR;

import com.google.common.hash.Hashing;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.dto.EditorChangesDto;
import org.eclipse.che.api.project.shared.dto.ServerError;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.watcher.server.detectors.FileTrackingOperationEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class contains methods to simplify the work with editor working copies.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorWorkingCopyManager {

  private static final Logger LOG = LoggerFactory.getLogger(EditorWorkingCopyManager.class);
  private static final String WORKING_COPIES_DIR = "/" + CHE_DIR + "/workingCopies";
  private static final String WORKING_COPY_ERROR_METHOD = "track:editor-working-copy-error";

  private final FsManager fsManager;
  private final ProjectManager projectManager;
  private final Map<String, EditorWorkingCopy> workingCopiesStorage = new HashMap<>();

  private EventService eventService;
  private RequestTransmitter transmitter;
  private EventSubscriber<FileTrackingOperationEvent> fileOperationEventSubscriber;

  @Inject
  public EditorWorkingCopyManager(
      EventService eventService,
      RequestTransmitter transmitter,
      FsManager fsManager,
      ProjectManager projectManager) {
    this.eventService = eventService;
    this.transmitter = transmitter;
    this.fsManager = fsManager;
    this.projectManager = projectManager;

    fileOperationEventSubscriber =
        new EventSubscriber<FileTrackingOperationEvent>() {
          @Override
          public void onEvent(FileTrackingOperationEvent event) {
            onFileOperation(event.getEndpointId(), event.getFileTrackingOperation());
          }
        };
    eventService.subscribe(fileOperationEventSubscriber);
  }

  /**
   * Gets in-memory working copy by path to the original file. Note: returns {@code null} when
   * working copy is not found
   *
   * @param filePath path to the original file
   * @return in-memory working copy for the file which corresponds given {@code filePath} or {@code
   *     null} when working copy is not found
   */
  @Nullable
  public EditorWorkingCopy getWorkingCopy(String filePath) {
    return workingCopiesStorage.get(filePath);
  }

  void onEditorContentUpdated(String endpointId, EditorChangesDto changes) {
    String filePath = changes.getFileLocation();
    String projectPath = changes.getProjectPath();

    try {
      if (filePath.isEmpty() || projectPath.isEmpty()) {
        throw new NotFoundException("Paths for file and project should be defined");
      }

      EditorWorkingCopy workingCopy = workingCopiesStorage.get(filePath);
      if (workingCopy == null) {
        workingCopy = createWorkingCopy(filePath);
      }

      workingCopy.applyChanges(changes);
      eventService.publish(new EditorWorkingCopyUpdatedEvent(endpointId, changes));

    } catch (IOException | ForbiddenException | ConflictException | ServerException e) {
      String errorMessage = "Can not handle editor changes: " + e.getLocalizedMessage();

      LOG.error(errorMessage);

      transmitError(500, errorMessage, endpointId);
    } catch (NotFoundException e) {
      String errorMessage = "Can not handle editor changes: " + e.getLocalizedMessage();

      LOG.debug(errorMessage);
    }
  }

  private void onFileOperation(String endpointId, FileTrackingOperationDto operation) {
    try {
      FileTrackingOperationDto.Type type = operation.getType();
      switch (type) {
        case START:
          {
            String path = operation.getPath();
            EditorWorkingCopy workingCopy = workingCopiesStorage.get(path);
            if (workingCopy == null) {
              createWorkingCopy(path);
            }
            // TODO At opening file we can have persistent working copy when user has unsaved data
            // at this case we need provide ability to recover unsaved data
            break;
          }
        case STOP:
          {
            String path = operation.getPath();
            EditorWorkingCopy workingCopy = workingCopiesStorage.get(path);
            if (workingCopy == null) {
              return;
            }

            if (isWorkingCopyHasUnsavedData(path)) {
              createPersistentWorkingCopy(
                  path); // to have ability to recover unsaved data when the file will be open later
            } else {

              String projectPath = workingCopy.getProjectPath();
              String workingCopyPath = projectPath + separator + toWorkingCopyPath(path);
              if (fsManager.existsAsFile(workingCopyPath)) {
                fsManager.delete(workingCopyPath);
              }
            }
            workingCopiesStorage.remove(path);
            break;
          }

        case MOVE:
          {
            String oldPath = operation.getOldPath();
            String newPath = operation.getPath();

            EditorWorkingCopy workingCopy = workingCopiesStorage.remove(oldPath);
            if (workingCopy == null) {
              return;
            }

            String workingCopyNewPath = toWorkingCopyPath(newPath);
            workingCopy.setPath(workingCopyNewPath);
            workingCopiesStorage.put(newPath, workingCopy);

            String projectPath = workingCopy.getProjectPath();
            String workingCopyPath = projectPath + separator + toWorkingCopyPath(oldPath);
            if (fsManager.existsAsFile(workingCopyPath)) {
              fsManager.delete(workingCopyPath);
            }
            break;
          }

        default:
          {
            break;
          }
      }
    } catch (ServerException | IOException | ForbiddenException | ConflictException e) {
      String errorMessage = "Can not handle file operation: " + e.getMessage();

      LOG.error(errorMessage);

      transmitError(500, errorMessage, endpointId);
    } catch (NotFoundException e) {
      String errorMessage = "Can not handle file operation: " + e.getMessage();

      LOG.error(errorMessage);

      transmitError(400, errorMessage, endpointId);
    }
  }

  private void transmitError(int code, String errorMessage, String endpointId) {
    DtoFactory dtoFactory = DtoFactory.getInstance();
    ServerError error =
        dtoFactory.createDto(ServerError.class).withCode(code).withMessage(errorMessage);
    transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(WORKING_COPY_ERROR_METHOD)
        .paramsAsDto(error)
        .sendAndSkipResult();
  }

  private boolean isWorkingCopyHasUnsavedData(String originalFilePath) {
    try {
      EditorWorkingCopy workingCopy = workingCopiesStorage.get(originalFilePath);
      if (workingCopy == null) {
        return false;
      }
      String workingCopyContent = workingCopy.getContentAsString();

      String originalFileContent;
      if (fsManager.existsAsFile(originalFilePath)) {
        InputStream inputStream = fsManager.read(originalFilePath);
        originalFileContent = IOUtils.toString(inputStream);
      } else {
        return false;
      }

      if (workingCopyContent == null || originalFileContent == null) {
        return false;
      }

      String workingCopyHash =
          Hashing.md5().hashString(workingCopyContent, defaultCharset()).toString();
      String originalFileHash =
          Hashing.md5().hashString(originalFileContent, defaultCharset()).toString();

      return !Objects.equals(workingCopyHash, originalFileHash);
    } catch (NotFoundException | IOException | ServerException | ConflictException e) {
      LOG.error(e.getLocalizedMessage());
    }

    return false;
  }

  private EditorWorkingCopy createWorkingCopy(String filePath)
      throws NotFoundException, ServerException, ConflictException, ForbiddenException,
          IOException {

    InputStream fileContentAsStream = fsManager.read(filePath);
    byte[] fileContentAsBytes = IOUtils.toByteArray(fileContentAsStream);

    String projectPath =
        projectManager
            .getClosest(filePath)
            .orElseThrow(() -> new NotFoundException("Project is not found for file: " + filePath))
            .getPath();

    String workingCopyPath = toWorkingCopyPath(filePath);

    EditorWorkingCopy workingCopy =
        new EditorWorkingCopy(workingCopyPath, projectPath, fileContentAsBytes);
    workingCopiesStorage.put(filePath, workingCopy);

    return workingCopy;
  }

  private void createPersistentWorkingCopy(String originalFilePath)
      throws ServerException, ForbiddenException, ConflictException {
    try {
      EditorWorkingCopy workingCopy = workingCopiesStorage.get(originalFilePath);
      if (workingCopy == null) {
        throw new ServerException("Can not create recovery file for " + originalFilePath);
      }

      byte[] content = workingCopy.getContentAsBytes();
      String projectPath = workingCopy.getProjectPath();
      String workingCopyStoragePath = projectPath + WORKING_COPIES_DIR;

      if (fsManager.existsAsDir(projectPath)) {
        if (!fsManager.existsAsDir(workingCopyStoragePath)) {
          fsManager.createDir(workingCopyStoragePath);
        }
      } else {
        throw new ServerException("No project directory exists " + projectPath);
      }

      if (fsManager.existsAsFile(originalFilePath)) {
        String workingCopyFilePath =
            workingCopyStoragePath + separator + toWorkingCopyPath(originalFilePath);
        fsManager.update(workingCopyFilePath, new ByteArrayInputStream(content));
      } else {
        fsManager.createFile(workingCopy.getPath(), new ByteArrayInputStream(content));
      }

    } catch (ConflictException | NotFoundException e) {
      LOG.error(e.getLocalizedMessage());
      throw new ServerException("Can not create recovery file for " + originalFilePath);
    }
  }

  private String toWorkingCopyPath(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1, path.length());
    }
    return path.replace('/', '.');
  }

  @PreDestroy
  private void unsubscribe() {
    eventService.unsubscribe(fileOperationEventSubscriber);
  }
}
