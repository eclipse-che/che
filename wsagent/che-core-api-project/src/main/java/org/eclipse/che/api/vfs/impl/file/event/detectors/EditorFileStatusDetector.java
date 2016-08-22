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

import com.google.common.annotations.Beta;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto;
import org.eclipse.che.api.project.shared.dto.event.FileInVfsStatusDto;
import org.eclipse.che.api.project.shared.dto.event.FileInVfsStatusDto.Status;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto.Status.CLOSED;
import static org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto.Status.OPENED;
import static org.eclipse.che.api.project.shared.dto.event.FileInVfsStatusDto.Status.REMOVED;
import static org.eclipse.che.api.project.shared.dto.event.FileInVfsStatusDto.Status.UPDATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Dmitry Kuleshov
 */
@Beta
@Singleton
public class EditorFileStatusDetector implements HiEventDetector<FileInVfsStatusDto>, JsonRpcRequestReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(EditorFileStatusDetector.class);

    private final Receiver     receiver     = new Receiver();
    private final FileRegistry fileRegistry = new FileRegistry();

    private final VirtualFileSystemProvider vfsProvider;
    private final JsonRpcRequestTransmitter transmitter;

    @Inject
    public EditorFileStatusDetector(VirtualFileSystemProvider vfsProvider, JsonRpcRequestTransmitter transmitter) {
        this.vfsProvider = vfsProvider;
        this.transmitter = transmitter;
    }

    @Override
    public Optional<HiEvent<FileInVfsStatusDto>> detect(EventTreeNode eventTreeNode) {
        if (!eventTreeNode.isRoot() || eventTreeNode.getChildren().isEmpty()) {
            return empty();
        }
        final Set<EventTreeNode> files = eventTreeNode.stream()
                                                      .filter(EventTreeNode::modificationOccurred)
                                                      .filter(EventTreeNode::isFile)
                                                      .filter(event -> fileRegistry.isTracked(event.getPath()))
                                                      .collect(toSet());

        filterAndTransmit(files, UPDATED);
        filterAndTransmit(files, REMOVED);

        return Optional.empty();
    }

    private void filterAndTransmit(Set<EventTreeNode> files, Status status) {
        final FileWatcherEventType type = status == UPDATED ? MODIFIED : DELETED;

        files.stream()
             .filter(event -> type == event.getLastEventType())
             .forEach(file -> prepareAndTransmit(file, status));
    }

    private void prepareAndTransmit(EventTreeNode file, Status status) {
        final String path = file.getPath();

        final JsonRpcRequest request = newDto(JsonRpcRequest.class)
                .withMethod("event:file-in-vfs-status-changed")
                .withJsonrpc("2.0")
                .withParams(newDto(FileInVfsStatusDto.class).withPath(path).withStatus(status).toString());

        final boolean contentUpdated = fileRegistry.updateHash(path);

        if (contentUpdated) {
            fileRegistry.getEndpoints(path).forEach(endpoint -> transmitter.transmit(request, endpoint));
        }
    }

    @Override
    public void receive(JsonRpcRequest request, Integer endpoint) {
        receiver.receive(request, endpoint);
    }

    private class Receiver implements JsonRpcRequestReceiver {
        @Override
        public void receive(JsonRpcRequest request, Integer endpoint) {
            final FileInEditorStatusDto dto = DtoFactory.getInstance().createDtoFromJson(request.getParams(), FileInEditorStatusDto.class);
            final FileInEditorStatusDto.Status status = dto.getStatus();
            final String path = dto.getPath();

            if (status == OPENED) {
                fileRegistry.add(path, endpoint);
            } else if (status == CLOSED) {
                fileRegistry.remove(path, endpoint);
            }
        }
    }

    private class FileRegistry {
        private final Map<String, HashCode>      hashRegistry     = new HashMap<>();
        private final Map<String, List<Integer>> endpointRegistry = new HashMap<>();

        private void add(String path, int endpoint) {
            List<Integer> endpoints = endpointRegistry.get(path);

            if (endpoints == null) {
                endpoints = new LinkedList<>();
                endpointRegistry.put(path, endpoints);
                hashRegistry.put(path, getHash(path));
            }

            endpoints.add(endpoint);
        }

        private void remove(String path, int endpoint) {
            final List<Integer> endpoints = endpointRegistry.get(path);

            if (endpoints == null) {
                return;
            }

            endpoints.remove((Integer)endpoint);

            if (endpoints.isEmpty()) {
                hashRegistry.remove(path);
                endpointRegistry.remove(path);
            }
        }

        private boolean updateHash(String path) {
            final HashCode newHash = getHash(path);
            final HashCode oldHash = hashRegistry.put(path, newHash);

            return !Objects.equals(oldHash, newHash);
        }

        private boolean isTracked(String path) {
            return endpointRegistry.keySet().contains(path);
        }

        private Set<Integer> getEndpoints(String path) {
            return Collections.unmodifiableSet(new HashSet<>(endpointRegistry.get(path)));
        }

        private HashCode getHash(String path) {
            try {
                final VirtualFile file = vfsProvider.getVirtualFileSystem()
                                                    .getRoot()
                                                    .getChild(Path.of(path));
                String content;
                if (file == null) {
                    content = "";
                } else {
                    content = file.getContentAsString();
                }

                return Hashing.sha1().hashString(content, defaultCharset());
            } catch (ServerException | ForbiddenException e) {
                LOG.error("Error trying to read {} file and broadcast it", path, e);
            }
            return null;
        }
    }
}
