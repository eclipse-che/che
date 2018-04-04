/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.client.project;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.factory.FactoryAcceptedEvent;
import org.eclipse.che.ide.api.factory.FactoryAcceptedHandler;
import org.eclipse.che.ide.api.factory.model.FactoryImpl;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.model.ProjectConfigImpl;
import org.eclipse.che.plugin.maven.client.service.MavenServerServiceClient;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;

/**
 * Provide functionality for import/re-import maven model for given project
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class MavenModelImporter implements FactoryAcceptedHandler {

  private final NotificationManager notificationManager;
  private final MavenServerServiceClient mavenClient;

  @Inject
  public MavenModelImporter(
      EventBus eventBus,
      NotificationManager notificationManager,
      MavenServerServiceClient mavenClient) {
    this.notificationManager = notificationManager;
    this.mavenClient = mavenClient;
    eventBus.addHandler(FactoryAcceptedEvent.TYPE, this);
  }

  @Override
  public void onFactoryAccepted(FactoryAcceptedEvent event) {
    final FactoryImpl factory = event.getFactory();
    final List<ProjectConfigImpl> projects = factory.getWorkspace().getProjects();
    final List<String> paths = new ArrayList<>();
    for (ProjectConfigImpl project : projects) {
      if (MavenAttributes.MAVEN_ID.equals(project.getType())) {
        paths.add(project.getPath());
      }
    }
    if (!paths.isEmpty()) {
      reimport(paths);
    }
  }

  /** @param projectPaths */
  public void reimport(List<String> projectPaths) {
    mavenClient
        .reImportProjects(projectPaths)
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(
                    "Problem with reimporting maven", arg.getMessage(), FAIL, EMERGE_MODE);
              }
            });
  }
}
