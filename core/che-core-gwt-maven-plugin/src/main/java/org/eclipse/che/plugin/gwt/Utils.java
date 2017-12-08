/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.gwt;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

public class Utils {

  private Utils() {}

  /**
   * Reads content of the file from ZIP archive.
   *
   * @param zipFile ZIP file
   * @param path path of the file to read content
   * @return content of the file with the given path
   * @throws IOException if error occurs while reading
   * @throws IllegalArgumentException if file not found in ZIP archive
   */
  public static String getFileContent(ZipFile zipFile, String path) throws IOException {
    Enumeration<? extends ZipEntry> entries = zipFile.entries();

    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();

      if (path.equals(entry.getName())) {
        try (InputStream in = zipFile.getInputStream(entry)) {
          byte[] bytes = IOUtils.toByteArray(in);

          return new String(bytes);
        }
      }
    }

    throw new IllegalArgumentException(
        format("Cannot find file '%s' in '%s'", path, zipFile.getName()));
  }
}
