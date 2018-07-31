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
package org.eclipse.che.api.git;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Paths.get;
import static java.util.regex.Pattern.compile;
import static org.eclipse.che.api.fs.server.WsPathUtils.SEPARATOR;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type.BRANCH;
import static org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type.REVISION;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.PathMatcher;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type;
import org.eclipse.che.api.search.server.excludes.HiddenItemPathMatcher;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.slf4j.Logger;

public class GitCheckoutDetector {

  private static final Logger LOG = getLogger(GitCheckoutDetector.class);

  private static final String GIT_DIR = ".git";
  private static final String HEAD_FILE = "HEAD";
  private static final Pattern PATTERN = compile("ref: refs/heads/");
  private static final String INCOMING_METHOD = "track/git-checkout";
  private static final String OUTGOING_METHOD = "event/git-checkout";

  private final RequestTransmitter transmitter;
  private final FileWatcherManager manager;
  private final FsManager fsManager;
  private final ProjectManager projectManager;
  private final PathTransformer pathTransformer;
  private final HiddenItemPathMatcher hiddenItemPathMatcher;

  private final Set<String> endpointIds = newConcurrentHashSet();

  private int id;

  @Inject
  public GitCheckoutDetector(
      RequestTransmitter transmitter,
      FileWatcherManager manager,
      FsManager fsManager,
      ProjectManager projectManager,
      PathTransformer pathTransformer,
      HiddenItemPathMatcher hiddenItemPathMatcher) {
    this.transmitter = transmitter;
    this.manager = manager;
    this.fsManager = fsManager;
    this.projectManager = projectManager;
    this.pathTransformer = pathTransformer;
    this.hiddenItemPathMatcher = hiddenItemPathMatcher;
  }

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(INCOMING_METHOD)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);
  }

  @PostConstruct
  public void startWatcher() {
    id = manager.registerByMatcher(matcher(), createConsumer(), modifyConsumer(), deleteConsumer());
  }

  @PreDestroy
  public void stopWatcher() {
    manager.unRegisterByMatcher(id);
  }

  private PathMatcher matcher() {
    return path -> {
      if (isDirectory(path)
          || !HEAD_FILE.equals(path.getFileName().toString())
          || !GIT_DIR.equals(path.getParent().getFileName().toString())) {
        return false;
      }

      String wsPath = pathTransformer.transform(path);
      String projectPath = wsPath.split(SEPARATOR)[1];
      return !hiddenItemPathMatcher.matches(get(projectPath));
    };
  }

  private Consumer<String> createConsumer() {
    return fsEventConsumer();
  }

  private Consumer<String> modifyConsumer() {
    return fsEventConsumer();
  }

  private Consumer<String> deleteConsumer() {
    return it -> {};
  }

  private Consumer<String> fsEventConsumer() {
    return it -> {
      try {
        String content = fsManager.readAsString(it);
        Type type = content.contains("ref:") ? BRANCH : REVISION;
        String name = type == REVISION ? content : PATTERN.split(content)[1];
        String project = it.substring(1, it.indexOf('/', 1));

        // Update project attributes with new git values

        String wsPath = absolutize(it.split("/")[1]);
        projectManager.setType(wsPath, GitProjectType.TYPE_ID, true);

        endpointIds.forEach(transmitConsumer(type, name, project));

      } catch (ServerException | ForbiddenException e) {
        LOG.error("Error trying to read {} file and broadcast it", it, e);
      } catch (NotFoundException | ConflictException | BadRequestException e) {
        LOG.error("Error trying to update project attributes", it, e);
      }
    };
  }

  private Consumer<String> transmitConsumer(Type type, String name, String project) {
    return id ->
        transmitter
            .newRequest()
            .endpointId(id)
            .methodName(OUTGOING_METHOD)
            .paramsAsDto(
                newDto(GitCheckoutEventDto.class)
                    .withName(name)
                    .withType(type)
                    .withProjectName(project))
            .sendAndSkipResult();
  }
}
