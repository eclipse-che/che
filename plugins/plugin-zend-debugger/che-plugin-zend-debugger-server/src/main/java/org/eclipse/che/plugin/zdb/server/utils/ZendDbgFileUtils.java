/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.zdb.server.utils;

import com.google.inject.Singleton;
import javax.inject.Inject;
import org.eclipse.che.api.fs.server.FsPaths;

/**
 * Zend debug utils.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDbgFileUtils {

  private final FsPaths fsPaths;

  @Inject
  public ZendDbgFileUtils(FsPaths fsPaths) {
    this.fsPaths = fsPaths;
  }

  /**
   * Returns local file absolute path.
   *
   * @return local file absolute path
   */
  public String findAbsolutePath(String vfsPath) {
    return fsPaths.toFsPath(vfsPath).toString();
  }
}
