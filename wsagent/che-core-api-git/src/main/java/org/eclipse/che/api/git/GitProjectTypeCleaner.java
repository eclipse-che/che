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

import static org.eclipse.che.api.project.shared.Constants.VCS_PROVIDER_NAME;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.git.shared.event.GitRepositoryDeletedEvent;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.NewProjectConfigImpl;
import org.slf4j.Logger;

/**
 * Cleanup project config after removing git repository. Here will be cleaned: - git project type -
 * all related attributes (like current branch, remote repository and etc) - source information
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class GitProjectTypeCleaner implements EventSubscriber<GitRepositoryDeletedEvent> {

  private static final Logger LOG = getLogger(GitProjectTypeCleaner.class);

  private EventService eventService;
  private Provider<ProjectManager> projectManagerProvider;

  @Inject
  public GitProjectTypeCleaner(
      EventService eventService, Provider<ProjectManager> projectManagerProvider) {
    this.eventService = eventService;
    this.projectManagerProvider = projectManagerProvider;
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(this);
  }

  @PreDestroy
  private void unsubscribe() {
    eventService.unsubscribe(this);
  }

  private void onGitRepositoryDeletedEventReceived(String projectPath) {
    try {
      ProjectManager projectManager = projectManagerProvider.get();
      projectManager.removeType(projectPath, GitProjectType.TYPE_ID);
      ProjectConfig project = projectManager.get(projectPath).get();
      Map<String, List<String>> attributes = project.getAttributes();
      attributes.remove(GitProjectType.GIT_CURRENT_HEAD_NAME);
      attributes.remove(GitProjectType.GIT_REPOSITORY_REMOTES);
      attributes.remove(VCS_PROVIDER_NAME);

      NewProjectConfigImpl projectConfig =
          new NewProjectConfigImpl(
              project.getPath(),
              project.getType(),
              project.getMixins(),
              project.getName(),
              project.getDescription(),
              attributes,
              null,
              null);

      projectManager.update(projectConfig);
    } catch (NotFoundException e) {
      // throw if given project not found
      LOG.error(
          "Project {} not found for removing {} type", projectPath, GitProjectType.DISPLAY_NAME, e);
    } catch (ServerException e) {
      LOG.error(
          "Exception occurred during removing {} project type for project {}",
          projectPath,
          GitProjectType.DISPLAY_NAME,
          e);
    } catch (ConflictException | BadRequestException | ForbiddenException ignore) {
      // should never be here
      LOG.warn(
          "Usually should not occur. But exception occurred during removing {} project type",
          GitProjectType.DISPLAY_NAME,
          ignore);
    }
  }

  @Override
  public void onEvent(GitRepositoryDeletedEvent event) {
    onGitRepositoryDeletedEventReceived(event.getProjectPath());
  }
}
