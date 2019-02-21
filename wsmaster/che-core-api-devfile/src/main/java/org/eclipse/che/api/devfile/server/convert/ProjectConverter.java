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
package org.eclipse.che.api.devfile.server.convert;

import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.model.Source;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;

/**
 * Helps to convert {@link ProjectConfigImpl workspace project} to {@link Project devfile project}
 * and vice versa.
 *
 * @author Sergii Leshchenko
 */
public class ProjectConverter {

  /**
   * Converts the specified workspace project to devfile project.
   *
   * @param projectConfig source workspace project
   * @return created devfile project based on the specified workspace project
   */
  public Project toDevfileProject(ProjectConfigImpl projectConfig) {
    Source source =
        new Source()
            .withType(projectConfig.getSource().getType())
            .withLocation(projectConfig.getSource().getLocation());
    return new Project().withName(projectConfig.getName()).withSource(source);
  }

  /**
   * Converts the specified devfile project to workspace project.
   *
   * @param devProject base devfile project
   * @return created workspace project based on the specified devfile project
   */
  public ProjectConfigImpl toWorkspaceProject(Project devProject) {
    ProjectConfigImpl projectConfig = new ProjectConfigImpl();
    projectConfig.setName(devProject.getName());
    projectConfig.setPath("/" + projectConfig.getName());

    SourceStorageImpl sourceStorage = new SourceStorageImpl();
    sourceStorage.setType(devProject.getSource().getType());
    sourceStorage.setLocation(devProject.getSource().getLocation());
    projectConfig.setSource(sourceStorage);
    return projectConfig;
  }
}
