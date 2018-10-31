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
package org.eclipse.che.selenium.core.webdriver;

import static com.google.common.io.Files.createTempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipOutputStream;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.selenium.core.SeleniumWebDriver;

/** @author Dmytro Nochevnov */
public abstract class AbstractUploadUtil implements UploadUtil {
  private static final String ZIP_FILE_NAME = "upload.zip";

  @Override
  public final Path prepareToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath)
      throws IOException {
    if (!localPath.toFile().isFile()) {
      Path zipFile = Paths.get(createTempDir().toString()).resolve(ZIP_FILE_NAME);

      try (ZipOutputStream out = ZipUtils.stream(zipFile)) {
        ZipUtils.add(out, localPath, localPath);
      }
      localPath = zipFile;
    }

    return prepareFileToUpload(seleniumWebDriver, localPath);
  }

  abstract Path prepareFileToUpload(SeleniumWebDriver seleniumWebDriver, Path localPathtoFile)
      throws IOException;
}
