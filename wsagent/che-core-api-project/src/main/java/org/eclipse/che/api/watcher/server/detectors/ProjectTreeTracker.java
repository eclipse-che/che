/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.watcher.server.detectors;

import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.fs.server.WsPathUtils.isRoot;
import static org.eclipse.che.api.fs.server.WsPathUtils.parentOf;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.notification.ProjectDeletedEvent;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeStateUpdateDto;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto.Type;
import org.eclipse.che.api.search.server.excludes.HiddenItemPathMatcher;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.slf4j.Logger;

@Singleton
public class ProjectTreeTracker {

  private static final Logger LOG = getLogger(ProjectTreeTracker.class);

  private static final String OUTGOING_METHOD = "event/project-tree-state-changed";
  private static final String INCOMING_METHOD = "track/project-tree";

  private final Set<String> registeredProjectPaths;
  private final Map<String, Integer> watchIdRegistry = new HashMap<>();
  private final List<String> timers = new CopyOnWriteArrayList<>();

  private final RequestTransmitter transmitter;
  private final FileWatcherManager fileWatcherManager;
  private final ProjectManager projectManager;
  private final EventService eventService;
  private final HiddenItemPathMatcher hiddenItemPathMatcher;
  private final RootDirPathProvider rootDirPathProvider;

  @Inject
  public ProjectTreeTracker(
      RequestTransmitter transmitter,
      FileWatcherManager fileWatcherManager,
      HiddenItemPathMatcher hiddenItemPathMatcher,
      RootDirPathProvider rootDirPathProvider,
      ProjectManager projectManager,
      EventService eventService) {
    this.transmitter = transmitter;
    this.fileWatcherManager = fileWatcherManager;
    this.projectManager = projectManager;
    this.eventService = eventService;
    this.hiddenItemPathMatcher = hiddenItemPathMatcher;
    this.rootDirPathProvider = rootDirPathProvider;

    registeredProjectPaths =
        projectManager.getAll().stream().map(RegisteredProject::getBaseFolder).collect(toSet());
  }

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(INCOMING_METHOD)
        .paramsAsDto(ProjectTreeTrackingOperationDto.class)
        .noResult()
        .withBiConsumer(getProjectTreeTrackingOperationConsumer());
  }

  private BiConsumer<String, ProjectTreeTrackingOperationDto>
      getProjectTreeTrackingOperationConsumer() {
    return (String endpointId, ProjectTreeTrackingOperationDto operation) -> {
      final Type type = operation.getType();
      final String path = operation.getPath();

      switch (type) {
        case START:
          {
            LOG.debug("Received project tree tracking operation START trigger.");

            int pathRegistrationId =
                fileWatcherManager.registerByPath(
                    path,
                    getCreateOperation(endpointId),
                    getModifyConsumer(endpointId),
                    getDeleteOperation(endpointId));
            watchIdRegistry.put(path + endpointId, pathRegistrationId);
            break;
          }
        case STOP:
          {
            LOG.debug("Received project tree tracking operation STOP trigger.");

            Predicate<Entry<String, Integer>> isSubPath =
                it -> it.getKey().startsWith(path) && it.getKey().endsWith(endpointId);

            watchIdRegistry
                .entrySet()
                .stream()
                .filter(isSubPath)
                .map(Entry::getKey)
                .collect(toSet())
                .stream()
                .map(watchIdRegistry::remove)
                .forEach(fileWatcherManager::unRegisterByPath);

            break;
          }
        case SUSPEND:
          {
            LOG.debug("Received project tree tracking operation SUSPEND trigger.");

            break;
          }
        case RESUME:
          {
            LOG.debug("Received project tree tracking operation RESUME trigger.");

            break;
          }
        default:
          {
            LOG.error("Received file tracking operation UNKNOWN trigger.");

            break;
          }
      }
    };
  }

  private Consumer<String> getCreateOperation(String endpointId) {
    return it -> {
      if (isExcluded(it)) {
        return;
      }

      if (timers.contains(it)) {
        timers.remove(it);
      } else {
        ProjectTreeStateUpdateDto params =
            newDto(ProjectTreeStateUpdateDto.class)
                .withPath(it)
                .withFile(isFile(it))
                .withType(CREATED);
        transmitter
            .newRequest()
            .endpointId(endpointId)
            .methodName(OUTGOING_METHOD)
            .paramsAsDto(params)
            .sendAndSkipResult();
      }

      fireCreatedEventIfIsProject(it);
    };
  }

  private void fireCreatedEventIfIsProject(String path) {
    // Two or more clients can be subscribed to this handler, so need to skip firing the event
    // if it was already sent.
    if (registeredProjectPaths.contains(path)) {
      return;
    }

    // Need to distinguish project created event from empty folder created event by project check.
    // Timer is needed for wait while the project will be initialized.
    final Timer timer = new Timer();
    timer.schedule(
        new TimerTask() {

          int attempt = 1;

          @Override
          public void run() {
            if (projectManager.isRegistered(path)) {
              eventService.publish(new ProjectCreatedEvent(path));
              registeredProjectPaths.add(path);
              timer.cancel();
            } else if (attempt == 5) {
              timer.cancel();
            }
            attempt++;
          }
        },
        200,
        200);
  }

  private Consumer<String> getModifyConsumer(String endpointId) {
    return it -> {};
  }

  private Consumer<String> getDeleteOperation(String endpointId) {
    return it -> {
      if (isExcluded(it)) {
        return;
      }

      if (registeredProjectPaths.contains(it)) {
        eventService.publish(new ProjectDeletedEvent(it));
        registeredProjectPaths.remove(it);
      }

      timers.add(it);
      new Timer()
          .schedule(
              new TimerTask() {
                @Override
                public void run() {
                  if (timers.contains(it)) {
                    timers.remove(it);
                    ProjectTreeStateUpdateDto params =
                        newDto(ProjectTreeStateUpdateDto.class).withPath(it).withType(DELETED);
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
    };
  }

  private boolean isFile(String path) {
    return Paths.get(rootDirPathProvider.get(), path).toFile().isFile();
  }

  private boolean isExcluded(String path) {
    String parentPath = parentOf(path);
    return isRoot(parentPath) && hiddenItemPathMatcher.matches(Paths.get(path));
  }
}
