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
package org.eclipse.che.plugin.maven.server;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;

import org.eclipse.che.api.project.shared.dto.event.PomModifiedEventDto;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventBroadcaster;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.eclipse.che.api.vfs.impl.file.event.HiEventServerPublisher;

import java.util.Optional;

import static java.util.Optional.empty;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.api.vfs.impl.file.event.HiEvent.Category.PROJECT_INFRASTRUCTURE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Detects if any of maven project object model files is MODIFIED during previous
 * event segment. If there are more than one modified file, generate event for the
 * highest (in context of file system location hierarchy) spotted.
 *
 * <p>
 * Note: this implementation deals only with standard project object model file
 * names - {@code pom.xml}. So if it is necessary to support custom naming you
 * can extend this class.
 * </p>
 *
 * @author Dmitry Kuleshov
 * @since 4.5
 */
@Beta
public class PomModifiedHiEventDetector implements HiEventDetector<PomModifiedEventDto> {

    private static final String POM_XML = "pom.xml";

    private HiEventBroadcaster broadcaster;

    @Inject
    public PomModifiedHiEventDetector(HiEventServerPublisher broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public Optional<HiEvent<PomModifiedEventDto>> detect(EventTreeNode eventTreeNode) {
        if (!eventTreeNode.isRoot() || eventTreeNode.getChildren().isEmpty()) {
            return empty();
        }

        final Optional<EventTreeNode> highestPom = eventTreeNode.stream()
                                                                .filter(EventTreeNode::modificationOccurred)
                                                                .filter(EventTreeNode::isFile)
                                                                .filter(event -> POM_XML.equals(event.getName()))
                                                                .filter(event -> MODIFIED.equals(event.getLastEventType()))
                                                                // note the revers order of o1 and o2
                                                                .sorted((o1, o2) -> o2.getPath().compareTo(o1.getPath()))
                                                                .findFirst();

        if (!highestPom.isPresent()) {
            return empty();
        }

        PomModifiedEventDto dto = newDto(PomModifiedEventDto.class).withPath(highestPom.get().getPath());

        return Optional.of((HiEvent.newInstance(PomModifiedEventDto.class)
                                   .withCategory(PROJECT_INFRASTRUCTURE.withPriority(50))
                                   .withBroadcaster(broadcaster)
                                   .withDto(dto)));
    }
}
