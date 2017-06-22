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
package org.eclipse.che.api.project.server;

import com.google.common.hash.Hashing;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.shared.dto.EditorChangesDto;
import org.eclipse.che.api.project.shared.dto.ServerError;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.vfs.impl.file.event.detectors.FileTrackingOperationEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.eclipse.che.api.project.shared.Constants.CHE_DIR;

/**
 * The class contains methods to simplify the work with editor working copies.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorWorkingCopyManager {
    private static final Logger LOG                       = LoggerFactory.getLogger(EditorWorkingCopyManager.class);
    private static final String WORKING_COPIES_DIR        = "/" + CHE_DIR + "/workingCopies";
    private static final String WORKING_COPY_ERROR_METHOD = "track:editor-working-copy-error";

    private Provider<ProjectManager>                    projectManagerProvider;
    private EventService                                eventService;
    private RequestTransmitter                          transmitter;
    private EventSubscriber<FileTrackingOperationEvent> fileOperationEventSubscriber;

    private final Map<String, EditorWorkingCopy> workingCopiesStorage = new HashMap<>();

    @Inject
    public EditorWorkingCopyManager(Provider<ProjectManager> projectManagerProvider,
                                    EventService eventService,
                                    RequestTransmitter transmitter) {
        this.projectManagerProvider = projectManagerProvider;
        this.eventService = eventService;
        this.transmitter = transmitter;

        fileOperationEventSubscriber = new EventSubscriber<FileTrackingOperationEvent>() {
            @Override
            public void onEvent(FileTrackingOperationEvent event) {
                onFileOperation(event.getEndpointId(), event.getFileTrackingOperation());
            }
        };
        eventService.subscribe(fileOperationEventSubscriber);
    }

    /**
     * Gets in-memory working copy by path to the original file.
     * Note: returns {@code null} when working copy is not found
     *
     * @param filePath
     *         path to the original file
     * @return in-memory working copy for the file which corresponds given {@code filePath} or {@code null} when working copy is not found
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

            LOG.error(errorMessage);

            transmitError(400, errorMessage, endpointId);
        }
    }

    private void onFileOperation(String endpointId, FileTrackingOperationDto operation) {
        try {
            FileTrackingOperationDto.Type type = operation.getType();
            switch (type) {
                case START: {
                    String path = operation.getPath();
                    EditorWorkingCopy workingCopy = workingCopiesStorage.get(path);
                    if (workingCopy == null) {
                        createWorkingCopy(path);
                    }
                    //TODO At opening file we can have persistent working copy when user has unsaved data
                    // at this case we need provide ability to recover unsaved data
                    break;
                }
                case STOP: {
                    String path = operation.getPath();
                    EditorWorkingCopy workingCopy = workingCopiesStorage.get(path);
                    if (workingCopy == null) {
                        return;
                    }

                    if (isWorkingCopyHasUnsavedData(path)) {
                        createPersistentWorkingCopy(path);//to have ability to recover unsaved data when the file will be open later
                    } else {
                        VirtualFileEntry persistentWorkingCopy = getPersistentWorkingCopy(path, workingCopy.getProjectPath());
                        if (persistentWorkingCopy != null) {
                            persistentWorkingCopy.remove();
                        }
                    }
                    workingCopiesStorage.remove(path);
                    break;
                }

                case MOVE: {
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
                    VirtualFileEntry persistentWorkingCopy = getPersistentWorkingCopy(oldPath, projectPath);
                    if (persistentWorkingCopy != null) {
                        persistentWorkingCopy.remove();
                    }
                    break;
                }

                default: {
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
        ServerError error = dtoFactory.createDto(ServerError.class)
                                      .withCode(code)
                                      .withMessage(errorMessage);
        transmitter.newRequest()
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

            FileEntry originalFile = projectManagerProvider.get().asFile(originalFilePath);
            if (originalFile == null) {
                return false;
            }

            String workingCopyContent = workingCopy.getContentAsString();
            String originalFileContent = originalFile.getVirtualFile().getContentAsString();
            if (workingCopyContent == null || originalFileContent == null) {
                return false;
            }

            String workingCopyHash = Hashing.md5().hashString(workingCopyContent, defaultCharset()).toString();
            String originalFileHash = Hashing.md5().hashString(originalFileContent, defaultCharset()).toString();

            return !Objects.equals(workingCopyHash, originalFileHash);
        } catch (NotFoundException | ServerException | ForbiddenException e) {
            LOG.error(e.getLocalizedMessage());
        }

        return false;
    }

    private EditorWorkingCopy createWorkingCopy(String filePath)
            throws NotFoundException, ServerException, ConflictException, ForbiddenException, IOException {

        FileEntry file = projectManagerProvider.get().asFile(filePath);
        if (file == null) {
            throw new NotFoundException(format("Item '%s' isn't found. ", filePath));
        }

        String projectPath = file.getProject();
        String workingCopyPath = toWorkingCopyPath(filePath);

        EditorWorkingCopy workingCopy = new EditorWorkingCopy(workingCopyPath, projectPath, file.contentAsBytes());
        workingCopiesStorage.put(filePath, workingCopy);

        return workingCopy;
    }

    private void createPersistentWorkingCopy(String originalFilePath) throws ServerException, ForbiddenException, ConflictException {
        try {
            EditorWorkingCopy workingCopy = workingCopiesStorage.get(originalFilePath);
            if (workingCopy == null) {
                throw new ServerException("Can not create recovery file for " + originalFilePath);
            }

            byte[] content = workingCopy.getContentAsBytes();
            String projectPath = workingCopy.getProjectPath();

            VirtualFileEntry persistentWorkingCopy = getPersistentWorkingCopy(originalFilePath, projectPath);
            if (persistentWorkingCopy != null) {
                persistentWorkingCopy.getVirtualFile().updateContent(content);
                return;
            }

            FolderEntry persistentWorkingCopiesStorage = getPersistentWorkingCopiesStorage(projectPath);
            if (persistentWorkingCopiesStorage == null) {
                persistentWorkingCopiesStorage = createPersistentWorkingCopiesStorage(projectPath);
            }

            persistentWorkingCopiesStorage.createFile(workingCopy.getPath(), content);
        } catch (ConflictException | ForbiddenException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException("Can not create recovery file for " + originalFilePath);
        }
    }

    private VirtualFileEntry getPersistentWorkingCopy(String originalFilePath, String projectPath) {
        try {
            FolderEntry persistentWorkingCopiesStorage = getPersistentWorkingCopiesStorage(projectPath);
            if (persistentWorkingCopiesStorage == null) {
                return null;
            }

            String workingCopyPath = toWorkingCopyPath(originalFilePath);
            return persistentWorkingCopiesStorage.getChild(workingCopyPath);
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage());
            return null;
        }
    }

    private FolderEntry getPersistentWorkingCopiesStorage(String projectPath) {
        try {
            RegisteredProject project = projectManagerProvider.get().getProject(projectPath);
            FolderEntry baseFolder = project.getBaseFolder();
            if (baseFolder == null) {
                return null;
            }

            String tempDirectoryPath = baseFolder.getPath().toString() + WORKING_COPIES_DIR;
            return projectManagerProvider.get().asFolder(tempDirectoryPath);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            return null;
        }
    }

    private FolderEntry createPersistentWorkingCopiesStorage(String projectPath) throws ServerException {
        try {
            RegisteredProject project = projectManagerProvider.get().getProject(projectPath);
            FolderEntry baseFolder = project.getBaseFolder();
            if (baseFolder == null) {
                throw new ServerException("Can not create storage for recovery data");
            }

            return baseFolder.createFolder(WORKING_COPIES_DIR);
        } catch (NotFoundException | ConflictException | ForbiddenException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException("Can not create storage for recovery data " + e.getMessage());
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
