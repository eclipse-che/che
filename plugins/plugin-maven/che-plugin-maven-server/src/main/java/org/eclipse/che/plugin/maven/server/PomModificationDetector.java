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
package org.eclipse.che.plugin.maven.server;

import static java.nio.file.Files.isDirectory;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.Inject;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.shared.dto.event.PomModifiedEventDto;
import org.eclipse.che.api.watcher.server.FileWatcherManager;

public class PomModificationDetector {

  private static final String POM_XML = "pom.xml";

  private final FileWatcherManager manager;
  private final EventService eventService;

  private int id;

  @Inject
  public PomModificationDetector(EventService eventService, FileWatcherManager manager) {
    this.eventService = eventService;
    this.manager = manager;
  }

  @PostConstruct
  public void startWatcher() {
    id =
        manager.registerByMatcher(
            it -> !isDirectory(it) && POM_XML.equals(it.getFileName().toString()),
            it -> {},
            it -> eventService.publish(newDto(PomModifiedEventDto.class).withPath(it)),
            it -> {});
  }

  @PreDestroy
  public void stopWatcher() {
    manager.unRegisterByMatcher(id);
  }
}
