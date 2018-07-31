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
package org.eclipse.che.selenium.core.webdriver;

import static com.google.common.io.Files.createTempDir;
import static org.eclipse.che.commons.lang.ZipUtils.zipDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.SeleniumWebDriver;

/** @author Dmytro Nochevnov */
public abstract class AbstractUploadUtil implements UploadUtil {
  private static final String ZIP_FILE_NAME = "upload.zip";

  @Override
  public final Path prepareToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath)
      throws IOException {
    if (!localPath.toFile().isFile()) {
      Path zipFile = Paths.get(createTempDir().toString()).resolve(ZIP_FILE_NAME);

      zipDir(localPath.toString(), localPath.toFile(), zipFile.toFile(), null);

      localPath = zipFile;
    }

    return prepareFileToUpload(seleniumWebDriver, localPath);
  }

  abstract Path prepareFileToUpload(SeleniumWebDriver seleniumWebDriver, Path localPathtoFile)
      throws IOException;
}
