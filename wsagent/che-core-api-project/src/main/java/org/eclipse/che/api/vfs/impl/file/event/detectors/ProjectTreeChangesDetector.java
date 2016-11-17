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

import org.eclipse.che.api.core.jsonrpc.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeStatusUpdateDto;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.eclipse.che.commons.schedule.executor.ThreadPullLauncher;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 */
@Beta
@Singleton
public class ProjectTreeChangesDetector implements HiEventDetector<ProjectTreeChangesDetector> {
    private static final Logger LOG = getLogger(ProjectTreeChangesDetector.class);

    private final RequestTransmitter transmitter;
    private final ThreadPullLauncher launcher;

    private final Set<EventTreeNode> trees = new HashSet<>();

    private State state;

    @Inject
    public ProjectTreeChangesDetector(RequestTransmitter transmitter, ThreadPullLauncher launcher) {
        this.transmitter = transmitter;
        this.launcher = launcher;
    }

    public static String findLongestPrefix(String s1, String s2) {
        if (s1 == null) {
            return s2;
        }

        for (int i = min(s1.length(), s2.length()); ; i--) {
            if (s2.startsWith(s1.substring(0, i))) {
                return s1.substring(0, i);
            }
        }
    }

    @PostConstruct
    public void postConstruct() {
        this.state = State.RESUMED;
        launcher.scheduleWithFixedDelay(this::transmit, 20_000L, 1_500L, MILLISECONDS);
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
        if (eventTreeNode.isRoot() && !eventTreeNode.getChildren().isEmpty()) {
            trees.add(eventTreeNode);
        }

        return Optional.empty();
    }

    private void transmit() {
        if (state == State.SUSPENDED) {
            return;
        }

        final Optional<String> commonSubstring = trees.stream()
                                                      .flatMap(EventTreeNode::stream)
                                                      .filter(EventTreeNode::modificationOccurred)
                                                      .map(EventTreeNode::getPath)
                                                      .reduce(ProjectTreeChangesDetector::findLongestPrefix);
        if (commonSubstring.isPresent()) {
            final String s = commonSubstring.get();
            final String path = s.substring(0, s.lastIndexOf('/'));
            transmit(path, FileWatcherEventType.MODIFIED);
        }

        trees.clear();
    }

    private void transmit(String path, FileWatcherEventType type) {
        final ProjectTreeStatusUpdateDto params = getParams(path, type);
        transmitter.broadcast("event:project-tree-status-changed", params);
    }

    private ProjectTreeStatusUpdateDto getParams(String path, FileWatcherEventType type) {
        return newDto(ProjectTreeStatusUpdateDto.class).withPath(path).withType(type);
    }

    private enum State {
        SUSPENDED,
        RESUMED
    }
}
