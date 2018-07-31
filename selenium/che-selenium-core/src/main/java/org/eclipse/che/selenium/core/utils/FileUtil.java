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
package org.eclipse.che.selenium.core.utils;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** @author Dmytro Nochevnov */
public class FileUtil {

  /**
   * Checks if directoryToRemove is empty itself and remove it if it is empty only.
   *
   * @param directoryToRemove directory which is verified for emptiness
   * @throws IOException
   */
  public static void removeDirectoryIfItIsEmpty(Path directoryToRemove) throws IOException {
    if (Files.exists(directoryToRemove)
        && Files.isDirectory(directoryToRemove)
        && Files.list(directoryToRemove).collect(toList()).isEmpty()) {

      Files.delete(directoryToRemove);
    }
  }
}
