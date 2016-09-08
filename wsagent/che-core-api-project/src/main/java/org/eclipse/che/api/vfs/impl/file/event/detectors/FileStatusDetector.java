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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Tracks and notifies about VFS operations for registered files. List of registered files is
 * contained within an instance of {@link FileTrackingRegistry}.
 *
 * @author Dmitry Kuleshov
 */
@Beta
@Singleton
public class FileStatusDetector implements HiEventDetector<FileStatusDetector> {
    private final EventService eventService;


    @Inject
    public FileStatusDetector(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public Optional<HiEvent<FileStatusDetector>> detect(EventTreeNode eventTreeNode) {
        if (!eventTreeNode.isRoot() || eventTreeNode.getChildren().isEmpty()) {
            return Optional.empty();
        }

        eventTreeNode.stream()
                     .filter(EventTreeNode::modificationOccurred)
                     .filter(EventTreeNode::isFile)
                     .forEach(node -> eventService.publish(new FileTrackingEvent(node.getLastEventType(), node.getPath())));

        return Optional.empty();
    }

    public static class FileTrackingEvent {
        private final FileWatcherEventType type;
        private final String               path;

        public FileTrackingEvent(FileWatcherEventType type, String path) {
            this.type = type;
            this.path = path;
        }

        public FileWatcherEventType getType() {
            return type;
        }

        public String getPath() {
            return path;
        }
    }
}
