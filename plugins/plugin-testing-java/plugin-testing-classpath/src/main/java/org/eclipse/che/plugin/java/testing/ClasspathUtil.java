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
package org.eclipse.che.plugin.java.testing;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.CodeSource;

/** Class which helps to build classpath. */
public class ClasspathUtil {
  /**
   * Finds jar which contains current class and returns its path.
   *
   * @param clazz container of this class should be found
   * @return path to the jar
   */
  public static String getJarPathForClass(Class<?> clazz) {
    try {
      CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
      File jarFile;

      if (codeSource.getLocation() != null) {
        jarFile = new File(codeSource.getLocation().toURI());
      } else {
        String path = clazz.getResource(clazz.getSimpleName() + ".class").getPath();
        String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
        jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
        jarFile = new File(jarFilePath);
      }
      return jarFile.getAbsolutePath();
    } catch (URISyntaxException | UnsupportedEncodingException ignore) {
    }
    return null;
  }
}
