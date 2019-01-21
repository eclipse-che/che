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
package org.eclipse.che.plugin.java.plain.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.languageserver.LanguageServiceUtils;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.notification.ProjectUpdatedEvent;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors projects activity and updates jdt.ls workspace.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class ProjectsListener {
  private final EventService eventService;
  private final ProjectManager projectManager;
  private final JavaLanguageServerExtensionService javaService;

  private static final Logger LOG = LoggerFactory.getLogger(ProjectsListener.class);

  @Inject
  public ProjectsListener(
      EventService eventService,
      ProjectManager projectManager,
      JavaLanguageServerExtensionService javaService) {
    this.eventService = eventService;
    this.projectManager = projectManager;
    this.javaService = javaService;
  }

  @PostConstruct
  protected void initializeListeners() {
    eventService.subscribe(
        new EventSubscriber<ProjectUpdatedEvent>() {
          @Override
          public void onEvent(ProjectUpdatedEvent event) {
            onProjectUpdated(event);
          }
        });
  }

  protected void onProjectUpdated(ProjectUpdatedEvent event) {
    ProjectConfig newConfig = projectManager.getOrNull(event.getProjectPath());
    if (Constants.JAVAC.equals(newConfig.getType())
        && !Constants.JAVAC.equals(event.getOldConfig().getType())) {
      LOG.info("creating plain java project for {}", event.getProjectPath());
      javaService.createSimpleProject(
          LanguageServiceUtils.prefixURI(event.getProjectPath()), "src");
    }
  }
}
