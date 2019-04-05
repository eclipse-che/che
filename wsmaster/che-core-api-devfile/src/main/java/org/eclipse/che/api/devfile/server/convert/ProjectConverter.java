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
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.che.api.core.model.workspace.devfile.Project;
import org.eclipse.che.api.core.model.workspace.devfile.Source;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;

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
  public ProjectImpl toDevfileProject(ProjectConfigImpl projectConfig) {
    String refspec = projectConfig.getSource().getParameters().get(REFSPEC_PARAMETER_NAME);
    SourceImpl source =
        new SourceImpl(
            projectConfig.getSource().getType(), projectConfig.getSource().getLocation(), refspec);

    String path = projectConfig.getPath();
    while (path != null && path.startsWith("/")) {
      path = path.substring(1);
    }

    // don't specify the clonePath if it is the same as the project name
    if (projectConfig.getName().equals(path)) {
      path = null;
    }

    return new ProjectImpl(projectConfig.getName(), source, path);
  }

  /**
   * Converts the specified devfile project to workspace project.
   *
   * @param devProject base devfile project
   * @return created workspace project based on the specified devfile project
   */
  public ProjectConfigImpl toWorkspaceProject(Project devProject) throws DevfileException {
    String clonePath = devProject.getClonePath();
    if (clonePath == null || clonePath.isEmpty()) {
      clonePath = "/" + devProject.getName();
    } else {
      clonePath = "/" + sanitizeClonePath(clonePath);
    }

    ProjectConfigImpl projectConfig = new ProjectConfigImpl();
    projectConfig.setName(devProject.getName());
    projectConfig.setPath(clonePath);

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

  private String sanitizeClonePath(String clonePath) throws DevfileException {
    URI uri = clonePathToURI(clonePath);

    if (uri.isAbsolute()) {
      throw new DevfileException(
          "The clonePath must be a relative path. This seems to be an URI with a scheme: "
              + clonePath);
    }

    uri = uri.normalize();

    String path = uri.getPath();

    if (path.isEmpty()) {
      // the user tried to trick us with something like "blah/.."
      throw new DevfileException(
          "Cloning directly into projects root is not allowed."
              + " The clonePath that resolves to empty path: "
              + clonePath);
    }

    if (path.startsWith("/")) {
      // while technically we could allow this, because the project config contains what resembles
      // an absolute path, it is better to explicitly disallow this in devfile, which is
      // user-authored. Just don't give the user impression we can support absolute paths.
      throw new DevfileException(
          "The clonePath must be a relative. This seems to be an absolute path: " + clonePath);
    }

    if (path.startsWith("..")) {
      // an attempt to escape the projects root, e.g. "ich/../muss/../../raus". That's a no-no.
      throw new DevfileException(
          "The clonePath cannot escape the projects root. Don't use .. to try and do that."
              + " The invalid path was: "
              + clonePath);
    }

    return path;
  }

  /**
   * This merely re-throws the failure during the URI conversion as {@code DevfileException}.
   *
   * @param clonePath the path to convert to URI
   * @return the URI representing the clonePath
   * @throws DevfileException when the clonePath cannot be converted to an URI
   */
  private URI clonePathToURI(String clonePath) throws DevfileException {
    try {
      return new URI(clonePath);
    } catch (URISyntaxException e) {
      throw new DevfileException("Failed to parse the clonePath.", e);
    }
  }
}
