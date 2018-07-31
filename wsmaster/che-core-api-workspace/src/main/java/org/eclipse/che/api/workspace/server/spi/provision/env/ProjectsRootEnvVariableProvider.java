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
package org.eclipse.che.api.workspace.server.spi.provision.env;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.commons.lang.Pair;

/**
 * Add env variable to machines with path to root folder of projects
 *
 * @author Alexander Garagatyi
 */
public class ProjectsRootEnvVariableProvider implements EnvVarProvider {

  /** Env variable that points to root folder of projects in a machine */
  public static final String PROJECTS_ROOT_VARIABLE = "CHE_PROJECTS_ROOT";

  private String projectFolderPath;

  @Inject
  public ProjectsRootEnvVariableProvider(
      @Named("che.workspace.projects.storage") String projectFolderPath) {
    this.projectFolderPath = projectFolderPath;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return Pair.of(PROJECTS_ROOT_VARIABLE, projectFolderPath);
  }
}
