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

import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeStatusUpdateDto;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 */
@Beta
@Singleton
public class ProjectTreeChangesDetector implements HiEventDetector<ProjectTreeChangesDetector> {
    private static final Logger LOG = getLogger(ProjectTreeChangesDetector.class);

    private final JsonRpcRequestTransmitter transmitter;

    private State state;

    @Inject
    public ProjectTreeChangesDetector(JsonRpcRequestTransmitter transmitter) {
        this.transmitter = transmitter;
    }

    @PostConstruct
    public void postConstruct() {
        this.state = State.RESUMED;
    }

    public void suspend() {
        this.state = State.SUSPENDED;
    }

    public void resume() {
        // TODO this workaround is needed to compensate delays
        // in file watcher system, should be removed
        // after VFS is fixed properly
        try {
            Thread.sleep(5_000L);
        } catch (InterruptedException e) {
            LOG.error("Thread unexpectedly interrupted", e);
        }

        this.state = State.RESUMED;
    }

    @Override
    public Optional<HiEvent<ProjectTreeChangesDetector>> detect(EventTreeNode eventTreeNode) {
        if (!eventTreeNode.isRoot() || eventTreeNode.getChildren().isEmpty()) {
            return Optional.empty();
        }

        if (state == State.RESUMED) {
            final Set<EventTreeNode> directories = new HashSet<>();

            for (EventTreeNode candidateDir : eventTreeNode.stream()
                                                           .filter(EventTreeNode::modificationOccurred)
                                                           .filter(EventTreeNode::isDir)
                                                           .collect(toSet())) {
                directories.removeIf(dir -> dir.getPath().contains(candidateDir.getPath()));

                if (directories.stream().noneMatch(dir -> candidateDir.getPath().contains(dir.getPath()))) {
                    directories.add(candidateDir);
                }
            }

            for (EventTreeNode node : directories) {
                final String path = node.getPath();
                final FileWatcherEventType lastEventType = node.getLastEventType();

                transmit(path, lastEventType);
            }
        }

        return Optional.empty();
    }

    private void transmit(String path, FileWatcherEventType type) {
        final String params = getParams(path, type);
        final JsonRpcRequest request = getJsonRpcRequest(params);

        transmitter.transmit(request);
    }

    private String getParams(String path, FileWatcherEventType type) {
        return newDto(ProjectTreeStatusUpdateDto.class).withPath(path).withType(type).toString();
    }

    private JsonRpcRequest getJsonRpcRequest(String params) {
        return newDto(JsonRpcRequest.class)
                .withMethod("event:project-tree-status-changed")
                .withJsonrpc("2.0")
                .withParams(params);
    }

    private enum State {
        SUSPENDED,
        RESUMED
    }
}
