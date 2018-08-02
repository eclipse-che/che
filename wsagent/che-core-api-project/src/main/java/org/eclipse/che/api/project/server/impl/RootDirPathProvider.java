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
package org.eclipse.che.api.project.server.impl;

import com.google.inject.Inject;
import java.io.File;
import java.nio.file.Paths;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides ABSOLUTE path for projects root directory using: env variable 'CHE_PROJECTS_ROOT' (which
 * is set by workspace API == "project" volume configured) if not - uses
 * 'che.user.workspaces.storage' property otherwise - default with '/project' directory (backward
 * compatible solution)
 *
 * @author gazarenkov
 */
@Singleton
public class RootDirPathProvider implements Provider<String> {

  public static String DEFAULT = "/projects";

  @Inject(optional = true)
  @Named("che.user.workspaces.storage")
  protected File rootFile = null;

  @Override
  public String get() {

    String path = System.getenv("CHE_PROJECTS_ROOT");
    if (path == null && rootFile != null) {
      path = rootFile.getPath();
    }
    if (path == null) {
      path = DEFAULT;
    }

    if (Paths.get(path).isAbsolute()) {
      return path;
    }
    throw new IllegalArgumentException("Path to the project should be declared as absolute path");
  }
}
