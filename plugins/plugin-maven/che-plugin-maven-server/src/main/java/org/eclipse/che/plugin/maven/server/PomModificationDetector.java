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
package org.eclipse.che.plugin.maven.server;

import com.google.inject.Inject;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.shared.dto.event.PomModifiedEventDto;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static java.nio.file.Files.isDirectory;
import static org.eclipse.che.api.vfs.watcher.FileWatcherManager.EMPTY_CONSUMER;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

public class PomModificationDetector {
    private static final String POM_XML = "pom.xml";

    private final FileWatcherManager manager;
    private final EventService       eventService;

    private int id;

    @Inject
    public PomModificationDetector(EventService eventService, FileWatcherManager manager) {
        this.eventService = eventService;
        this.manager = manager;
    }

    @PostConstruct
    public void startWatcher() {
        id = manager.registerByMatcher(it -> !isDirectory(it) && POM_XML.equals(it.getFileName().toString()),
                                       EMPTY_CONSUMER,
                                            it -> eventService.publish(newDto(PomModifiedEventDto.class).withPath(it)),
                                       EMPTY_CONSUMER);
    }

    @PreDestroy
    public void stopWatcher() {
        manager.unRegisterByMatcher(id);
    }

}
