/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.nio.file.Files.isDirectory;
import static java.util.Collections.emptyList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.git.shared.EditedRegion;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.slf4j.Logger;

/**
 * Detects changes in index and ORIG_HEAD files and sends message to client Git handler.
 *
 * @author Igor Vinokur
 */
public class GitStatusChangedDetector {

  private static final Logger LOG = getLogger(GitStatusChangedDetector.class);

  private static final String GIT_DIR = ".git";
  private static final String INDEX_FILE = "index";
  private static final String ORIG_HEAD_FILE = "ORIG_HEAD";
  private static final String INCOMING_METHOD = "track/git-index";
  private static final String OUTGOING_METHOD = "event/git/status-changed";

  private final RequestTransmitter transmitter;
  private final FileWatcherManager manager;
  private final PathTransformer pathTransformer;
  private final ProjectManager projectManager;
  private final GitConnectionFactory gitConnectionFactory;

  private final Set<String> endpointIds = newConcurrentHashSet();

  private int indexId;
  private int origHeadId;

  @Inject
  public GitStatusChangedDetector(
      RequestTransmitter transmitter,
      FileWatcherManager manager,
      PathTransformer pathTransformer,
      ProjectManager projectManager,
      GitConnectionFactory gitConnectionFactory) {
    this.transmitter = transmitter;
    this.manager = manager;
    this.pathTransformer = pathTransformer;
    this.projectManager = projectManager;
    this.gitConnectionFactory = gitConnectionFactory;
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
  public void startWatchers() {
    indexId =
        manager.registerByMatcher(
            indexMatcher(), fsEventConsumer(), modifyConsumer(), deleteConsumer());
    origHeadId =
        manager.registerByMatcher(
            origHeadMatcher(), fsEventConsumer(), fsEventConsumer(), deleteConsumer());
  }

  @PreDestroy
  public void stopWatchers() {
    manager.unRegisterByMatcher(indexId);
    manager.unRegisterByMatcher(origHeadId);
  }

  private PathMatcher origHeadMatcher() {
    return it ->
        !isDirectory(it)
            && ORIG_HEAD_FILE.equals(it.getFileName().toString())
            && GIT_DIR.equals(it.getParent().getFileName().toString());
  }

  private PathMatcher indexMatcher() {
    return it ->
        !isDirectory(it)
            && INDEX_FILE.equals(it.getFileName().toString())
            && GIT_DIR.equals(it.getParent().getFileName().toString());
  }

  private Consumer<String> createConsumer() {
    return it -> {};
  }

  private Consumer<String> modifyConsumer() {
    return fsEventConsumer();
  }

  private Consumer<String> deleteConsumer() {
    return it -> {};
  }

  private Consumer<String> fsEventConsumer() {
    return it -> endpointIds.forEach(transmitConsumer(it));
  }

  private Consumer<String> transmitConsumer(String wsPath) {
    return id -> {
      try {
        RegisteredProject project =
            projectManager
                .getClosest(wsPath)
                .orElseThrow(() -> new NotFoundException("Can't find a project"));

        String projectFsPath = pathTransformer.transform(project.getPath()).toString();
        GitConnection connection = gitConnectionFactory.getConnection(projectFsPath);
        Status status = connection.status(emptyList());
        Status statusDto = newDto(Status.class);
        statusDto.setAdded(status.getAdded());
        statusDto.setUntracked(status.getUntracked());
        statusDto.setChanged(status.getChanged());
        statusDto.setModified(status.getModified());
        statusDto.setMissing(status.getMissing());
        statusDto.setRemoved(status.getRemoved());
        statusDto.setConflicting(status.getConflicting());

        Map<String, List<EditedRegion>> modifiedFiles = new HashMap<>();
        for (String file : status.getChanged()) {
          modifiedFiles.put(file, connection.getEditedRegions(file));
        }

        StatusChangedEventDto statusChangeEventDto =
            newDto(StatusChangedEventDto.class)
                .withProjectName(connection.getWorkingDir().getName())
                .withStatus(status)
                .withModifiedFiles(modifiedFiles);
        transmitter
            .newRequest()
            .endpointId(id)
            .methodName(OUTGOING_METHOD)
            .paramsAsDto(statusChangeEventDto)
            .sendAndSkipResult();
      } catch (ServerException | NotFoundException e) {
        String errorMessage = e.getMessage();
        if (!("Not a git repository".equals(errorMessage))) {
          LOG.error(errorMessage);
        }
      }
    };
  }
}
