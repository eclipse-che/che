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

import static org.eclipse.che.api.core.model.workspace.config.SourceStorage.REFSPEC_PARAMETER_NAME;

import com.google.common.base.Strings;
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
    String refspec = projectConfig.getSource().getParameters().get(REFSPEC_PARAMETER_NAME);
    Source source =
        new Source()
            .withType(projectConfig.getSource().getType())
            .withLocation(projectConfig.getSource().getLocation())
            .withRefspec(refspec);

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

    projectConfig.setSource(toSourceStorage(devProject.getSource()));
    return projectConfig;
  }

  private SourceStorageImpl toSourceStorage(Source source) {
    SourceStorageImpl sourceStorage = new SourceStorageImpl();

    sourceStorage.setType(source.getType());
    sourceStorage.setLocation(source.getLocation());
    String refspec = source.getRefspec();

    if (!Strings.isNullOrEmpty(refspec)) {
      sourceStorage.getParameters().put(REFSPEC_PARAMETER_NAME, refspec);
    }

    return sourceStorage;
  }
}
