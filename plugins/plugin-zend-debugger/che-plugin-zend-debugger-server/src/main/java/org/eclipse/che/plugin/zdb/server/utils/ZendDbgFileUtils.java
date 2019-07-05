/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.utils;

import com.google.inject.Singleton;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectManager;

/**
 * Zend debug utils.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDbgFileUtils {

  public static Provider<ProjectManager> projectManagerProvider;
  public static Provider<FsManager> fsManagerProvider;

  @Inject
  public ZendDbgFileUtils(
      Provider<ProjectManager> projectManagerProvider, Provider<FsManager> fsManagerProvider) {
    ZendDbgFileUtils.projectManagerProvider = projectManagerProvider;
    ZendDbgFileUtils.fsManagerProvider = fsManagerProvider;
  }
}
